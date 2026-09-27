package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.utils.DataFiles;
import ticket.booking.utils.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UserBookingService {

    private User user; // the logged-in user, or null when nobody is logged in

    private List<User> userList;

    private final ObjectMapper objectMapper;

    private final File usersFile;

    private final TrainService trainService;


    public UserBookingService() throws IOException{
        this(DataFiles.locate("users.json"), new TrainService());
    }

    // Tests pass their own files, so the real data is never touched
    public UserBookingService(File usersFile, TrainService trainService) throws IOException{
        this.usersFile = usersFile;
        this.trainService = trainService;
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadUsers();
    }

    private void loadUsers() throws IOException{
        userList = objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
    }

    // ------------------------------------------------------------ accounts

    /** Creates an account. Only a BCrypt hash of the password is saved. Returns false if the username is taken. */
    public boolean signUp(String username, String plainPassword) throws IOException{
        if (getUserByUsername(username).isPresent()) {
            return false;
        }
        User newUser = new User(username, UserServiceUtil.hashPassword(plainPassword),
                new ArrayList<>(), UUID.randomUUID().toString());
        userList.add(newUser);
        saveUserListToFile();
        return true;
    }

    /** Logs the user in when the password matches the stored hash. */
    public boolean login(String username, String plainPassword){
        Optional<User> foundUser = getUserByUsername(username);
        if (foundUser.isPresent() && UserServiceUtil.checkPassword(plainPassword, foundUser.get().getHashedPassword())) {
            user = foundUser.get();
            return true;
        }
        return false;
    }

    public void logout(){
        user = null;
    }

    public boolean isLoggedIn(){
        return user != null;
    }

    public String getLoggedInUsername(){
        return user == null ? null : user.getUsername();
    }

    public Optional<User> getUserByUsername(String username){
        return userList.stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    // -------------------------------------------------------------- trains

    public List<Train> getTrains(String source, String destination){
        return trainService.searchTrains(source, destination);
    }

    public List<String> getAllStations(){
        return trainService.getAllStations();
    }

    public Optional<Train> findTrain(String trainId){
        return trainService.findTrain(trainId);
    }

    // ------------------------------------------ bookings (login required)

    public List<Ticket> fetchBookings(){
        requireLogin();
        return List.copyOf(user.getTicketsBooked());
    }

    /**
     * Books one seat for the logged-in user.
     * source and destination are the stations the user searched for; row and column are 0-based.
     * Returns the new ticket, or an empty Optional if the seat is not available.
     */
    public Optional<Ticket> bookTrainSeat(Train train, String source, String destination,
                                          LocalDate dateOfTravel, int row, int column) throws IOException{
        requireLogin();
        Objects.requireNonNull(dateOfTravel, "dateOfTravel");

        if (!trainService.bookSeat(train.getTrainId(), row, column)) {
            return Optional.empty();
        }

        Ticket ticket = new Ticket(UserServiceUtil.generateTicketId(), user.getUserId(),
                TrainService.normalizeStation(source), TrainService.normalizeStation(destination),
                dateOfTravel.toString(), train.getTrainId(), train.getTrainNo(), row, column);
        user.getTicketsBooked().add(ticket);
        saveUserListToFile();
        return Optional.of(ticket);
    }

    /** Cancels one of the logged-in user's tickets and frees its seat again. */
    public boolean cancelBooking(String ticketId) throws IOException{
        requireLogin();
        if (ticketId == null || ticketId.isBlank()) {
            return false;
        }

        Optional<Ticket> ticket = user.getTicketsBooked().stream()
                .filter(t -> ticketId.trim().equals(t.getTicketId()))
                .findFirst();
        if (ticket.isEmpty()) {
            return false;
        }

        user.getTicketsBooked().remove(ticket.get());
        saveUserListToFile();

        if (ticket.get().hasSeatInfo()) { // tickets booked with the old version have no seat info
            trainService.releaseSeat(ticket.get().getTrainId(), ticket.get().getSeatRow(), ticket.get().getSeatColumn());
        }
        return true;
    }

    private void requireLogin(){
        if (user == null) {
            throw new IllegalStateException("Please log in first.");
        }
    }

    private void saveUserListToFile() throws IOException{
        objectMapper.writeValue(usersFile, userList);
    }

}
