package ticket.booking.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

public class Ticket {
    //Fields
    private String ticketId;
    private String userId;
    private String source;
    private String destination;
    private String dateOfTravel;

    // The ticket stores which train and which seat it is for (not a full copy of the train),
    // so the seat can be released again when the booking is cancelled.
    private String trainId;
    private String trainNo;
    private Integer seatRow;     // 0-based row in Train.seats
    private Integer seatColumn;  // 0-based column in Train.seats

    //Constructor
    public Ticket(){}

    public Ticket(String ticketId, String userId, String source, String destination, String dateOfTravel,
                  String trainId, String trainNo, Integer seatRow, Integer seatColumn){
        this.ticketId = ticketId;
        this.userId = userId;
        this.source = source;
        this.destination = destination;
        this.dateOfTravel = dateOfTravel;
        this.trainId = trainId;
        this.trainNo = trainNo;
        this.seatRow = seatRow;
        this.seatColumn = seatColumn;
    }

    @JsonIgnore // computed text, not something to save in users.json
    public String getTicketInfo(){
        return String.format("Ticket ID: %s belongs to User %s from %s to %s on %s", ticketId, userId, source, destination, dateOfTravel);
    }

    public String getTicketId(){
        return ticketId;
    }

    public void setTicketId(String ticketId){
        this.ticketId = ticketId;
    }

    public String getSource(){
        return source;
    }

    public void setSource(String source){
        this.source = source;
    }

    public String getUserId(){
        return userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getDestination(){
        return destination;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }

    public String getDateOfTravel(){
        return dateOfTravel;
    }

    public void setDateOfTravel(String dateOfTravel){
        this.dateOfTravel = dateOfTravel;
    }

    public String getTrainId(){
        return trainId;
    }

    public void setTrainId(String trainId){
        this.trainId = trainId;
    }

    public String getTrainNo(){
        return trainNo;
    }

    public void setTrainNo(String trainNo){
        this.trainNo = trainNo;
    }

    public Integer getSeatRow(){
        return seatRow;
    }

    public void setSeatRow(Integer seatRow){
        this.seatRow = seatRow;
    }

    public Integer getSeatColumn(){
        return seatColumn;
    }

    public void setSeatColumn(Integer seatColumn){
        this.seatColumn = seatColumn;
    }

    @JsonIgnore
    public boolean hasSeatInfo(){
        return trainId != null && seatRow != null && seatColumn != null;
    }

}
