package teamate;

import java.io.*;
import java.util.*;

/**
 * CSV utilities: load participants from CSV and save formed teams.
 * CSV columns expected (header):
 * ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType
 */
public class CSVUtil {

    /**
     * Load participants from CSV. Skips duplicate IDs/emails and malformed rows.
     * Returns list of valid Participant objects.
     */
    public static List<Participant> loadParticipants(String path) {
        List<Participant> list = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("CSV file not found: " + path);
            return list;
        }

        Set<String> seenIds = new HashSet<>();
        Set<String> seenEmails = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String header = br.readLine(); // skip header
            if (header == null) return list;

            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;
                // split but allow empty fields: -1
                String[] cols = line.split(",", -1);
                if (cols.length < 8) {
                    System.out.println("Skipping line " + lineNo + ": insufficient columns");
                    continue;
                }
                try {
                    String id = cols[0].trim();
                    String name = cols[1].trim();
                    String email = cols[2].trim();
                    String game = cols[3].trim();
                    String skillStr = cols[4].trim();
                    String role = cols[5].trim();
                    String pScoreStr = cols[6].trim();
                    String pType = cols[7].trim();

                    // basic presence checks
                    if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
                        System.out.println("Skipping line " + lineNo + ": missing mandatory fields (ID/Name/Email)");
                        continue;
                    }

                    // duplicate checks
                    if (seenIds.contains(id)) {
                        System.out.println("Skipping line " + lineNo + ": duplicate ID '" + id + "'");
                        continue;
                    }
                    if (seenEmails.contains(email.toLowerCase())) {
                        System.out.println("Skipping line " + lineNo + ": duplicate Email '" + email + "'");
                        continue;
                    }

                    // parse numeric fields with safe defaults/validation
                    int skill;
                    try { skill = Integer.parseInt(skillStr); }
                    catch (NumberFormatException nfe) { System.out.println("Line " + lineNo + ": invalid skill, setting to 1"); skill = 1; }

                    int pScore;
                    try { pScore = Integer.parseInt(pScoreStr); }
                    catch (NumberFormatException nfe) { System.out.println("Line " + lineNo + ": invalid personality score, setting to 0"); pScore = 0; }

                    // Validate ranges and known values
                    if (!Validator.isValidGame(game)) {
                        // Accept unknown games but warn
                        System.out.println("Line " + lineNo + ": unknown game '" + game + "' — accepted but consider standardising.");
                    }
                    if (!Validator.isValidRole(role)) {
                        System.out.println("Line " + lineNo + ": unknown role '" + role + "' — accepted but consider standardising.");
                    }
                    if (skill < 1) skill = 1;
                    if (skill > 10) skill = 10;
                    if (pScore < 0 || pScore > 100) {
                        pType = "Undefined";
                    }

                    Participant p = new Participant(id, name, email, game, skill, role, pScore, pType);
                    list.add(p);

                    // mark seen
                    seenIds.add(id);
                    seenEmails.add(email.toLowerCase());
                } catch (Exception ex) {
                    System.out.println("Skipping line " + lineNo + " due to unexpected error: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }
        return list;
    }

    /**
     * Save teams to CSV path.
     * Columns: TeamID,ParticipantID,Name,Email,Game,Role,Skill,PersonalityScore,PersonalityType
     */
    public static void saveTeams(String path, List<Team> teams) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("TeamID,ParticipantID,Name,Email,Game,Role,Skill,PersonalityScore,PersonalityType");
            bw.newLine();
            int teamId = 1;
            for (Team t : teams) {
                for (Participant p : t.getMembers()) {
                    bw.write(p.toCSVRow(Integer.toString(teamId)));
                    bw.newLine();
                }
                teamId++;
            }
            System.out.println("Saved formed teams to: " + path);
        } catch (IOException e) {
            System.out.println("Error writing teams CSV: " + e.getMessage());
        }
    }
}
