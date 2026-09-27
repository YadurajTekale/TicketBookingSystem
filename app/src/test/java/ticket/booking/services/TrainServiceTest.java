package ticket.booking.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrainServiceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File trainsFile;
    private TrainService trainService;

    @Before
    public void setUp() throws IOException {
        trainsFile = tempFolder.newFile("trains.json");
        Files.writeString(trainsFile.toPath(), TestData.TRAINS_JSON);
        trainService = new TrainService(trainsFile);
    }

    @Test
    public void findsTrainWhenSourceComesBeforeDestination() {
        List<Train> trains = trainService.searchTrains("lonavala", "dadar");

        assertEquals(1, trains.size());
        assertEquals("T1", trains.get(0).getTrainId());
    }

    @Test
    public void doesNotFindTrainGoingTheOtherWay() {
        assertTrue(trainService.searchTrains("dadar", "pune").isEmpty());
    }

    @Test
    public void searchIgnoresCaseAndSpaces() {
        assertEquals(1, trainService.searchTrains("  Dadar ", "Grant Road").size());
    }

    @Test
    public void unknownStationFindsNothing() {
        assertTrue(trainService.searchTrains("nashik", "dadar").isEmpty());
    }

    @Test
    public void bookingAFreeSeatIsSavedToTheFile() throws IOException {
        assertTrue(trainService.bookSeat("T1", 1, 2));

        TrainService reloaded = new TrainService(trainsFile);
        assertFalse(reloaded.findTrain("T1").orElseThrow().isSeatAvailable(1, 2));
    }

    @Test
    public void cannotBookASeatThatIsAlreadyBooked() throws IOException {
        assertFalse(trainService.bookSeat("T1", 0, 2));
    }

    @Test
    public void cannotBookASeatThatDoesNotExist() throws IOException {
        assertFalse(trainService.bookSeat("T1", 5, 0));
        assertFalse(trainService.bookSeat("T1", 0, -1));
        assertFalse(trainService.bookSeat("T999", 0, 0));
    }

    @Test
    public void releasingASeatMakesItFreeAgain() throws IOException {
        assertTrue(trainService.releaseSeat("T1", 0, 2));

        TrainService reloaded = new TrainService(trainsFile);
        assertTrue(reloaded.findTrain("T1").orElseThrow().isSeatAvailable(0, 2));
    }

    @Test
    public void countsFreeAndTotalSeats() {
        Train train = trainService.findTrain("T1").orElseThrow();

        assertEquals(5, train.availableSeatCount());
        assertEquals(6, train.totalSeatCount());
    }

    @Test
    public void listsEveryStationOnceInAlphabeticalOrder() {
        assertEquals(List.of("dadar", "grantroad", "karjat", "lonavala", "pune"), trainService.getAllStations());
    }
}
