package teamate;

/**
 * - Score calculation: sum of five 1..5 answers * 4 -> 20..100
 * - Classification ranges:
 *   90-100 -> Leader
 *   70-89  -> Balanced
 *   50-69  -> Thinker
 */
public class PersonalityClassifier {

    //Calculate scaled personality score from five answers (1..5)
    public static int calculateScore(int q1, int q2, int q3, int q4, int q5) {
        if (!validAnswer(q1) || !validAnswer(q2) || !validAnswer(q3) || !validAnswer(q4) || !validAnswer(q5)) {
            return -1;
        }
        int total = q1 + q2 + q3 + q4 + q5; // 5..25
        return total * 4; // 20..100
    }

    private static boolean validAnswer(int q) {
        return q >= 1 && q <= 5;
    }

    //Classify score to personality type

    public static String classify(int score) {
        if (score >= 90 && score <= 100) return "Leader";
        if (score >= 70 && score <= 89) return "Balanced";
        if (score >= 50 && score <= 69) return "Thinker";
        return "Undefined";
    }
}
