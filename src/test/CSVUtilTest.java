package test;

import org.junit.jupiter.api.*;
import teamate.CSVUtil;
import teamate.Participant;
import teamate.Team;

import java.io.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CSVUtilTest {

    private File tempCSV;

    @BeforeEach
    void setup() throws Exception {
        tempCSV = File.createTempFile("participants", ".csv");
        FileWriter fw = new FileWriter(tempCSV);
        fw.write("ID,Name,Email,Game,Skill,Role,PScore,PType\n");
        fw.write("1,John,j@mail.com,Valorant,8,Strategist,80,Balanced\n");
        fw.close();
    }

    @Test
    void testLoadParticipants() {
        List<Participant> list = CSVUtil.loadParticipants(tempCSV.getAbsolutePath());
        assertEquals(1, list.size());
        assertEquals("John", list.get(0).getName());
    }

    @Test
    void testSaveTeams() throws Exception {
        File out = File.createTempFile("teams", ".csv");

        Participant p = new Participant("1", "John", "j@mail.com", "Valorant",
                8, "Attacker", 80, "Balanced");

        Team t = new Team();
        t.addMember(p);

        CSVUtil.saveTeams(out.getAbsolutePath(), List.of(t));
        assertTrue(out.exists());
        assertTrue(out.length() > 0);
    }
}
