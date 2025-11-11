package teamate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple CSV reader/writer for our format.
 * Input columns: id,name,game,role,skill,q1,q2,q3,q4,q5
 */
public class CSVUtil {

    public static List<Participant> readParticipants(String filename) throws IOException {
        List<Participant> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null) throw new IOException("Empty file");
            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length < 10) {
                    throw new IOException("Invalid data at line " + lineNo + ": found " + cols.length + " columns");
                }
                String id = cols[0].trim();
                String name = cols[1].trim();
                String game = cols[2].trim();
                String role = cols[3].trim();
                int skill = Integer.parseInt(cols[4].trim());
                int[] resp = new int[5];
                for (int i=0;i<5;i++){
                    resp[i] = Integer.parseInt(cols[5 + i].trim());
                }
                Participant p = new Participant(id, name, game, role, skill, resp);
                list.add(p);
            }
        }
        return list;
    }

    public static void writeTeams(String filename, List<Team> teams) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // header
            bw.write("team_id,member_id,member_name,game,role,skill,personalityScore,personalityType");
            bw.newLine();
            for (Team t : teams) {
                for (Participant p : t.getMembers()) {
                    bw.write(String.join(",",
                            t.getId(),
                            p.getId(),
                            p.getName(),
                            p.getGame(),
                            p.getRole(),
                            Integer.toString(p.getSkill()),
                            Integer.toString(p.getPersonalityScore()),
                            p.getPersonalityType()
                    ));
                    bw.newLine();
                }
            }
        }
    }
}
