package teamate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validation utilities: email, allowed games/roles, ID format, rating ranges.
 */
public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    private static final Set<String> ALLOWED_GAMES = new HashSet<>(Arrays.asList(
            "Valorant", "DOTA 2", "FIFA", "Basketball", "Badminton", "Chess", "CS:GO"
    ));

    private static final Set<String> ALLOWED_ROLES = new HashSet<>(Arrays.asList(
            "Strategist", "Attacker", "Defender", "Supporter", "Coordinator"
    ));

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidGame(String game) {
        if (game == null) return false;
        return ALLOWED_GAMES.contains(game.trim());
    }

    public static boolean isValidRole(String role) {
        if (role == null) return false;
        return ALLOWED_ROLES.contains(role.trim());
    }

    public static boolean isValidSkill(int s) {
        return s >= 1 && s <= 10;
    }

    public static boolean isValidRating(int r) {
        return r >= 1 && r <= 5;
    }
}
