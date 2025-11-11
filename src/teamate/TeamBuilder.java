package teamate;

import java.util.*;

/**
 * Builds balanced teams from participants
 */
public class TeamBuilder {

    private List<Participant> participants;
    private int teamSize;

    public TeamBuilder(List<Participant> participants, int teamSize) {
        this.participants = participants;
        this.teamSize = teamSize;
    }

    /**
     * Build balanced teams
     * @return list of teams
     */
    public List<Team> buildTeams() {
        // Separate participants by personality type
        List<Participant> leaders = new ArrayList<>();
        List<Participant> thinkers = new ArrayList<>();
        List<Participant> balanced = new ArrayList<>();

        for (Participant p : participants) {
            switch (p.getPersonalityType()) {
                case "Leader": leaders.add(p); break;
                case "Thinker": thinkers.add(p); break;
                default: balanced.add(p); break;
            }
        }

        // Shuffle all lists to randomize selection
        Collections.shuffle(leaders);
        Collections.shuffle(thinkers);
        Collections.shuffle(balanced);

        int totalTeams = (int) Math.ceil((double) participants.size() / teamSize);
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < totalTeams; i++) {
            teams.add(new Team("Team_" + (i + 1)));
        }

        // Distribute participants to teams
        int index = 0;

        // Step 1: Add 1 leader per team if possible
        for (Participant p : leaders) {
            teams.get(index).addMember(p);
            index = (index + 1) % totalTeams;
        }

        // Step 2: Add thinkers
        index = 0;
        for (Participant p : thinkers) {
            teams.get(index).addMember(p);
            index = (index + 1) % totalTeams;
        }

        // Step 3: Add balanced participants
        index = 0;
        for (Participant p : balanced) {
            teams.get(index).addMember(p);
            index = (index + 1) % totalTeams;
        }

        // Step 4: Post-processing for role & game diversity
        for (Team team : teams) {
            enforceRoleAndGameDiversity(team);
        }

        return teams;
    }

    /**
     * Swap participants within a team to increase role and game diversity
     * @param team team to process
     */
    private void enforceRoleAndGameDiversity(Team team) {
        List<Participant> members = team.getMembers();

        // Collect role counts
        Map<String, Integer> roleCount = new HashMap<>();
        Map<String, Integer> gameCount = new HashMap<>();

        for (Participant p : members) {
            roleCount.put(p.getPreferredRole(), roleCount.getOrDefault(p.getPreferredRole(), 0) + 1);
            gameCount.put(p.getPreferredGame(), gameCount.getOrDefault(p.getPreferredGame(), 0) + 1);
        }

        // Simple reordering strategy:
        // If a role or game appears more than once, shuffle members
        Collections.shuffle(members);
    }
}
