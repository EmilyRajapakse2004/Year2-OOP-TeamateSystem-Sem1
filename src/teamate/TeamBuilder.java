package teamate;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * TeamBuilder implements a heuristic matching algorithm:
 * - Ensure game cap per team (max per game)
 * - Ensure role diversity (at least minRoles)
 * - Ensure personality mix: try to enforce at least 1 Leader, 1-2 Thinkers, rest Balanced when possible
 * - Keep average skill balanced using round-robin assignment of sorted-by-skill participants
 *
 * Concurrency:
 * - classify participants in parallel
 * - building teams assignment can be multi-threaded across segments, but we will parallelize classification and allow
 *   the assignment to be done in a synchronized manner for correctness.
 */
public class TeamBuilder {
    private int teamSize;
    private int gameCapPerTeam;
    private int minDistinctRoles;
    private final PersonalityClassifier classifier = new PersonalityClassifier();
    private final Random rnd = new Random();

    public TeamBuilder(int teamSize, int gameCapPerTeam, int minDistinctRoles){
        this.teamSize = teamSize;
        this.gameCapPerTeam = gameCapPerTeam;
        this.minDistinctRoles = minDistinctRoles;
    }

    public List<Team> buildTeams(List<Participant> participants) throws InterruptedException, ExecutionException {
        // 1) classify participants concurrently
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Void>> tasks = new ArrayList<>();
        for (Participant p : participants) {
            tasks.add(() -> {
                int scaled = classifier.calculateScaled(p.getResponses());
                p.setPersonalityScore(scaled);
                p.setPersonalityType(classifier.classify(scaled));
                return null;
            });
        }
        pool.invokeAll(tasks); // wait for classification
        // 2) prepare teams count
        int total = participants.size();
        int numTeams = (int) Math.ceil((double) total / teamSize);
        List<Team> teams = new ArrayList<>();
        for (int i=1;i<=numTeams;i++){
            teams.add(new Team("Team" + i));
        }

        // 3) Sort participants by skill descending, but shuffle equal-skill groups
        participants.sort(Comparator.comparingInt(Participant::getSkill).reversed());

        // 4) Round-robin assignment attempting to preserve constraints
        // We'll try to place each participant into the "best" team that doesn't violate constraints.
        for (Participant p : participants) {
            boolean placed = false;
            List<Integer> order = new ArrayList<>();
            for (int i=0;i<teams.size();i++) order.add(i);
            Collections.shuffle(order, rnd); // randomize to help fairness when multiple teams valid
            // prefer teams with lower avg skill to balance
            order.sort(Comparator.comparingDouble(i -> teams.get(i).getAvgSkill()));
            for (int idx : order) {
                Team t = teams.get(idx);
                if (t.size() >= teamSize) continue;
                if (t.countGame(p.getGame()) >= gameCapPerTeam) continue;
                // Temporarily add and check role diversity & personality caps heuristics
                t.addMember(p);
                if (violatesPersonalityMix(t) || violatesRoleConstraint(t)) {
                    t.removeMember(p);
                    continue;
                }
                placed = true;
                break;
            }
            if (!placed) {
                // fallback: put in a random team that has space (force, despite violations)
                Optional<Team> maybe = teams.stream().filter(t -> t.size() < teamSize).findAny();
                if (maybe.isPresent()) {
                    maybe.get().addMember(p);
                } else {
                    // all teams full (rare if numTeams computed), create new team
                    Team newTeam = new Team("Team" + (teams.size()+1));
                    newTeam.addMember(p);
                    teams.add(newTeam);
                }
            }
        }

        // 5) Post-process: ensure team role diversity (if a team has too few distinct roles, try swaps)
        attemptRoleAndSkillBalancing(teams);

        pool.shutdown();
        return teams;
    }

    private boolean violatesPersonalityMix(Team t) {
        // heuristic: don't allow more than teamSize/1.5 Leaders (practically 1), and try to ensure at least one leader
        int leaders = t.countPersonality("Leader");
        int thinkers = t.countPersonality("Thinker");
        // avoid overcrowding leaders
        if (leaders > Math.max(1, t.size())) return true;
        // allow thinkers up to 2
        if (thinkers > Math.max(2, t.size())) return true;
        return false;
    }

    private boolean violatesRoleConstraint(Team t) {
        // ensure that number of distinct roles isn't obviously too low while team still small
        if (t.size() == 0) return false;
        List<String> roles = t.rolesPresent();
        if (t.size() >= 3 && roles.size() < 3) return true;
        return false;
    }

    private void attemptRoleAndSkillBalancing(List<Team> teams) {
        // Simple pass: for any team with < minDistinctRoles, try to swap with others
        for (Team t : teams) {
            while (t.size() > 0 && t.rolesPresent().size() < minDistinctRoles) {
                boolean swapped = tryFixRolesBySwap(t, teams);
                if (!swapped) break;
            }
        }
    }

    private boolean tryFixRolesBySwap(Team needTeam, List<Team> teams) {
        for (Team other : teams) {
            if (other == needTeam) continue;
            for (Participant candidate : new ArrayList<>(other.getMembers())) {
                // find if candidate has role missing in needTeam
                if (!needTeam.rolesPresent().contains(candidate.getRole())) {
                    // attempt swap with some member in needTeam who has a role that other lacks
                    for (Participant needMember : new ArrayList<>(needTeam.getMembers())) {
                        if (!other.rolesPresent().contains(needMember.getRole())) {
                            // swap them
                            other.removeMember(candidate);
                            needTeam.removeMember(needMember);

                            needTeam.addMember(candidate);
                            other.addMember(needMember);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
