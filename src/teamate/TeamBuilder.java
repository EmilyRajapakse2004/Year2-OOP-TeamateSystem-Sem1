package teamate;

import java.util.*;
import java.util.concurrent.*;

public class TeamBuilder {
    private List<Participant> participants;
    private int teamSize;

    public TeamBuilder(List<Participant> participants, int teamSize) {
        this.participants = participants;
        this.teamSize = teamSize;
    }

    // Build balanced teams concurrently
    public List<Team> buildTeams() {
        List<Team> teams = new ArrayList<>();
        List<Participant> copy = new ArrayList<>(participants);
        Collections.shuffle(copy); // Randomize participants

        int numTeams = (int) Math.ceil(copy.size() / (double) teamSize);

        ExecutorService executor = Executors.newFixedThreadPool(numTeams);
        List<Future<Team>> futures = new ArrayList<>();

        for (int i = 0; i < numTeams; i++) {
            final int start = i * teamSize;
            final int end = Math.min(start + teamSize, copy.size());

            Callable<Team> task = () -> {
                Team team = new Team();
                Set<String> games = new HashSet<>();
                Set<String> roles = new HashSet<>();
                int leaders = 0, thinkers = 0, balanced = 0;

                for (int j = start; j < end; j++) {
                    Participant p = copy.get(j);
                    // Ensure role diversity & personality mix
                    if (roles.size() < 3 || !roles.contains(p.getPreferredRole())) {
                        team.addMember(p);
                        roles.add(p.getPreferredRole());
                        games.add(p.getPreferredGame());
                        if (p.getPersonalityType().equals("Leader")) leaders++;
                        else if (p.getPersonalityType().equals("Thinker")) thinkers++;
                        else balanced++;
                    } else {
                        team.addMember(p);
                    }
                }
                return team;
            };
            futures.add(executor.submit(task));
        }

        for (Future<Team> f : futures) {
            try {
                teams.add(f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return teams;
    }
}
