package teamate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {

    // Load participants from CSV
    public static List<Participant> loadParticipants(String filepath) {
        List<Participant> participants = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 8) continue; // skip invalid
                Participant p = new Participant(
                        data[0], data[1], data[2],
                        data[3], Integer.parseInt(data[4]),
                        data[5], Integer.parseInt(data[6]),
                        data[7]
                );
                participants.add(p);
            }
        } catch (Exception e) {
            System.out.println("Error loading CSV: " + e.getMessage());
        }
        return participants;
    }

    // Save teams to CSV
    public static void saveTeams(String filepath, List<Team> teams) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filepath))) {
            pw.println("TeamID,ParticipantID,Name,Email,Game,Role,Skill,PersonalityScore,PersonalityType");
            int teamId = 1;
            for (Team t : teams) {
                for (Participant p : t.getMembers()) {
                    pw.printf("%d,%s,%s,%s,%s,%s,%d,%d,%s\n",
                            teamId, p.getId(), p.getName(), p.getEmail(),
                            p.getPreferredGame(), p.getPreferredRole(),
                            p.getSkillLevel(), p.getPersonalityScore(),
                            p.getPersonalityType()
                    );
                }
                teamId++;
            }
        } catch (IOException e) {
            System.out.println("Error saving teams CSV: " + e.getMessage());
        }
    }
}
