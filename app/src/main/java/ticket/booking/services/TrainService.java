package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ticket.booking.entities.Train;
import ticket.booking.utils.DataFiles;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {

    private List<Train> trainList;
    private final ObjectMapper objectMapper;
    private final File trainsFile;

    public TrainService() throws IOException{
        this(DataFiles.locate("trains.json"));
    }

    // Tests pass their own file, so the real data is never touched
    public TrainService(File trainsFile) throws IOException{
        this.trainsFile = trainsFile;
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadTrains();
    }

    public void loadTrains() throws IOException{
        trainList = objectMapper.readValue(trainsFile, new TypeReference<List<Train>>() {});
    }

    /** "  Grant Road " -> "grantroad", the format station names are stored in. */
    public static String normalizeStation(String name){
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    public List<Train> searchTrains(String source, String destination){
        String from = normalizeStation(source);
        String to = normalizeStation(destination);
        return trainList.stream()
                .filter(train -> validTrain(train, from, to))
                .collect(Collectors.toList());
    }

    public Optional<Train> findTrain(String trainId){
        return trainList.stream()
                .filter(train -> train.getTrainId().equalsIgnoreCase(trainId))
                .findFirst();
    }

    /** Every station served by any train, sorted and without duplicates. */
    public List<String> getAllStations(){
        return trainList.stream()
                .flatMap(train -> train.getStations().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Marks a seat as booked and saves trains.json.
     * Returns false if the train or seat does not exist, or the seat is already booked.
     */
    public boolean bookSeat(String trainId, int row, int column) throws IOException{
        Optional<Train> train = findTrain(trainId);
        if (train.isEmpty() || !train.get().isSeatAvailable(row, column)) {
            return false;
        }
        train.get().getSeats().get(row).set(column, 1);
        saveTrainListToFile();
        return true;
    }

    /** Makes a booked seat free again (used when a booking is cancelled). */
    public boolean releaseSeat(String trainId, int row, int column) throws IOException{
        Optional<Train> train = findTrain(trainId);
        if (train.isEmpty() || !train.get().hasSeat(row, column)) {
            return false;
        }
        train.get().getSeats().get(row).set(column, 0);
        saveTrainListToFile();
        return true;
    }

    public void addTrain(Train newTrain) throws IOException{
        // Checking here if a train with the same trainId already exists
        Optional<Train> existingTrain = findTrain(newTrain.getTrainId());

        if (existingTrain.isPresent()) {
            // If a train with the same trainId exists, update it instead of adding a new one
            updateTrain(newTrain);
        } else {
            // Otherwise, add the new train to the list
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain) throws IOException{
        // Find the index of the train with the same trainId
        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i -> trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();

        if (index.isPresent()) {
            // If found, replace the existing train with the updated one
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
        } else {
            // If not found, treat it as adding a new train
            addTrain(updatedTrain);
        }
    }

    private void saveTrainListToFile() throws IOException{
        objectMapper.writeValue(trainsFile, trainList);
    }

    private boolean validTrain(Train train, String source, String destination) {
        // A train is valid when both stations are on its route
        // and the source comes before the destination (trains only run one way)
        List<String> stationList = train.getStations();
        int sourceIndex = stationList.indexOf(source);
        int destinationIndex = stationList.indexOf(destination);

        return sourceIndex != -1
                && destinationIndex != -1
                && sourceIndex < destinationIndex;
    }

}
