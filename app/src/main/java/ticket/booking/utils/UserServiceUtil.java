package ticket.booking.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Locale;
import java.util.UUID;

public class UserServiceUtil {
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException invalidHash) {
            // a missing or corrupted hash in users.json should mean "wrong password", not a crash
            return false;
        }
    }

    /** Short, random, readable ticket id such as TKT-3F9A2C10 (was the current time in milliseconds). */
    public static String generateTicketId() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
