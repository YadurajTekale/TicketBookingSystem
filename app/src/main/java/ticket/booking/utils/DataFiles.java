package ticket.booking.utils;

import java.io.File;

/**
 * Finds the JSON "database" files (users.json, trains.json).
 * Works whether the app is started from the project root (IntelliJ, ./gradlew run)
 * or from inside the app/ folder.
 */
public final class DataFiles {

    private static final String FROM_PROJECT_ROOT = "app/src/main/java/ticket/booking/localDb/";
    private static final String FROM_APP_FOLDER = "src/main/java/ticket/booking/localDb/";

    private DataFiles() {
    }

    public static File locate(String fileName) {
        File fromRoot = new File(FROM_PROJECT_ROOT + fileName);
        if (fromRoot.exists()) {
            return fromRoot;
        }
        File fromAppFolder = new File(FROM_APP_FOLDER + fileName);
        if (fromAppFolder.exists()) {
            return fromAppFolder;
        }
        return fromRoot; // not found: the error message will show the expected path
    }
}
