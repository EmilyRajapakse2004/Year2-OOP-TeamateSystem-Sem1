package teamate;

public class PersonalityClassifier {

    /**
     * responses length must be 5 with each in 1..5
     * Scaled to 100 by multiplying total by 4 (total range:5..25 -> 20..100)
     */
    public int calculateScaled(int[] responses) {
        if (responses == null || responses.length != 5)
            throw new IllegalArgumentException("Expect 5 responses");
        int total = 0;
        for (int r : responses) {
            if (r < 1 || r > 5) throw new IllegalArgumentException("Responses must be 1..5");
            total += r;
        }
        return total * 4; // scale to 100
    }

    public String classify(int scaledScore) {
        if (scaledScore >= 90 && scaledScore <= 100) return "Leader";
        if (scaledScore >= 70 && scaledScore <= 89) return "Balanced";
        if (scaledScore >= 50 && scaledScore <= 69) return "Thinker";
        // Fallbacks
        if (scaledScore < 50) return "Thinker";
        return "Balanced";
    }
}
