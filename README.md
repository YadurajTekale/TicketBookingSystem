# Ticket Booking System (CLI)

A console-based train ticket booking app written in Java. You can sign up, log in, search trains between two stations, pick a seat from a seat map, book it for a travel date, view your tickets and cancel bookings. Data is stored in JSON files with Jackson, and passwords are hashed with BCrypt.

> **Credits:** This project is based on [Ticket Booking System by Sameer Sayyed](https://github.com/Sameer07x19/TicketBookingSystem), which was built following [Lovepreet Singh](https://github.com/AlphaDecodeX)'s tutorial. What changed since then is listed under [Changes from the original](#changes-from-the-original).

## Demo

```text
  +----------------------------------------------------------+
  | MAIN MENU                                                |
  | Logged in as demo  |  No train selected                  |
  +----------------------------------------------------------+
  | 1. Sign up                  5. Book a seat               |
  | 2. Log out                  6. Cancel a booking          |
  | 3. My bookings              7. Exit                      |
  | 4. Search trains                                         |
  +----------------------------------------------------------+
  > Choose an option (1-7): 4

  -- Search trains -------------------------------------------
  Stations: Andheri, Bandra, Borivali, Byculla, Dadar, Goregaon, Grantroad, Kandivali, Malad, Vileparle
  > From: borivali
  > To: goregaon

  1 train found:

  [1] Train T123 (No. 12345)      9 of 15 seats free
      Borivali 09:00 AM  -->  Goregaon 10:00 AM
      Route: Kandivali > Borivali > Goregaon > Malad

  > Select a train (0 to go back): 1
  [OK] Train T123 selected.

    Seat      1    2    3    4    5
    Row 1    [X]  [X]  [X]  [X]  [ ]
    Row 2    [X]  [X]  [ ]  [ ]  [ ]
    Row 3    [ ]  [ ]  [ ]  [ ]  [ ]

    [ ] free   [X] booked        9 of 15 seats free
  ...
  > Row number: 3
  > Seat number: 4
  > Travel date (YYYY-MM-DD, press Enter for today): 2026-10-05
  [OK] Seat booked. Have a safe journey!
  +----------------------------------------------------------+
  | TICKET  TKT-66988140                                     |
  +----------------------------------------------------------+
  | Train   T123 (No. 12345)                                 |
  | From    Borivali   09:00 AM                              |
  | To      Goregaon   10:00 AM                              |
  | Date    2026-10-05                                       |
  | Seat    Row 3, Seat 4                                    |
  +----------------------------------------------------------+
```

## Features

- Sign up and log in (passwords are stored only as BCrypt hashes)
- Search trains between two stations, with departure and arrival times and the number of free seats
- Seat map showing free `[ ]` and booked `[X]` seats
- Book a seat for a travel date and get a ticket
- View and cancel your bookings (cancelling frees the seat again)
- Everything is saved to JSON files, so your data is still there the next time you run the app

## Tech stack

Java 21 · Gradle · Jackson (JSON) · jBCrypt · JUnit 4

## How to run

You need JDK 21. If it is not installed, Gradle can download it for you.

**IntelliJ IDEA:** open the project folder, wait for Gradle to finish syncing, then run `org.example.App`.

**Terminal**, from the project folder:

```sh
# Windows (Command Prompt or PowerShell)
.\gradlew.bat run -q --console=plain

# macOS / Linux
./gradlew run -q --console=plain
```

The first run takes a minute while Gradle downloads what it needs.

Run the tests with `.\gradlew.bat test` on Windows or `./gradlew test` on macOS/Linux.

**Demo account:** username `demo`, password `demo123`. You can also choose **1. Sign up** to create your own account.

## How it works

| Class | Package | Responsibility |
|---|---|---|
| `App` | `org.example` | Main menu, with one method per menu option |
| `ConsoleUi` | `ticket.booking.ui` | Boxes, seat map and input validation |
| `UserBookingService` | `ticket.booking.services` | Sign up, log in, book and cancel tickets |
| `TrainService` | `ticket.booking.services` | Search trains, book and release seats |
| `User`, `Train`, `Ticket` | `ticket.booking.entities` | Data classes that are saved as JSON |

- **Search:** a train matches when both stations are on its route and the source comes before the destination.
- **Seats:** each train has a 2D list of seats, where `0` means free and `1` means booked. The screen numbers rows and seats from 1, while the code uses 0-based indexes.
- **Storage:** Jackson's `ObjectMapper` reads and writes `users.json` and `trains.json` (in `app/src/main/java/ticket/booking/localDb`) using snake_case field names.
- **Passwords:** hashed with BCrypt, which adds a random salt, and checked with `BCrypt.checkpw` at login.

## Changes from the original

**Console UI**
- A new main menu shows who is logged in and which train is selected. Option 2 switches between Log in and Log out.
- Search results show departure and arrival times for your stations and how many seats are free. All stations are listed before you search.
- The seat map has row and seat numbers.
- Tickets are shown as a formatted card. Cancelling shows your bookings as a numbered list and asks for confirmation.
- Station names are matched ignoring case and spaces, so "Grant Road" finds `grantroad`.
- Output uses only ASCII characters, so it looks right in Command Prompt, PowerShell, IntelliJ and Linux terminals.

**Bug fixes**
- Typing a letter where a number was expected crashed the app (`InputMismatchException`). All inputs are now validated.
- Booking without logging in crashed with a `NullPointerException`, after the seat had already been marked as booked.
- Tickets were saved with the train's first and last station instead of the stations the user searched for.
- The travel date was always `2021-09-01`. The user now enters a date, and past dates are rejected.
- Cancelling a booking did not free the seat.
- The plain-text password was saved in `users.json` next to its hash. Now only the hash is saved.
- `./gradlew run` could not find the data files or read keyboard input. This is fixed in `app/build.gradle`.

**Code quality**
- The 200-line `main` method is split into one method per menu option, and console formatting moved into `ConsoleUi`.
- Seat booking and release moved into `TrainService`. Sign-up and login, including password hashing, moved into `UserBookingService`.
- Tickets store the train ID and seat position instead of a full copy of the train.
- Ticket IDs look like `TKT-3F9A2C10` instead of the current time in milliseconds.
- Jackson was upgraded from 2.12.6, which has known security vulnerabilities, to 2.17.2. The deprecated `PropertyNamingStrategy.SnakeCaseStrategy` was replaced with `PropertyNamingStrategies.SnakeCaseStrategy`.
- 19 JUnit tests cover search, seat booking and release, sign-up, login, booking and cancellation.

## Known limitations and ideas

- Seats are not tracked per date: once a seat is booked, it is taken for every date.
- A seat is booked for the whole route, not only between the chosen stations.
- Data lives in JSON files. A database such as MySQL would be the next step.
- The services could be exposed as a REST API with Spring Boot.
