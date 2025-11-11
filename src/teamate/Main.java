package teamate;

import java.util.List;

/**
 * Main class to run TeamMate system
 */
public class Main {
    public static void main(String[] args) {
        String filePath = "data/participants_sample.csv"; // relative path

        // 1. Load participants
        List<Participant> participants = CSVUtil.loadParticipants(filePath);
        System.out.println("Total participants loaded: " + participants.size());

        // 2. Classify personality (optional, if not in CSV)
        for (Participant p : participants) {
            String personality = PersonalityClassifier.classify(p.getPersonalityScore());
            p.setPersonalityType(personality);
        }

        // 3. Build teams (team size 5 for example)
        int teamSize = 5;
        TeamBuilder builder = new TeamBuilder(participants, teamSize);
        List<Team> teams = builder.buildTeams();

        // 4. Print teams
        for (Team t : teams) {
            System.out.println(t);
        }
    }
}
