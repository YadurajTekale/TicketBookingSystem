package ticket.booking.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)

public class User {

    private String username;
    private String userId;
    // Only the BCrypt hash is stored. The plain-text password used to be saved to users.json as well.
    private String hashedPassword;
    private List<Ticket> ticketsBooked = new ArrayList<>();

    // Constructor
    public User(){}

    public User(String username, String hashedPassword, List<Ticket> ticketsBooked, String userId){
        this.username = username;
        this.userId = userId;
        this.hashedPassword = hashedPassword;
        // a modifiable list, so tickets can be added later
        this.ticketsBooked = ticketsBooked != null ? new ArrayList<>(ticketsBooked) : new ArrayList<>();
    }

    // Getter's
    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public List<Ticket> getTicketsBooked() {
        return ticketsBooked;
    }

    public String getUserId() {
        return userId;
    }


    // Setter's
    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTicketsBooked(List<Ticket> ticketsBooked) {
        this.ticketsBooked = ticketsBooked != null ? new ArrayList<>(ticketsBooked) : new ArrayList<>();
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
}

