package teamate;

import java.util.*;
import java.util.concurrent.*;

/**
 * TeamBuilder creates balanced teams with the following constraints:
 * - teamSize between 3 and 6
 * - max 2 members from same game per team
 * - at least minRoles = min(3, teamSize)
 * - personality mix: ideally 1 Leader, 1-2 Thinkers, rest Balanced
 * - skill balance: this try to place higher skill players into teams with lower avg skill
 * - randomize when multiple options exist
 *
 * Concurrency:
 * - buildTeams uses a thread pool to assign remaining participants in parallel.
 */
public class TeamBuilder {
    private final List<Participant> participants;
    private final int teamSize;
    private final int maxSameGamePerTeam = 2;
    private final Random rnd = new Random();

    public TeamBuilder(List<Participant> participants, int teamSize) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
    }

    public List<Team> buildTeams() {
        if (teamSize < 3 || teamSize > 6) {
            throw new IllegalArgumentException("teamSize must be between 3 and 6");
        }
        int total = participants.size();
        int numTeams = (int) Math.ceil(total / (double) teamSize);
        if (numTeams == 0) return new ArrayList<>();

        // Ensures all participants have personalityType set
        participants.forEach(p -> {
            if (p.getPersonalityType() == null || p.getPersonalityType().trim().isEmpty() ||
                    p.getPersonalityType().equalsIgnoreCase("Undefined")) {
                String t = PersonalityClassifier.classify(p.getPersonalityScore());
                p.setPersonalityType(t);
            }
        });

        // split participants by personality
        List<Participant> leaders = new ArrayList<>();
        List<Participant> thinkers = new ArrayList<>();
        List<Participant> balanced = new ArrayList<>();
        List<Participant> others = new ArrayList<>();

        for (Participant p : participants) {
            String type = p.getPersonalityType();
            if ("Leader".equalsIgnoreCase(type)) leaders.add(p);
            else if ("Thinker".equalsIgnoreCase(type)) thinkers.add(p);
            else if ("Balanced".equalsIgnoreCase(type)) balanced.add(p);
            else others.add(p);
        }

        // randomize each list
        Collections.shuffle(leaders, rnd);
        Collections.shuffle(thinkers, rnd);
        Collections.shuffle(balanced, rnd);
        Collections.shuffle(others, rnd);

        // initialize teams
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < numTeams; i++) teams.add(new Team());

        // Round-robin assign: 1 leader per team if possible
        assignRoundRobin(teams, leaders);

        // assign 1 thinker per team where possible, then a second thinker to some teams (max 2)
        assignRoundRobin(teams, thinkers);
        // second pass - teams that can accept second thinker will get one
        assignRoundRobin(teams, thinkers);

        // assign 1 balanced per team to fill base personality requirements
        assignRoundRobin(teams, balanced);

        // Collect all remaining participants into pool (including leftovers from lists)
        List<Participant> remaining = new ArrayList<>();
        remaining.addAll(leaders.stream().filter(p -> !isAssigned(p, teams)).toList());
        remaining.addAll(thinkers.stream().filter(p -> !isAssigned(p, teams)).toList());
        remaining.addAll(balanced.stream().filter(p -> !isAssigned(p, teams)).toList());
        remaining.addAll(others);

        // Also add any participants not yet assigned
        for (Participant p : participants) if (!isAssigned(p, teams) && !remaining.contains(p)) remaining.add(p);

        // To help skill balance: sort remaining by skill descending (placing strong players in weaker teams)
        remaining.sort((a, b) -> Integer.compare(b.getSkillLevel(), a.getSkillLevel()));

        // Now assign remaining participants concurrently: each task picks best team for that participant
        ExecutorService exec = Executors.newFixedThreadPool(Math.min(8, Math.max(2, remaining.size())));
        List<Callable<Void>> tasks = new ArrayList<>();

        for (Participant p : remaining) {
            tasks.add(() -> {
                Team best = findBestTeamForParticipant(teams, p);
                synchronized (best) {
                    if (best.size() < teamSize) best.addMember(p);
                    else {
                        // fallback: put into any team with space (synchronized search)
                        Team fallback = null;
                        for (Team t : teams) {
                            synchronized (t) {
                                if (t.size() < teamSize) {
                                    fallback = t;
                                    break;
                                }
                            }
                        }
                        if (fallback != null) synchronized (fallback) { fallback.addMember(p); }
                    }
                }
                return null;
            });
        }

        try {
            exec.invokeAll(tasks);
        } catch (InterruptedException e) {
            System.out.println("Team assignment interrupted: " + e.getMessage());
        } finally {
            exec.shutdown();
        }

        // Post-process to try to ensure role diversity and personality mix
        postProcessSwapForRolesAndPersonality(teams);

        return teams;
    }

    // helper: assign in round-robin while ensuring team capacity
    private void assignRoundRobin(List<Team> teams, List<Participant> pool) {
        int t = 0;
        for (Iterator<Participant> it = pool.iterator(); it.hasNext(); ) {
            Participant p = it.next();
            // try to add to next team with space that doesn't violate game cap
            boolean placed = false;
            for (int tries = 0; tries < teams.size(); tries++) {
                Team team = teams.get((t + tries) % teams.size());
                synchronized (team) {
                    if (team.size() < teamSize && team.countByGame(p.getPreferredGame()) < maxSameGamePerTeam) {
                        team.addMember(p);
                        placed = true;
                        break;
                    }
                }
            }
            it.remove();
            t = (t + 1) % teams.size();
            if (!placed) {

            }
        }
    }

    // check if participant already assigned to a team
    private boolean isAssigned(Participant p, List<Team> teams) {
        for (Team t : teams) {
            if (t.getMembers().contains(p)) return true;
        }
        return false;
    }

    /**
     * Find best team for participant p using heuristic:
     * - prefer teams with space
     * - penalize if game count >= maxSameGamePerTeam
     * - penalize if adding would reduce role diversity (we prefer filling teams that miss this participant's role)
     * - prefer teams with lower average skill (to balance skill)
     * - prefer teams missing desired personality type where appropriate
     */
    private Team findBestTeamForParticipant(List<Team> teams, Participant p) {
        Team best = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (Team team : teams) {
            synchronized (team) {
                if (team.size() >= teamSize) continue;
                double score = 0.0;

                // game penalty: heavy if already at cap
                long gameCount = team.countByGame(p.getPreferredGame());
                if (gameCount >= maxSameGamePerTeam) score += 50;

                // role diversity : prefer teams missing this role
                if (team.rolesPresent().contains(p.getPreferredRole())) score += 5;
                else score -= 10;

                // personality mix: if team lacks leader and p is leader, reduce score
                if (p.getPersonalityType().equalsIgnoreCase("Leader")) {
                    if (team.countByPersonality("Leader") == 0) score -= 8;
                    else score += 4; // discourage second leader if team already has one
                } else if (p.getPersonalityType().equalsIgnoreCase("Thinker")) {
                    long thinkers = team.countByPersonality("Thinker");
                    if (thinkers < 2) score -= 4;
                    else score += 3;
                } else { // Balanced
                    // prefer teams that are low on balanced
                    long balancedCount = team.countByPersonality("Balanced");
                    if (balancedCount < teamSize) score -= 1;
                }

                // skill balancing: prefer teams with lower avg skill
                double avg = team.getAvgSkill();
                score += avg;

                // small random tie-breaker
                score += rnd.nextDouble() * 0.5;

                if (score < bestScore) {
                    bestScore = score;
                    best = team;
                }
            }
        }

        // fallback: any team with space
        if (best == null) {
            for (Team t : teams) {
                synchronized (t) {
                    if (t.size() < teamSize) {
                        best = t;
                        break;
                    }
                }
            }
        }
        return best;
    }

    /**
     * Try to swap participants between teams to improve:
     * - role diversity (at least minRoles per team)
     * - presence of at least 1 leader in each team where possible
     */
    private void postProcessSwapForRolesAndPersonality(List<Team> teams) {
        int minRoles = Math.min(3, teamSize);

        // try to ensure each team has at least one leader
        int totalLeaders = participants.stream().filter(p -> "Leader".equalsIgnoreCase(p.getPersonalityType())).toArray().length;
        if (totalLeaders >= teams.size()) {
            // for teams without leader, try to swap with a team that has >1 leader
            for (Team need : teams) {
                if (need.countByPersonality("Leader") > 0) continue; // already has
                boolean swapped = trySwapForPersonality(need, "Leader", teams);
                if (!swapped) {
                    // attempt to move any leader into this team (if team has space)
                    for (Team from : teams) {
                        if (from == need) continue;
                        if (from.countByPersonality("Leader") > 1) {
                            Participant move = null;
                            synchronized (from) {
                                for (Participant p : from.getMembers()) {
                                    if ("Leader".equalsIgnoreCase(p.getPersonalityType())) { move = p; break; }
                                }
                                if (move != null && need.size() < teamSize) {
                                    from.getMembers().remove(move);
                                    need.addMember(move);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // ensure role diversity: try swaps to increase distinct roles
        for (Team t : teams) {
            if (t.distinctRolesCount() >= minRoles) continue;
            boolean improved = true;
            while (t.distinctRolesCount() < minRoles && improved) {
                improved = tryImproveRolesBySwap(t, teams);
            }
        }
    }

    private boolean trySwapForPersonality(Team need, String personality, List<Team> teams) {
        for (Team donor : teams) {
            if (donor == need) continue;
            if (donor.countByPersonality(personality) <= 1) continue; // donor must have extra
            Participant candidate = null;
            synchronized (donor) {
                for (Participant p : donor.getMembers()) {
                    if (personality.equalsIgnoreCase(p.getPersonalityType())) { candidate = p; break; }
                }
            }
            if (candidate != null) {
                // try to find someone from need to swap back that donor is missing type/role/etc
                Participant toMoveBack = null;
                synchronized (need) {
                    for (Participant q : need.getMembers()) {
                        // prefer swapping a participant whose role is extra in need and absent in donor
                        toMoveBack = q;
                        break;
                    }
                }
                if (toMoveBack != null) {
                    synchronized (donor) { donor.getMembers().remove(candidate); }
                    synchronized (need) { need.getMembers().add(candidate); }
                    if (toMoveBack != null) {
                        synchronized (need) { need.getMembers().remove(toMoveBack); }
                        synchronized (donor) { donor.getMembers().add(toMoveBack); }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    //Attempt swaps to increase distinct roles for team 'need'

    private boolean tryImproveRolesBySwap(Team need, List<Team> teams) {
        for (Team donor : teams) {
            if (donor == need) continue;
            // find a participant in donor whose role is NOT present in need
            Participant candidate = null;
            synchronized (donor) {
                for (Participant p : donor.getMembers()) {
                    if (!need.rolesPresent().contains(p.getPreferredRole())) { candidate = p; break; }
                }
            }
            if (candidate == null) continue;
            // find participant in need to move to donor
            Participant toMove = null;
            synchronized (need) {
                for (Participant q : need.getMembers()) {
                    // choose someone whose role donor already has to maximize swap benefit
                    if (donor.rolesPresent().contains(q.getPreferredRole())) { toMove = q; break; }
                }
            }
            if (toMove == null) {
                // if not found, pick any participant from need
                synchronized (need) {
                    if (!need.getMembers().isEmpty()) toMove = need.getMembers().get(0);
                }
            }
            if (toMove != null) {
                synchronized (donor) { donor.getMembers().remove(candidate); }
                synchronized (need) { need.getMembers().remove(toMove); }
                synchronized (need) { need.getMembers().add(candidate); }
                synchronized (donor) { donor.getMembers().add(toMove); }
                return true;
            }
        }
        return false;
    }
}
