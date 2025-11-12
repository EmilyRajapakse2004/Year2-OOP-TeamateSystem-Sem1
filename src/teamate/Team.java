package teamate;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private List<Participant> members;

    public Team() {
        members = new ArrayList<>();
    }

    public void addMember(Participant p) {
        members.add(p);
    }

    public List<Participant> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team Members:\n");
        for (Participant p : members) {
            sb.append(" - ").append(p.toString()).append("\n");
        }
        return sb.toString();
    }
}
