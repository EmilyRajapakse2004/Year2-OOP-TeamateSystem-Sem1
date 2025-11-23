package teamate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//Main class
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Participant> participants = new ArrayList<>();

        System.out.println("Do you want to load participants from CSV? (y/n): ");
        String loadCSV = sc.nextLine();
        if (loadCSV.equalsIgnoreCase("y")) {
            System.out.println("Enter path to participants CSV (e.g., data/participants_sample.csv):");
            String csvPath = sc.nextLine();
            participants.addAll(CSVUtil.loadParticipants(csvPath));
        }

        // Interactive survey
        System.out.println("Do you want to add participants manually? (y/n): ");
        String manual = sc.nextLine();

        while (manual.equalsIgnoreCase("y")) {
            System.out.println("Enter participant ID:");
            String id = sc.nextLine();
            System.out.println("Enter participant Name:");
            String name = sc.nextLine();
            System.out.println("Enter Email:");
            String email = sc.nextLine();

            System.out.println("Select preferred game: Valorant, Dota, FIFA, Basketball, Badminton");
            String game = sc.nextLine();

            System.out.println("Select preferred role: Strategist, Attacker, Defender, Supporter, Coordinator");
            String role = sc.nextLine();

            System.out.println("Rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree):");
            System.out.println("Q1: I enjoy taking the lead and guiding others during group activities.");
            int q1 = sc.nextInt();
            System.out.println("Q2: I prefer analyzing situations and coming up with strategic solutions.");
            int q2 = sc.nextInt();
            System.out.println("Q3: I work well with others and enjoy collaborative teamwork.");
            int q3 = sc.nextInt();
            System.out.println("Q4: I am calm under pressure and can help maintain team morale.");
            int q4 = sc.nextInt();
            System.out.println("Q5: I like making quick decisions and adapting in dynamic situations.");
            int q5 = sc.nextInt();
            sc.nextLine();

            int score = PersonalityClassifier.calculateScore(q1, q2, q3, q4, q5);
            String type = PersonalityClassifier.classify(score);

            System.out.println("Enter skill level (1–10):");
            int skill = sc.nextInt();
            sc.nextLine();

            Participant p = new Participant(id, name, email, game, skill, role, score, type);
            participants.add(p);

            System.out.println("Participant added. Add another? (y/n): ");
            manual = sc.nextLine();
        }

        if (participants.isEmpty()) {
            System.out.println("No participants to form teams. Exiting.");
            return;
        }

        System.out.println("Enter desired team size:");
        int teamSize = sc.nextInt();

        TeamBuilder builder = new TeamBuilder(participants, teamSize);
        System.out.println("Forming teams...");
        List<Team> teams = builder.buildTeams();

        // Display the teams
        for (Team t : teams) System.out.println(t);

        CSVUtil.saveTeams("data/formed_teams.csv", teams);
        System.out.println("Teams saved to data/formed_teams.csv");
    }
}
