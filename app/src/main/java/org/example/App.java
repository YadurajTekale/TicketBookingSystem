package org.example;

import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.services.TrainService;
import ticket.booking.services.UserBookingService;
import ticket.booking.ui.ConsoleUi;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console front end of the Ticket Booking System.
 * Each menu option has its own method; all printing and reading goes through ConsoleUi.
 */
public class App {

    private static final int EXIT_OPTION = 7;

    private final UserBookingService bookingService;
    private final ConsoleUi ui;

    // The train picked in "Search trains", remembered for "Book a seat"
    private Train selectedTrain;
    private String selectedSource;
    private String selectedDestination;

    public App(UserBookingService bookingService, ConsoleUi ui) {
        this.bookingService = bookingService;
        this.ui = ui;
    }

    public static void main(String[] args) {
        ConsoleUi ui = new ConsoleUi(new Scanner(System.in), System.out);
        UserBookingService bookingService;
        try {
            bookingService = new UserBookingService();
        } catch (IOException ex) {
            ui.error("Could not load the data files: " + ex.getMessage());
            ui.info("Start the app from the project folder (see README.md).");
            return;
        }
        new App(bookingService, ui).run();
    }

    public void run() {
        ui.banner("TICKET BOOKING SYSTEM", "Search trains  -  Pick a seat  -  Manage bookings");
        try {
            while (true) {
                showMenu();
                int option = ui.readInt("Choose an option (1-" + EXIT_OPTION + ")", 1, EXIT_OPTION);
                if (option == EXIT_OPTION) {
                    ui.blankLine();
                    ui.info("Thank you for using the Ticket Booking System. Goodbye!");
                    return;
                }
                handle(option);
            }
        } catch (NoSuchElementException inputClosed) {
            // Ctrl+D / Ctrl+Z, or the end of piped input
            ui.blankLine();
            ui.info("Input closed. Goodbye!");
        }
    }

    private void showMenu() {
        String login = bookingService.isLoggedIn()
                ? "Logged in as " + bookingService.getLoggedInUsername()
                : "Not logged in";
        String train = selectedTrain == null
                ? "No train selected"
                : "Train " + selectedTrain.getTrainId() + ": " + ConsoleUi.titleCase(selectedSource)
                        + " -> " + ConsoleUi.titleCase(selectedDestination);
        String logInOrOut = bookingService.isLoggedIn() ? "2. Log out" : "2. Log in";

        ui.blankLine();
        ui.box(List.of(
                "MAIN MENU",
                login + "  |  " + train,
                ConsoleUi.DIVIDER,
                menuRow("1. Sign up", "5. Book a seat"),
                menuRow(logInOrOut, "6. Cancel a booking"),
                menuRow("3. My bookings", "7. Exit"),
                menuRow("4. Search trains", "")));
    }

    private static String menuRow(String left, String right) {
        return String.format("%-28s%s", left, right);
    }

    private void handle(int option) {
        try {
            switch (option) {
                case 1 -> signUp();
                case 2 -> {
                    if (bookingService.isLoggedIn()) {
                        logOut();
                    } else {
                        logIn();
                    }
                }
                case 3 -> showBookings();
                case 4 -> searchTrains();
                case 5 -> bookSeat();
                case 6 -> cancelBooking();
                default -> ui.error("Unknown option.");
            }
        } catch (IOException ex) {
            ui.error("Could not save your changes: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------ 1. Sign up

    private void signUp() throws IOException {
        ui.section("Sign up");
        String username = readUsername();
        String password = ui.readNonBlank("Choose a password");
        if (bookingService.signUp(username, password)) {
            bookingService.login(username, password);
            ui.success("Account created. You are now logged in as " + username + ".");
        } else {
            ui.error("The username '" + username + "' is already taken. Please choose another one.");
        }
    }

    private String readUsername() {
        while (true) {
            String username = ui.readNonBlank("Choose a username");
            if (!username.contains(" ")) {
                return username;
            }
            ui.error("Username cannot contain spaces.");
        }
    }

    // ------------------------------------------------------ 2. Log in / out

    private void logIn() {
        ui.section("Log in");
        String username = ui.readNonBlank("Username");
        String password = ui.readNonBlank("Password");
        if (bookingService.login(username, password)) {
            ui.success("Welcome, " + username + "!");
        } else {
            ui.error("Wrong username or password.");
        }
    }

    private void logOut() {
        String username = bookingService.getLoggedInUsername();
        bookingService.logout();
        ui.success("Logged out " + username + ".");
    }

    // ------------------------------------------------------- 3. My bookings

    private void showBookings() {
        if (!requireLogin()) {
            return;
        }
        ui.section("My bookings");
        List<Ticket> tickets = bookingService.fetchBookings();
        if (tickets.isEmpty()) {
            ui.info("You have no bookings yet. Use option 4 to search for a train.");
            return;
        }
        for (int i = 0; i < tickets.size(); i++) {
            if (i > 0) {
                ui.blankLine();
            }
            printTicket(tickets.get(i));
        }
    }

    // ----------------------------------------------------- 4. Search trains

    private void searchTrains() {
        ui.section("Search trains");
        ui.info("Stations: " + String.join(", ", ConsoleUi.titleCase(bookingService.getAllStations())));
        String source = TrainService.normalizeStation(ui.readNonBlank("From"));
        String destination = TrainService.normalizeStation(ui.readNonBlank("To"));

        List<Train> trains = bookingService.getTrains(source, destination);
        if (trains.isEmpty()) {
            ui.error("No trains found from " + ConsoleUi.titleCase(source)
                    + " to " + ConsoleUi.titleCase(destination) + ".");
            ui.info("Check the spelling. Trains only run in the direction of their route.");
            return;
        }

        ui.blankLine();
        ui.info(trains.size() + (trains.size() == 1 ? " train" : " trains") + " found:");
        for (int i = 0; i < trains.size(); i++) {
            printTrain(i + 1, trains.get(i), source, destination);
        }
        ui.blankLine();

        int choice = ui.readInt("Select a train (0 to go back)", 0, trains.size());
        if (choice == 0) {
            return;
        }
        selectedTrain = trains.get(choice - 1);
        selectedSource = source;
        selectedDestination = destination;
        ui.success("Train " + selectedTrain.getTrainId() + " selected.");
        ui.seatMap(selectedTrain.getSeats());
        ui.blankLine();
        ui.info("Choose option 5 to book a seat on this train.");
    }

    private void printTrain(int number, Train train, String source, String destination) {
        ui.blankLine();
        ui.info(String.format("[%d] Train %s (No. %s)      %d of %d seats free",
                number, train.getTrainId(), train.getTrainNo(),
                train.availableSeatCount(), train.totalSeatCount()));
        ui.info("    " + ConsoleUi.titleCase(source) + " " + time(train, source)
                + "  -->  " + ConsoleUi.titleCase(destination) + " " + time(train, destination));
        ui.info("    Route: " + String.join(" > ", ConsoleUi.titleCase(train.getStations())));
    }

    // ------------------------------------------------------ 5. Book a seat

    private void bookSeat() throws IOException {
        if (!requireLogin()) {
            return;
        }
        if (selectedTrain == null) {
            ui.error("Please search for a train and select it first (option 4).");
            return;
        }
        ui.section("Book a seat on train " + selectedTrain.getTrainId() + "  ("
                + ConsoleUi.titleCase(selectedSource) + " -> " + ConsoleUi.titleCase(selectedDestination) + ")");
        if (selectedTrain.availableSeatCount() == 0) {
            ui.error("Sorry, this train is full. Please choose another train.");
            return;
        }
        ui.seatMap(selectedTrain.getSeats());
        ui.blankLine();

        List<List<Integer>> seats = selectedTrain.getSeats();
        int row;
        int seat;
        while (true) {
            row = ui.readInt("Row number", 1, seats.size());
            seat = ui.readInt("Seat number", 1, seats.get(row - 1).size());
            if (selectedTrain.isSeatAvailable(row - 1, seat - 1)) {
                break;
            }
            ui.error("Row " + row + ", seat " + seat + " is already booked. Please pick a seat marked [ ].");
        }
        LocalDate date = readTravelDate();

        // The screen counts rows and seats from 1, the seat list counts from 0
        Optional<Ticket> ticket = bookingService.bookTrainSeat(
                selectedTrain, selectedSource, selectedDestination, date, row - 1, seat - 1);
        if (ticket.isPresent()) {
            ui.success("Seat booked. Have a safe journey!");
            printTicket(ticket.get());
        } else {
            ui.error("Sorry, that seat could not be booked. Please try another one.");
        }
    }

    private LocalDate readTravelDate() {
        LocalDate today = LocalDate.now();
        while (true) {
            String input = ui.readLine("Travel date (YYYY-MM-DD, press Enter for today)");
            if (input.isEmpty()) {
                return today;
            }
            try {
                LocalDate date = LocalDate.parse(input);
                if (!date.isBefore(today)) {
                    return date;
                }
                ui.error("The travel date cannot be in the past.");
            } catch (DateTimeParseException ex) {
                ui.error("Please type the date like this: " + today.plusDays(1));
            }
        }
    }

    // -------------------------------------------------- 6. Cancel a booking

    private void cancelBooking() throws IOException {
        if (!requireLogin()) {
            return;
        }
        ui.section("Cancel a booking");
        List<Ticket> tickets = bookingService.fetchBookings();
        if (tickets.isEmpty()) {
            ui.info("You have no bookings to cancel.");
            return;
        }
        for (int i = 0; i < tickets.size(); i++) {
            ui.info(String.format("%d) %s", i + 1, summary(tickets.get(i))));
        }
        ui.blankLine();

        int choice = ui.readInt("Booking to cancel (0 to go back)", 0, tickets.size());
        if (choice == 0) {
            return;
        }
        Ticket ticket = tickets.get(choice - 1);
        if (!ui.confirm("Cancel ticket " + ticket.getTicketId() + "?")) {
            ui.info("Nothing was cancelled.");
            return;
        }
        if (bookingService.cancelBooking(ticket.getTicketId())) {
            ui.success("Ticket " + ticket.getTicketId() + " cancelled. The seat is free again.");
        } else {
            ui.error("That booking could not be found.");
        }
    }

    // ------------------------------------------------------------- helpers

    /** Shows a message and returns false when nobody is logged in. */
    private boolean requireLogin() {
        if (bookingService.isLoggedIn()) {
            return true;
        }
        ui.error("Please log in first (option 2), or sign up (option 1).");
        return false;
    }

    private void printTicket(Ticket ticket) {
        Optional<Train> train = bookingService.findTrain(ticket.getTrainId());
        String departs = train.map(t -> time(t, ticket.getSource())).orElse("");
        String arrives = train.map(t -> time(t, ticket.getDestination())).orElse("");
        String trainText = ticket.getTrainId() == null
                ? "-"
                : ticket.getTrainId() + " (No. " + ticket.getTrainNo() + ")";

        List<String> lines = new ArrayList<>();
        lines.add("TICKET  " + ticket.getTicketId());
        lines.add(ConsoleUi.DIVIDER);
        lines.add(field("Train", trainText));
        lines.add(field("From", ConsoleUi.titleCase(ticket.getSource()) + "   " + departs));
        lines.add(field("To", ConsoleUi.titleCase(ticket.getDestination()) + "   " + arrives));
        lines.add(field("Date", ticket.getDateOfTravel()));
        lines.add(field("Seat", seatText(ticket)));
        ui.box(lines);
    }

    private static String summary(Ticket ticket) {
        return String.format("%s  %s -> %s  on %s  (%s)",
                ticket.getTicketId(),
                ConsoleUi.titleCase(ticket.getSource()),
                ConsoleUi.titleCase(ticket.getDestination()),
                ticket.getDateOfTravel(),
                seatText(ticket));
    }

    private static String field(String label, String value) {
        return String.format("%-8s%s", label, value == null ? "-" : value);
    }

    private static String seatText(Ticket ticket) {
        if (ticket.getSeatRow() == null || ticket.getSeatColumn() == null) {
            return "seat not recorded";
        }
        return "Row " + (ticket.getSeatRow() + 1) + ", Seat " + (ticket.getSeatColumn() + 1);
    }

    private static String time(Train train, String station) {
        if (train.getStationTimes() == null) {
            return "";
        }
        return train.getStationTimes().getOrDefault(station, "");
    }
}
