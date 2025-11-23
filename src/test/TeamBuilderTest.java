package test;

import org.junit.jupiter.api.Test;
import teamate.Participant;
import teamate.Team;
import teamate.TeamBuilder;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TeamBuilderTest {

    @Test
    void testBuildTeamsCreatesCorrectNumber() {
        List<Participant> list = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            list.add(new Participant("P" + i, "User" + i, "u"+i+"@mail.com",
                    "Dota", 5, "Role"+i, 80, "Balanced"));
        }

        TeamBuilder builder = new TeamBuilder(list, 3);
        List<Team> teams = builder.buildTeams();

        assertEquals(4, teams.size()); // 10 / 3 = 4 teams
    }

    @Test
    void testTeamsNotEmpty() {
        List<Participant> list = List.of(
                new Participant("1","A","a","Dota",5,"Attacker",80,"Leader")
        );

        TeamBuilder builder = new TeamBuilder(list, 2);
        List<Team> teams = builder.buildTeams();

        assertFalse(teams.get(0).getMembers().isEmpty());
    }
}
