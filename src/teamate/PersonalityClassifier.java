package teamate;

public class PersonalityClassifier {

    // Calculate total score from 5 answers (1–5 scale)
    public static int calculateScore(int q1, int q2, int q3, int q4, int q5) {
        int total = q1 + q2 + q3 + q4 + q5;
        return total * 4; // scale to 100
    }

    // Classify personality based on score
    public static String classify(int score) {
        if (score >= 90) return "Leader";
        if (score >= 70) return "Balanced";
        if (score >= 50) return "Thinker";
        return "Undefined";
    }
}
