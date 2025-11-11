package teamate;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team of participants
 */
public class Team {
    private List<Participant> members;
    private String teamName;

    public Team(String teamName) {
        this.teamName = teamName;
        this.members = new ArrayList<>();
    }

    public void addMember(Participant p) {
        members.add(p);
    }

    public List<Participant> getMembers() {
        return members;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getTotalSkill() {
        int total = 0;
        for (Participant p : members) {
            total += p.getSkillLevel();
        }
        return total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team: ").append(teamName).append("\n");
        for (Participant p : members) {
            sb.append("  ").append(p).append("\n");
        }
        return sb.toString();
    }
}
