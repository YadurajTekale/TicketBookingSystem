package ticket.booking.services;

/** Small sample data used by the tests (the real JSON files are never touched by tests). */
final class TestData {

    private TestData() {
    }

    // T1 runs Pune -> Dadar and has one booked seat (row 0, column 2).
    // T2 runs Dadar -> Grant Road and is full.
    static final String TRAINS_JSON = """
            [ {
              "train_id" : "T1",
              "train_no" : "11111",
              "seats" : [ [ 0, 0, 1 ], [ 0, 0, 0 ] ],
              "station_times" : {
                "pune" : "06:00 AM",
                "lonavala" : "07:15 AM",
                "karjat" : "08:00 AM",
                "dadar" : "09:30 AM"
              },
              "stations" : [ "pune", "lonavala", "karjat", "dadar" ]
            }, {
              "train_id" : "T2",
              "train_no" : "22222",
              "seats" : [ [ 1, 1 ] ],
              "station_times" : {
                "dadar" : "10:00 AM",
                "grantroad" : "10:20 AM"
              },
              "stations" : [ "dadar", "grantroad" ]
            } ]
            """;

    static final String NO_USERS_JSON = "[ ]";
}
