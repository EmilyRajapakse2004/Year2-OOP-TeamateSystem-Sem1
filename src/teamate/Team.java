package teamate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Team container with helper methods for constraints
 */
public class Team {
    private final List<Participant> members;

    public Team() {
        members = new ArrayList<>();
    }

    public List<Participant> getMembers() { return members; }

    public void addMember(Participant p) { members.add(p); }

    public int size() { return members.size(); }

    public double getAvgSkill() {
        if (members.isEmpty()) return 0.0;
        return members.stream().mapToInt(Participant::getSkillLevel).average().orElse(0.0);
    }

    public long countByGame(String game) {
        return members.stream().filter(m -> m.getPreferredGame().equalsIgnoreCase(game)).count();
    }

    public long countByRole(String role) {
        return members.stream().filter(m -> m.getPreferredRole().equalsIgnoreCase(role)).count();
    }

    public long countByPersonality(String type) {
        return members.stream().filter(m -> m.getPersonalityType().equalsIgnoreCase(type)).count();
    }

    public int distinctRolesCount() {
        return (int) members.stream().map(Participant::getPreferredRole).distinct().count();
    }

    public List<String> rolesPresent() {
        return members.stream().map(Participant::getPreferredRole).distinct().collect(Collectors.toList());
    }

    public Map<String, Long> gameDistribution() {
        return members.stream().collect(Collectors.groupingBy(Participant::getPreferredGame, Collectors.counting()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Team(size=%d, avgSkill=%.2f):\n", size(), getAvgSkill()));
        for (Participant p : members) sb.append("  ").append(p.toString()).append("\n");
        return sb.toString();
    }
}
