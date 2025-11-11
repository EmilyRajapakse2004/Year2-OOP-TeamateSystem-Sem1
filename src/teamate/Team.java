package teamate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Team {
    private String id;
    private List<Participant> members;

    public Team(String id) {
        this.id = id;
        this.members = new ArrayList<>();
    }

    public String getId(){ return id; }
    public List<Participant> getMembers(){ return members; }

    public void addMember(Participant p){ members.add(p); }
    public void removeMember(Participant p){ members.remove(p); }
    public int size(){ return members.size(); }

    public double getAvgSkill(){
        if (members.isEmpty()) return 0.0;
        return members.stream().mapToInt(Participant::getSkill).average().orElse(0.0);
    }

    public boolean containsRole(String role){
        return members.stream().anyMatch(m -> m.getRole().equalsIgnoreCase(role));
    }

    public int countPersonality(String type){
        return (int) members.stream().filter(m -> m.getPersonalityType().equalsIgnoreCase(type)).count();
    }

    public int countGame(String game){
        return (int) members.stream().filter(m -> m.getGame().equalsIgnoreCase(game)).count();
    }

    public List<String> rolesPresent(){
        return members.stream().map(Participant::getRole).distinct().collect(Collectors.toList());
    }

    @Override
    public String toString(){
        return String.format("Team %s members=%d avgSkill=%.2f", id, members.size(), getAvgSkill());
    }
}
