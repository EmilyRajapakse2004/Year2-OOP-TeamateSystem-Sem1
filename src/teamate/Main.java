package teamate;

import java.util.*;
import java.util.concurrent.*;

/**
 * Main application - role-aware (Organizer / Participant)
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Participant> participants = new ArrayList<>();
        // Keep track of IDs/emails to prevent duplicates when adding manually
        Set<String> existingIds = new HashSet<>();
        Set<String> existingEmails = new HashSet<>();

        // top-level role selection loop
        while (true) {
            System.out.println("\nSelect role: (1) Organizer  (2) Participant  (3) Exit");
            String choice = sc.nextLine().trim();
            if (choice.equals("1")) {
                organizerFlow(sc, participants, existingIds, existingEmails);
            } else if (choice.equals("2")) {
                participantFlow(sc, participants, existingIds, existingEmails);
            } else if (choice.equals("3") || choice.equalsIgnoreCase("exit")) {
                System.out.println("Exiting application.");
                break;
            } else {
                System.out.println("Invalid selection. Choose 1, 2 or 3.");
            }
        }

        sc.close();
    }

    /* Organizer menu: load CSV, define team size, form teams, view & export */
    private static void organizerFlow(Scanner sc, List<Participant> participants,
                                      Set<String> existingIds, Set<String> existingEmails) {
        Integer teamSize = null;
        while (true) {
            System.out.println("\nOrganizer Menu:");
            System.out.println("1. Load participants from CSV (data/participants_sample.csv)");
            System.out.println("2. Define team size (3-6)");
            System.out.println("3. Initiate team formation");
            System.out.println("4. View participants (count)");
            System.out.println("5. Export formed teams (if generated)");
            System.out.println("6. Back to role selection");
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1" -> {
                    List<Participant> loaded = CSVUtil.loadParticipants("data/participants_sample.csv");
                    // Add only those not already present (by ID/email)
                    int added = 0;
                    for (Participant p : loaded) {
                        if (existingIds.contains(p.getId()) || existingEmails.contains(p.getEmail().toLowerCase())) {
                            System.out.println("Skipped duplicate from CSV: " + p.getId() + " / " + p.getEmail());
                        } else {
                            participants.add(p);
                            existingIds.add(p.getId());
                            existingEmails.add(p.getEmail().toLowerCase());
                            added++;
                        }
                    }
                    System.out.println("Loaded " + added + " new participants from CSV. Total participants: " + participants.size());
                }
                case "2" -> {
                    System.out.println("Enter desired team size (3-6):");
                    String s = sc.nextLine().trim();
                    try {
                        int ts = Integer.parseInt(s);
                        if (ts < 3 || ts > 6) {
                            System.out.println("Team size must be between 3 and 6.");
                        } else {
                            teamSize = ts;
                            System.out.println("Team size set to " + teamSize);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                    }
                }
                case "3" -> {
                    if (participants.isEmpty()) {
                        System.out.println("No participants available. Load CSV or add participants first.");
                        break;
                    }
                    if (teamSize == null) {
                        System.out.println("Define team size first (option 2).");
                        break;
                    }
                    // Ensure personality types are set
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

                    TeamBuilder builder = new TeamBuilder(participants, teamSize);
                    System.out.println("Forming teams...");
                    List<Team> teams = builder.buildTeams();

                    // Display teams and save to file
                    int i = 1;
                    for (Team t : teams) {
                        System.out.println("===== Team " + i + " =====");
                        System.out.println(t.toString());
                        i++;
                    }
                    CSVUtil.saveTeams("data/formed_teams.csv", teams);
                    System.out.println("Teams formed and saved to data/formed_teams.csv");
                }
                case "4" -> {
                    System.out.println("Total participants loaded: " + participants.size());
                    // show a short sample
                    int c = 0;
                    for (Participant p : participants) {
                        System.out.println(" - " + p.toString());
                        if (++c >= 5) break;
                    }
                }
                case "5" -> {
                    System.out.println("Export uses the last formed_teams.csv file produced by the system.");
                    System.out.println("If you want a fresh export, run option 3 again to re-generate teams.");
                }
                case "6" -> {
                    return; // back to role selection
                }
                default -> System.out.println("Invalid option. Choose 1..6.");
            }
        }
    }

    /* Participant flow: complete survey (validated) */
    private static void participantFlow(Scanner sc, List<Participant> participants,
                                        Set<String> existingIds, Set<String> existingEmails) {
        System.out.println("Participant Survey - you can add your details now.");

        // read ID and check uniqueness
        String id;
        while (true) {
            System.out.println("Enter participant ID (e.g., P101):");
            id = sc.nextLine().trim();
            if (id.isEmpty()) { System.out.println("ID cannot be empty."); continue; }
            if (existingIds.contains(id)) { System.out.println("This ID already exists. Use another."); continue; }
            break;
        }

        String name;
        while (true) {
            System.out.println("Enter participant Name:");
            name = sc.nextLine().trim();
            if (name.isEmpty()) { System.out.println("Name cannot be empty."); continue; }
            break;
        }

        String email;
        while (true) {
            System.out.println("Enter Email:");
            email = sc.nextLine().trim();
            if (!Validator.isValidEmail(email)) { System.out.println("Invalid email format. Try again."); continue; }
            if (existingEmails.contains(email.toLowerCase())) { System.out.println("Email already used. Use another."); continue; }
            break;
        }

        String game;
        while (true) {
            System.out.println("Select preferred game (exact): Valorant, DOTA 2, FIFA, Basketball, Badminton, Chess, CS:GO");
            game = sc.nextLine().trim();
            if (!Validator.isValidGame(game)) {
                System.out.println("Warning: game not in standard list. Type exactly or choose a provided option.");
                // Let them accept unknown games but confirm
                System.out.println("Do you want to accept '" + game + "'? (y/n)");
                String conf = sc.nextLine().trim();
                if (!conf.equalsIgnoreCase("y")) continue;
            }
            break;
        }

        String role;
        while (true) {
            System.out.println("Select preferred role (Strategist, Attacker, Defender, Supporter, Coordinator):");
            role = sc.nextLine().trim();
            if (!Validator.isValidRole(role)) {
                System.out.println("Invalid role. Please choose one of the allowed roles.");
                continue;
            }
            break;
        }

        int q1 = askRating(sc, "Q1: I enjoy taking the lead and guiding others during group activities.");
        int q2 = askRating(sc, "Q2: I prefer analyzing situations and coming up with strategic solutions.");
        int q3 = askRating(sc, "Q3: I work well with others and enjoy collaborative teamwork.");
        int q4 = askRating(sc, "Q4: I am calm under pressure and can help maintain team morale.");
        int q5 = askRating(sc, "Q5: I like making quick decisions and adapting in dynamic situations.");

        int score = PersonalityClassifier.calculateScore(q1, q2, q3, q4, q5);
        String pType = PersonalityClassifier.classify(score);

        int skill = askSkill(sc, "Enter skill level (1-10):");

        Participant p = new Participant(id, name, email, game, skill, role, score, pType);
        participants.add(p);
        existingIds.add(id);
        existingEmails.add(email.toLowerCase());
        System.out.println("Participant added: " + p);
    }

    private static int askRating(Scanner sc, String prompt) {
        int v = 0;
        while (true) {
            System.out.println(prompt + " (1-5):");
            String s = sc.nextLine().trim();
            try {
                v = Integer.parseInt(s);
                if (!Validator.isValidRating(v)) {
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
                if (!Validator.isValidSkill(v)) {
                    System.out.println("Skill must be between 1 and 10.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
        return v;
    }
}
