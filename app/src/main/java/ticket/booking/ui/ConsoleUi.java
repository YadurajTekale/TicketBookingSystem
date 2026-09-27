package ticket.booking.ui;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * All console input and output goes through this class, so App.java only
 * describes the flow of each screen.
 *
 * Only plain ASCII characters are used for borders and markers so the output
 * looks the same in Windows Command Prompt, PowerShell, IntelliJ and Linux terminals.
 */
public class ConsoleUi {

    /** A line containing only this text is drawn as a divider inside a box. */
    public static final String DIVIDER = "-";

    private static final int MIN_BOX_WIDTH = 56;

    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleUi(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    // ---------------------------------------------------------------- output

    public void banner(String... lines) {
        int width = boxWidth(List.of(lines));
        String border = "  +" + "=".repeat(width + 2) + "+";
        out.println();
        out.println(border);
        for (String line : lines) {
            out.println("  | " + center(line, width) + " |");
        }
        out.println(border);
    }

    public void box(List<String> lines) {
        int width = boxWidth(lines);
        String border = "  +" + "-".repeat(width + 2) + "+";
        out.println(border);
        for (String line : lines) {
            if (DIVIDER.equals(line)) {
                out.println(border);
            } else {
                out.println("  | " + padRight(line, width) + " |");
            }
        }
        out.println(border);
    }

    public void section(String title) {
        String heading = "-- " + title + " ";
        out.println();
        out.println("  " + heading + "-".repeat(Math.max(3, MIN_BOX_WIDTH + 4 - heading.length())));
    }

    public void info(String message) {
        out.println("  " + message);
    }

    public void success(String message) {
        out.println("  [OK] " + message);
    }

    public void error(String message) {
        out.println("  [!]  " + message);
    }

    public void blankLine() {
        out.println();
    }

    /**
     * Prints the seat layout of a train. Rows and seats are numbered from 1
     * for the user, while the lists themselves are 0-based.
     *   0 in the data = free   -> shown as [ ]
     *   1 in the data = booked -> shown as [X]
     */
    public void seatMap(List<List<Integer>> seats) {
        int maxSeats = seats.stream().mapToInt(List::size).max().orElse(0);
        int free = 0;
        int total = 0;

        // both prefixes are 13 characters wide, so each seat number sits above the middle of its [ ]
        StringBuilder header = new StringBuilder(String.format("%-13s", "    Seat"));
        for (int seat = 1; seat <= maxSeats; seat++) {
            header.append(String.format(" %-4d", seat));
        }
        out.println();
        out.println(header.toString().stripTrailing());

        for (int row = 0; row < seats.size(); row++) {
            StringBuilder line = new StringBuilder(String.format("    Row %-3d  ", row + 1));
            for (Integer value : seats.get(row)) {
                boolean isFree = value != null && value == 0;
                line.append(isFree ? "[ ]  " : "[X]  ");
                total++;
                if (isFree) {
                    free++;
                }
            }
            out.println(line.toString().stripTrailing());
        }
        out.println();
        out.println("    [ ] free   [X] booked        " + free + " of " + total + " seats free");
    }

    // ----------------------------------------------------------------- input

    /** Reads one line. Throws NoSuchElementException when the input is closed. */
    public String readLine(String prompt) {
        out.print("  > " + prompt + ": ");
        out.flush();
        return scanner.nextLine().trim();
    }

    public String readNonBlank(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            error("This field cannot be empty.");
        }
    }

    /** Keeps asking until the user types a whole number between min and max. */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the error message below
            }
            error("Please enter a number from " + min + " to " + max + ".");
        }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String value = readLine(prompt + " (y/n)").toLowerCase(Locale.ROOT);
            if (value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("n") || value.equals("no")) {
                return false;
            }
            error("Please type y or n.");
        }
    }

    // --------------------------------------------------------------- helpers

    /** "kandivali" -> "Kandivali" (station names are stored in lowercase). */
    public static String titleCase(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public static List<String> titleCase(List<String> texts) {
        List<String> result = new ArrayList<>();
        for (String text : texts) {
            result.add(titleCase(text));
        }
        return result;
    }

    private static int boxWidth(List<String> lines) {
        int width = MIN_BOX_WIDTH;
        for (String line : lines) {
            width = Math.max(width, line.length());
        }
        return width;
    }

    private static String padRight(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }

    private static String center(String text, int width) {
        int left = Math.max(0, (width - text.length()) / 2);
        return padRight(" ".repeat(left) + text, width);
    }
}
