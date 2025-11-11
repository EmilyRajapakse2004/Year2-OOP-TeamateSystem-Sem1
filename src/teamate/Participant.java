package teamate;

import java.util.Arrays;

public class Participant {
    private String id;
    private String name;
    private String game;
    private String role;
    private int skill; // 1-100
    private int[] responses; // 5 responses 1..5
    private int personalityScore; // 0..100
    private String personalityType; // Leader/Balanced/Thinker

    public Participant(String id, String name, String game, String role, int skill, int[] responses) {
        this.id = id;
        this.name = name;
        this.game = game;
        this.role = role;
        this.skill = skill;
        this.responses = responses == null ? new int[5] : Arrays.copyOf(responses, responses.length);
    }

    public String getId(){ return id; }
    public String getName(){ return name; }
    public String getGame(){ return game; }
    public String getRole(){ return role; }
    public int getSkill(){ return skill; }
    public int[] getResponses(){ return Arrays.copyOf(responses, responses.length); }
    public int getPersonalityScore(){ return personalityScore; }
    public void setPersonalityScore(int score){ this.personalityScore = score; }
    public String getPersonalityType(){ return personalityType; }
    public void setPersonalityType(String t){ this.personalityType = t; }

    public String toCSVRow(){
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(",").append(name).append(",")
                .append(game).append(",").append(role).append(",")
                .append(skill).append(",");
        for (int i = 0; i < responses.length; i++){
            sb.append(responses[i]);
            if (i < responses.length-1) sb.append(",");
        }
        sb.append(",").append(personalityScore).append(",").append(personalityType);
        return sb.toString();
    }

    @Override
    public String toString(){
        return String.format("%s (%s) role=%s game=%s skill=%d pScore=%d pType=%s",
                name, id, role, game, skill, personalityScore, personalityType);
    }
}
