package teamate;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {
        String input = "participants_sample.csv";
        String output = "formed_teams.csv";

        // Allow CLI args to specify team size
        int teamSize = 5;
        if (args.length >= 1) {
            try {
                teamSize = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.err.println("Invalid team size passed - using default 5");
            }
        }
        System.out.println("Team size: " + teamSize);

        try {
            List<Participant> participants = CSVUtil.readParticipants(input);
            System.out.println("Loaded participants: " + participants.size());
            // print sample
            // Build teams
            TeamBuilder builder = new TeamBuilder(teamSize, 2, Math.min(3, teamSize));
            List<Team> teams = builder.buildTeams(participants);

            // Display teams summary
            for (Team t : teams) {
                System.out.println(t);
                for (Participant p : t.getMembers()) {
                    System.out.println("  - " + p);
                }
            }

            CSVUtil.writeTeams(output, teams);
            System.out.println("Formed teams written to " + output);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
