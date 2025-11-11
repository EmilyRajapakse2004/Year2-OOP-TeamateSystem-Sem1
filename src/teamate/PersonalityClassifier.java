package teamate;

/**
 * Class to determine personality type based on survey score
 */
public class PersonalityClassifier {

    /**
     * Classify personality type based on score
     * @param score personality score (0-100)
     * @return personality type: Leader, Balanced, Thinker
     */
    public static String classify(int score) {
        if (score >= 90) return "Leader";
        else if (score >= 70) return "Balanced";
        else return "Thinker;
    }
}
