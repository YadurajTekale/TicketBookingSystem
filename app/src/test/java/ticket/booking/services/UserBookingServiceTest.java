package ticket.booking.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class UserBookingServiceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File usersFile;
    private File trainsFile;
    private UserBookingService service;

    @Before
    public void setUp() throws IOException {
        trainsFile = tempFolder.newFile("trains.json");
        Files.writeString(trainsFile.toPath(), TestData.TRAINS_JSON);
        usersFile = tempFolder.newFile("users.json");
        Files.writeString(usersFile.toPath(), TestData.NO_USERS_JSON);
        service = newService();
    }

    /** Same data files, fresh objects: like restarting the app. */
    private UserBookingService newService() throws IOException {
        return new UserBookingService(usersFile, new TrainService(trainsFile));
    }

    private void signUpAndLogIn() throws IOException {
        assertTrue(service.signUp("riya", "secret1"));
        assertTrue(service.login("riya", "secret1"));
    }

    @Test
    public void signUpRejectsADuplicateUsername() throws IOException {
        assertTrue(service.signUp("riya", "secret1"));
        assertFalse(service.signUp("riya", "another"));
    }

    @Test
    public void passwordIsSavedOnlyAsABcryptHash() throws IOException {
        service.signUp("riya", "secret1");

        String savedJson = Files.readString(usersFile.toPath());
        assertFalse(savedJson.contains("secret1"));
        assertTrue(savedJson.contains("$2a$"));
    }

    @Test
    public void loginChecksThePassword() throws IOException {
        service.signUp("riya", "secret1");
        UserBookingService restarted = newService();

        assertFalse(restarted.login("riya", "wrong-password"));
        assertFalse(restarted.isLoggedIn());
        assertTrue(restarted.login("riya", "secret1"));
        assertTrue(restarted.isLoggedIn());
    }

    @Test
    public void bookingWithoutLoggingInIsRefusedAndTheSeatStaysFree() {
        Train train = service.findTrain("T1").orElseThrow();

        assertThrows(IllegalStateException.class,
                () -> service.bookTrainSeat(train, "pune", "dadar", LocalDate.now(), 0, 0));
        assertTrue(train.isSeatAvailable(0, 0));
    }

    @Test
    public void ticketHasTheSearchedStationsDateAndSeat() throws IOException {
        signUpAndLogIn();
        Train train = service.getTrains("Lonavala", "Karjat").get(0);
        LocalDate travelDate = LocalDate.now().plusDays(3);

        Ticket ticket = service.bookTrainSeat(train, "Lonavala", "Karjat", travelDate, 1, 0).orElseThrow();

        assertEquals("lonavala", ticket.getSource());
        assertEquals("karjat", ticket.getDestination());
        assertEquals(travelDate.toString(), ticket.getDateOfTravel());
        assertEquals("T1", ticket.getTrainId());
        assertEquals(Integer.valueOf(1), ticket.getSeatRow());
        assertEquals(Integer.valueOf(0), ticket.getSeatColumn());
        assertTrue(ticket.getTicketId().startsWith("TKT-"));
        assertEquals(1, service.fetchBookings().size());
    }

    @Test
    public void theSameSeatCannotBeBookedTwice() throws IOException {
        signUpAndLogIn();
        Train train = service.findTrain("T1").orElseThrow();

        assertTrue(service.bookTrainSeat(train, "pune", "dadar", LocalDate.now(), 1, 1).isPresent());
        assertFalse(service.bookTrainSeat(train, "pune", "dadar", LocalDate.now(), 1, 1).isPresent());
        assertEquals(1, service.fetchBookings().size());
    }

    @Test
    public void cancellingABookingFreesTheSeat() throws IOException {
        signUpAndLogIn();
        Train train = service.findTrain("T1").orElseThrow();
        Ticket ticket = service.bookTrainSeat(train, "pune", "dadar", LocalDate.now(), 1, 1).orElseThrow();
        assertFalse(train.isSeatAvailable(1, 1));

        assertTrue(service.cancelBooking(ticket.getTicketId()));

        assertTrue(service.fetchBookings().isEmpty());
        TrainService reloaded = new TrainService(trainsFile);
        assertTrue(reloaded.findTrain("T1").orElseThrow().isSeatAvailable(1, 1));
    }

    @Test
    public void cancellingAnUnknownTicketReturnsFalse() throws IOException {
        signUpAndLogIn();

        assertFalse(service.cancelBooking("TKT-DOESNOTEXIST"));
    }

    @Test
    public void bookingsAreStillThereAfterARestart() throws IOException {
        signUpAndLogIn();
        Train train = service.findTrain("T1").orElseThrow();
        service.bookTrainSeat(train, "pune", "dadar", LocalDate.now(), 0, 0);

        UserBookingService restarted = newService();
        assertTrue(restarted.login("riya", "secret1"));
        assertEquals(1, restarted.fetchBookings().size());
    }
}
