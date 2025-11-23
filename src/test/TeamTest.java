package test;

import org.junit.jupiter.api.Test;
import teamate.Participant;
import teamate.Team;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    @Test
    void testAddMemberAndSize() {
        Team team = new Team();
        Participant p = new Participant("1", "A", "a@mail.com", "Dota",
                5, "Supporter", 50, "Thinker");

        team.addMember(p);
        assertEquals(1, team.size());
    }

    @Test
    void testAvgSkill() {
        Team team = new Team();
        team.addMember(new Participant("1","A","a@a","FIFA",8,"Attacker",80,"Leader"));
        team.addMember(new Participant("2","B","b@b","FIFA",4,"Defender",60,"Balanced"));

        assertEquals(6.0, team.getAvgSkill());
    }

    @Test
    void testCountByRole() {
        Team t = new Team();
        t.addMember(new Participant("1","A","a","FIFA",7,"Supporter",60,"Thinker"));
        t.addMember(new Participant("2","B","b","Dota",6,"Supporter",60,"Balanced"));

        assertEquals(2, t.countByRole("Supporter"));
    }
}
