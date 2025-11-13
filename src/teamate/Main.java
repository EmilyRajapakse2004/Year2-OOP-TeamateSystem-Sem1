package teamate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Main program:
 * - load participants_sample.csv (if user chooses)
 * - optionally add participants manually via survey (Q1..Q5 + game + role + skill)
 * - ask for team size (3..6)
 * - concurrently classify (ensure personality types)
 * - build balanced teams
 * - save formed_teams.csv
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Participant> participants = new ArrayList<>();

        System.out.println("Load participants from CSV? (y/n) [CSV path: data/participants_sample.csv]");
        String load = sc.nextLine().trim();
        if (load.equalsIgnoreCase("y")) {
            participants.addAll(CSVUtil.loadParticipants("data/participants_sample.csv"));
            System.out.println("Loaded " + participants.size() + " participants from CSV.");
        }

        // allow manual additions
        System.out.println("Do you want to add participants manually via survey? (y/n)");
        String manual = sc.nextLine().trim();
        while (manual.equalsIgnoreCase("y")) {
            System.out.println("Enter participant ID (e.g., P101):");
            String id = sc.nextLine().trim();
            System.out.println("Enter participant Name:");
            String name = sc.nextLine().trim();
            System.out.println("Enter Email:");
            String email = sc.nextLine().trim();

            System.out.println("Select preferred game (type exactly): Valorant, DOTA 2, FIFA, Basketball, Badminton, Chess, CS:GO");
            String game = sc.nextLine().trim();

            System.out.println("Select preferred role (Strategist, Attacker, Defender, Supporter, Coordinator):");
            String role = sc.nextLine().trim();

            int q1 = askRating(sc, "Q1: I enjoy taking the lead and guiding others during group activities.");
            int q2 = askRating(sc, "Q2: I prefer analyzing situations and coming up with strategic solutions.");
            int q3 = askRating(sc, "Q3: I work well with others and enjoy collaborative teamwork.");
            int q4 = askRating(sc, "Q4: I am calm under pressure and can help maintain team morale.");
            int q5 = askRating(sc, "Q5: I like making quick decisions and adapting in dynamic situations.");

            int score = PersonalityClassifier.calculateScore(q1,q2,q3,q4,q5);
            String pType = PersonalityClassifier.classify(score);

            int skill = askSkill(sc, "Enter skill level (1-10):");

            Participant p = new Participant(id, name, email, game, skill, role, score, pType);
            participants.add(p);
            System.out.println("Participant added: " + p);

            System.out.println("Add another participant? (y/n)");
            manual = sc.nextLine().trim();
        }

        if (participants.isEmpty()) {
            System.out.println("No participants available. Exiting.");
            return;
        }

        // ask team size
        int teamSize = 0;
        while (true) {
            System.out.println("Enter desired team size (3 - 6):");
            String s = sc.nextLine().trim();
            try {
                teamSize = Integer.parseInt(s);
                if (teamSize < 3 || teamSize > 6) {
                    System.out.println("Team size must be between 3 and 6.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter an integer between 3 and 6.");
            }
        }

        // Concurrent classification step (if any participants missing classification)
        ExecutorService classifyPool = Executors.newFixedThreadPool(Math.min(8, participants.size()));
        List<Callable<Void>> classifyTasks = new ArrayList<>();
        for (Participant p : participants) {
            classifyTasks.add(() -> {
                if (p.getPersonalityType() == null || p.getPersonalityType().trim().isEmpty()
                        || "Undefined".equalsIgnoreCase(p.getPersonalityType())) {
                    String t = PersonalityClassifier.classify(p.getPersonalityScore());
                    p.setPersonalityType(t);
                }
                return null;
            });
        }
        try {
            classifyPool.invokeAll(classifyTasks);
        } catch (InterruptedException e) {
            System.out.println("Classification interrupted: " + e.getMessage());
        } finally {
            classifyPool.shutdown();
        }

        // Build teams
        TeamBuilder builder = new TeamBuilder(participants, teamSize);
        System.out.println("Forming teams (this may take a moment)...");
        List<Team> teams = builder.buildTeams();

        // Display teams summary
        int i = 1;
        for (Team t : teams) {
            System.out.println("===== Team " + i + " =====");
            System.out.println(t.toString());
            i++;
        }

        // Save to CSV
        CSVUtil.saveTeams("data/formed_teams.csv", teams);

        System.out.println("Done. Output saved to data/formed_teams.csv");
    }

    private static int askRating(Scanner sc, String prompt) {
        int v = 0;
        while (true) {
            System.out.println(prompt + " (1-5):");
            String s = sc.nextLine().trim();
            try {
                v = Integer.parseInt(s);
                if (v < 1 || v > 5) {
                    System.out.println("Please enter a value between 1 and 5.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
        return v;
    }

    private static int askSkill(Scanner sc, String prompt) {
        int v = 0;
        while (true) {
            System.out.println(prompt);
            String s = sc.nextLine().trim();
            try {
                v = Integer.parseInt(s);
                if (v < 1) v = 1;
                if (v > 10) v = 10;
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
        return v;
    }
}
