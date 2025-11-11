package teamate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to load participants from CSV
 */
public class CSVUtil {

    /**
     * Load participants from CSV file
     * @param filePath path to CSV file
     * @return list of Participant objects
     */
    public static List<Participant> loadParticipants(String filePath) {
        List<Participant> participants = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length >= 8) {
                    Participant p = new Participant(
                            data[0].trim(), // ID
                            data[1].trim(), // Name
                            data[2].trim(), // Email
                            data[3].trim(), // PreferredGame
                            Integer.parseInt(data[4].trim()), // SkillLevel
                            data[5].trim(), // PreferredRole
                            Integer.parseInt(data[6].trim()), // PersonalityScore
                            data[7].trim()  // PersonalityType
                    );
                    participants.add(p);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }

        return participants;
    }
}
