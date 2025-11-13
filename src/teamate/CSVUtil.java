package teamate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV utilities: load participants from CSV and save formed teams.
 * Expects input CSV columns:
 * ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType
 */
public class CSVUtil {

    public static List<Participant> loadParticipants(String path) {
        List<Participant> list = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("CSV file not found: " + path);
            return list;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String header = br.readLine(); // skip header
            if (header == null) return list;

            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",", -1); // keep empty fields
                if (cols.length < 8) {
                    System.out.println("Skipping invalid line " + lineNo + ": insufficient columns");
                    continue;
                }
                try {
                    String id = cols[0].trim();
                    String name = cols[1].trim();
                    String email = cols[2].trim();
                    String game = cols[3].trim();
                    int skill = Integer.parseInt(cols[4].trim());
                    String role = cols[5].trim();
                    int pScore = Integer.parseInt(cols[6].trim());
                    String pType = cols[7].trim();
                    // Validate ranges
                    if (skill < 1) skill = 1;
                    if (skill > 10) skill = 10;
                    if (pScore < 0 || pScore > 100) pType = "Undefined";
                    Participant p = new Participant(id, name, email, game, skill, role, pScore, pType);
                    list.add(p);
                } catch (NumberFormatException nfe) {
                    System.out.println("Skipping line " + lineNo + " due to parse error: " + nfe.getMessage());
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
