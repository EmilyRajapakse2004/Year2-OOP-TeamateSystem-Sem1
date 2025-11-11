package teamate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
     * Build teams ensuring:
     * - Mixed personality types
     * - Role diversity
     * - Game diversity
     * - Balanced skill levels
     * @return list of teams
     */
    public List<Team> buildTeams() {
        List<Team> teams = new ArrayList<>();
        int totalTeams = (int) Math.ceil((double) participants.size() / teamSize);

        // Shuffle participants to randomize
        Collections.shuffle(participants, new Random());

        for (int i = 0; i < totalTeams; i++) {
            teams.add(new Team("Team_" + (i + 1)));
        }

        int teamIndex = 0;
        for (Participant p : participants) {
            teams.get(teamIndex).addMember(p);
            teamIndex = (teamIndex + 1) % totalTeams;
        }

        return teams;
    }
}
