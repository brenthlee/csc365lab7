import java.sql.*;
import java.util.*;

// import sun.awt.www.content.audio.x_aiff;

import java.time.LocalDate;

public class InnReservations {
    static String url;
    static String name;
    static String pass;
    static final String rooms = "blee96.lab7_rooms";
    static final String reservations = "blee96.lab7_reservations";

    public static void main(String[] args) throws SQLException {
        try {
            InnReservations IR = new InnReservations();
            // // TO-DO
            // // Implement environment variables for login.
            url = System.getenv("APP_JDBC_URL");
            name = System.getenv("APP_JDBC_USER");
            pass = System.getenv("APP_JDBC_PW");
            // url =
            // "jdbc:mysql://db.labthreesixfive.com/blee96?autoReconnect=true&useSSL=false";
            // name = "blee96";
            // pass = "WinterTwenty20_365_011115373";
            while (true) {
                IR.prompt();
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error: " + e.getErrorCode());
            System.err.println("StackTrace: " + e.getStackTrace());
        }
    }

    public void prompt() throws SQLException {
        // funcReq6();
        
        int choice = -1;
        String input = "";
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the inn reservation system");
        System.out.println("Press 1 to view Rooms and Rates");
        System.out.println("Press 2 to make a Reservation");
        System.out.println("Press 3 to edit an existing Reservation");
        System.out.println("Press 4 to cancel a Reservation");
        System.out.println("Press 5 to see detailed reservation information");
        System.out.println("Press 6 to see an Overview of Revenue");
        System.out.println("Press 0 to quit\n");

        while (choice != 0) {
            System.out.print("choice: ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();

                if (choice >= 0 && choice <= 6) {
                    if (choice == 1)      { funcReq1(); }
                    else if (choice == 2) { funcReq2(); }
                    else if (choice == 3) { funcReq3(); }
                    else if (choice == 4) { funcReq4(); }
                    else if (choice == 5) { funcReq5(); }
                    else if (choice == 6) { funcReq6(); }
                    else if (choice == 0) {
                        System.out.println("Thank you! See you next time!");
                        System.exit(0);
                    }
                }
                else {
                    System.out.println("Please enter a valid command: 1-6 or 0 to quit!");
                }
            }
            else {
                System.out.println("Please enter a valid command: 1-6 or 0 to quit!");
                scanner.next();
            }
        }
        System.out.println("Goodbye");
        System.exit(0);
    }

    public void funcReq1() throws SQLException {
        String sql =
            "with DaysOccupiedByReservation as ( " +
                "select Code, " +
                    "Room, " +
                    "datediff( " +
                        "least(CheckOut, current_date()), " +
                        "greatest(CheckIn, date_add(current_date(), interval -180 day)) " +
                    ") as days_occupied_in_last_180 " +
                "from lab7_reservations " +
            "), FixDaysOccupiedByReservation as ( " +
                "select Code, Room, (case when (days_occupied_in_last_180 > 0) then days_occupied_in_last_180 else 0 end) as days_occupied_in_last_180 " +
                "from DaysOccupiedByReservation " +
            "), DaysOccupiedByRoom as ( " +
                "select Room, sum(days_occupied_in_last_180) as occupied_sum " +
                "from FixDaysOccupiedByReservation " +
                "group by Room " +
            "), FixedDaysOccupiedByRoom as ( " +
                "select Room, (case when (occupied_sum > 0) then occupied_sum else 0 end) as occupied_sum " +
                "from DaysOccupiedByRoom " +
            "), PopularityScoreByRoom as ( " +
                "select Room, round((occupied_sum / 180), 2) as popularity_score " +
                "from FixedDaysOccupiedByRoom " +
            "), RoomNextAvail as ( " +
                "select Room, max(case when (current_date() between CheckIn and CheckOut) then CheckOut else current_date() end) as next_available " +
                "from lab7_reservations " +
                "group by Room " +
            "), MostRecentCheckout as ( " +
                "select Room, max(CheckOut) as most_recent_checkout " +
                "from lab7_reservations " +
                "where CheckOut <= current_date() " +
                "group by Room " +
            "), LengthOfStay as ( " +
                "select Room, datediff(CheckOut, CheckIn) as length_of_stay " +
                "from MostRecentCheckout natural join lab7_reservations " +
                "where most_recent_checkout = CheckOut " +
            ") " +
                "select RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor, popularity_score, next_available, most_recent_checkout, length_of_stay " +
                "from lab7_rooms, PopularityScoreByRoom natural join RoomNextAvail natural join MostRecentCheckout natural join LengthOfStay " +
                "where Room = RoomCode " +
            "order by popularity_score desc;";


        try {
            executeFR1(sql);
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public void executeFR1(String sql) throws SQLException {
        Connection dbConnection = connect();
        try (Statement stmt = dbConnection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            String rc = "RoomCode";
            String rn = "RoomName";
            String beds = "Beds";
            String bedType = "bedType";
            String maxOcc = "maxOcc";
            String basePrice = "basePrice";
            String decor = "decor";
            String pop = "popularity_score";
            String lastLen = "length_of_stay";
            String lastCheckOut = "most_recent_checkout";
            String nextAvail = "next_available";
            System.out.format("\n%-8s | %-24s | %4s | %7s | %6s | %9s | %11s | %20s | %18s | %24s | %18s\n", rc, rn,
                    beds, bedType, maxOcc, basePrice, decor, pop, lastLen, lastCheckOut, nextAvail);
            while (rs.next()) {
                String Rc = rs.getString(rc);
                String Rn = rs.getString(rn);
                int Beds = rs.getInt(beds);
                String BedType = rs.getString(bedType);
                int MaxOcc = rs.getInt(maxOcc);
                int BasePrice = rs.getInt(basePrice);
                String Decor = rs.getString(decor);
                float popularity = rs.getFloat(pop);
                int LastLen = rs.getInt(lastLen);
                String LastCheckOut = rs.getString(lastCheckOut);
                String NextAvail = rs.getString(lastCheckOut);
                System.out.format("%-8s | %-24s | %4s | %7s | %6s | %9s | %11s | %20s | %18s | %24s | %18s\n", Rc, Rn,
                        Beds, BedType, MaxOcc, BasePrice, Decor, popularity, LastLen, LastCheckOut, NextAvail);
            }
        }
        System.out.println("");
    }

    public void funcReq2() throws SQLException {
        String sqlBase =
            "with OverlappingRooms as ( " +
                "select distinct Room " +
                "from lab7_reservations " +
                "where (CheckIn <=  ?  and  ?  < CheckOut) " +
            "), OverlappingRooms_ShiftRight as ( " +
                "select distinct Room " +
                "from lab7_reservations " +
                "where (CheckIn <= date_add( ? , interval 1 day) and date_add( ? , interval 1 day) < CheckOut) " +
            "), OverlappingRooms_ShiftLeft as ( " +
                "select distinct Room " +
                "from lab7_reservations " +
                "where (CheckIn <= date_add( ? , interval -1 day) and date_add( ? , interval -1 day) < CheckOut) " +
            "), OverlappingRooms_ShortenEnd as ( " +
                "select distinct Room " +
                "from lab7_reservations " +
                "where (CheckIn <= date_add( ? , interval -1 day) and ? < CheckOut) " +
            "), OverlappingRooms_ShortenStart as ( " +
                "select distinct Room " +
                "from lab7_reservations " +
                "where (CheckIn <= ? and date_add( ? , interval 1 day) < CheckOut) " +
            "), OverlappingRooms_ShiftRightTwo as ( " +
                "select distinct Room " +
                "from lab7_reservations " +
                "where (CheckIn <= date_add( ? , interval 2 day) and date_add( ? , interval 2 day) < CheckOut) " +
            "), OverlappingRooms_ShiftLeftTwo as ( " +
                "select distinct Room " +
                "from lab7_reservations " +
                "where (CheckIn <= date_add( ? , interval -2 day) and date_add( ? , interval -2 day) < CheckOut) " +
            "), AvailableRooms as ( " +
                "select RoomCode as Room, " +
                    " ?  as CheckIn, " +
                    " ?  as CheckOut, " +
                    "0 as Priority " +
                "from lab7_rooms " +
                "where RoomCode not in ( " +
                    "select Room from OverlappingRooms " +
                ") " +
            "), AvailableRooms_ShiftRight as ( " +
                "select RoomCode as Room, " +
                    "date_add( ? , interval 1 day) as CheckIn, " +
                    "date_add( ? , interval 1 day) as CheckOut, " +
                    "2 as Priority " +
                "from lab7_rooms " +
                "where RoomCode not in ( " +
                    "select Room from OverlappingRooms_ShiftRight " +
                ") " +
            "), AvailableRooms_ShiftLeft as ( " +
                "select RoomCode as Room, " +
                    "date_add( ? , interval -1 day) as CheckIn, " +
                    "date_add( ? , interval -1 day) as CheckOut, " +
                    "2 as Priority " +
                "from lab7_rooms " +
                "where RoomCode not in ( " +
                    "select Room from OverlappingRooms_ShiftLeft " +
                ") " +
            "), AvailableRooms_ShortenEnd as ( " +
                "select RoomCode as Room, " +
                    " ?  as CheckIn, " +
                    "date_add( ? , interval -1 day) as CheckOut, " +
                    "1 as Priority " +
                "from lab7_rooms " +
                "where RoomCode not in ( " +
                    "select Room from OverlappingRooms_ShortenEnd " +
                ") " +
            "), AvailableRooms_ShortenStart as ( " +
                "select RoomCode as Room, " +
                    "date_add( ? , interval 1 day) as CheckIn, " +
                    " ?  as CheckOut, " +
                    "1 as Priority " +
                "from lab7_rooms " +
                "where RoomCode not in ( " +
                    "select Room from OverlappingRooms_ShortenStart " +
                ") " +
            "), AvailableRooms_ShiftRightTwo as ( " +
                "select RoomCode as Room, " +
                    "date_add( ? , interval 2 day) as CheckIn, " +
                    "date_add( ? , interval 2 day) as CheckOut, " +
                    "3 as Priority " +
                "from lab7_rooms " +
                "where RoomCode not in ( " +
                    "select Room from OverlappingRooms_ShiftRightTwo " +
                ") " +
            "), AvailableRooms_ShiftLeftTwo as ( " +
                "select RoomCode as Room, " +
                    "date_add( ? , interval -2 day) as CheckIn, " +
                    "date_add( ? , interval -2 day) as CheckOut, " +
                    "3 as Priority " +
                "from lab7_rooms " +
                "where RoomCode not in ( " +
                    "select Room from OverlappingRooms_ShiftLeftTwo " +
                ") " +
            "), AllAvailByPriority as ( " +
                "select * " +
                "from AvailableRooms " +
                "union " +
                "select * " +
                "from AvailableRooms_ShiftRight " +
                "union " +
                "select * " +
                "from AvailableRooms_ShiftLeft " +
                "union " +
                "select * " +
                "from AvailableRooms_ShortenEnd " +
                "union " +
                "select * " +
                "from AvailableRooms_ShortenStart " +
                "union " +
                "select * " +
                "from AvailableRooms_ShiftRightTwo " +
                "union " +
                "select * " +
                "from AvailableRooms_ShiftLeftTwo " +
            "), AllAvailPlusInfo as ( " +
                "select *  " +
                "from AllAvailByPriority, lab7_rooms " +
                "where RoomCode = Room " +
                "order by Priority, Room " +
            "), CheckOccupancy as ( " +
                "select Room, CheckIn, CheckOut, Priority, bedType, basePrice " +
                "from AllAvailPlusInfo " +
                "where maxOcc >= ? " +
            "), FuzzyMatch as ( " +
                "select * " +
                "from CheckOccupancy " +
            "), FilterBed as ( " +
                "select * " +
                "from CheckOccupancy " +
                "where bedType like ? " +
            "), FilterRoom as ( " +
                "select * " +
                "from FilterBed " +
                "where Room like ? " +
            "), ExactMatch as ( " +
                "select * " +
                "from FilterRoom " +
                "where Priority = 0 " +
            ") ";

        String sqlExact =
            "select * " +
            "from ExactMatch;";

        String sqlFuzzy =
            "select * " +
            "from FuzzyMatch " +
            "limit 5;";

        System.out.println("MAKE A RESERVATION");
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter your first name: ");
        String fn = scan.next();
        System.out.println("Enter your last name: ");
        String ln = scan.next();
        System.out.println("Enter a room code or type Any: ");
        String rc = scan.next();
        System.out.println("Enter a bed type or type Any: ");
        String bt = scan.next();
        System.out.println("Enter a check-in date (YYYY-MM-DD): ");
        String ci = scan.next();
        System.out.println("Enter a check-out date (YYYY-MM-DD): ");
        String co = scan.next();

        int nc, na;
        do {
            System.out.println("Enter the number of children: ");
            nc = scan.nextInt();
            System.out.println("Enter the number of adults: ");
            na = scan.nextInt();
            if (nc + na > 4) {
                System.out.println("The total number of people per reservation is 4.\nPlease break up your group and make separate reservations.");
            }
        } while (nc + na >  4);

        // Step 1: Establish connection to RDBMS
        try (Connection dbConnection = DriverManager.getConnection(url, name, pass)) {

            dbConnection.setAutoCommit(false);
            
            // Prepare exactQuery
            PreparedStatement exactQuery = dbConnection.prepareStatement(sqlBase + sqlExact);
            for (int i = 0; i < 13; i+=2)
            {
                exactQuery.setDate(i+1, java.sql.Date.valueOf(co), java.util.Calendar.getInstance());
                exactQuery.setDate(i+2, java.sql.Date.valueOf(ci), java.util.Calendar.getInstance());
            }
            for (int i = 14; i < 27; i+=2)
            {
                exactQuery.setDate(i+1, java.sql.Date.valueOf(ci), java.util.Calendar.getInstance());
                exactQuery.setDate(i+2, java.sql.Date.valueOf(co), java.util.Calendar.getInstance());
            }
            if (bt.toUpperCase().equals("ANY"))
            {
                bt = "%";
            }
            if (rc.toUpperCase().equals("ANY"))
            {
                rc = "%";
            }
            exactQuery.setInt(29, nc + na);
            exactQuery.setString(30, bt.toUpperCase());
            exactQuery.setString(31, rc.toUpperCase());

            ResultSet exactResult = exactQuery.executeQuery();
            

            // if there is an exact reult handle it
            if (exactResult.next())
            {
                System.out.println("The following rooms match your request exactly:");
                int index = 1;
                System.out.format("\n%-12s | %-12s | %-12s | %12s | %12s\n", "Listing #", "Room Code", "Available From", "Available To", "Bed Type");
                do
                {
                    String room = exactResult.getString("Room");
                    String checkIn = exactResult.getString("CheckIn");
                    String checkOut = exactResult.getString("CheckOut");
                    int priority = exactResult.getInt("Priority");
                    String bedType = exactResult.getString("bedType");

                    System.out.format("\n%-12d | %-12s | %-12s | %12s | %12s\n", index, room, checkIn, checkOut, bedType);
                    index += 1;
                }
                while (exactResult.next());

                System.out.println("Select the desired booking by listing #, enter 0 to cancel");
                int selection = scan.nextInt();
                if (selection == 0)
                {
                    return;
                }
                else if (exactResult.absolute(selection))
                {
                    int reservationCode = ((int)(Math.random() * ((1000000000 - 10000000) + 1)) + 10000000);
                    PreparedStatement insertReservation = dbConnection.prepareStatement("insert into lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) value ( ?, ?, ?, ?, ?, ?, ?, ?, ? );");
                    
                    insertReservation.setInt(1, reservationCode);
                    insertReservation.setString(2, exactResult.getString("Room"));

                    insertReservation.setDate(3, java.sql.Date.valueOf(exactResult.getString("CheckIn")), java.util.Calendar.getInstance());
                    insertReservation.setDate(4, java.sql.Date.valueOf(exactResult.getString("CheckOut")), java.util.Calendar.getInstance());
 
                    insertReservation.setInt(5, exactResult.getInt("basePrice"));

                    insertReservation.setString(6, ln);
                    insertReservation.setString(7, fn);
                    insertReservation.setInt(8, na);
                    insertReservation.setInt(9, nc);
                    
                    System.out.println("Confirm the following reservation: Y/N");
                    System.out.format("\n%-12s | %-12s | %-12s | %12s | %12s | %12s | %12s | %12s | %12s\n",
                        "CODE", "Room", "CheckIn", "Checkout", "Rate", "LastName", "FirstName", "Adults", "Kids");

                    System.out.format("\n%-12d | %-12s | %-12s | %12s | %12d | %12s | %12s | %12d | %12d\n",
                    reservationCode, exactResult.getString("Room"), exactResult.getString("CheckIn"), exactResult.getString("CheckOut"), exactResult.getInt("basePrice"), ln, fn, na, nc);
                    String selection2 = scan.next();

                    int updated = 0;
                    if (selection2.toUpperCase().equals("Y") || selection2.toUpperCase().equals("YES")) 
                    {
                        updated = insertReservation.executeUpdate(); 
                    }
                    if (updated == 1)
                    {
                        dbConnection.rollback();
                    }
                    else
                    {
                        dbConnection.rollback();
                    }
                }
                else 
                {
                    System.out.println("Invalid Selection");
                    return;
                }
            }
            // if there is no exact result, get the fuzzy result
            else 
            {
                System.out.println("No rooms match your request exactly, here are some similar available bookings:");
                
                // Prepare exactQuery
                PreparedStatement fuzzyQuery = dbConnection.prepareStatement(sqlBase + sqlFuzzy);
                for (int i = 0; i < 13; i+=2)
                {
                    fuzzyQuery.setDate(i+1, java.sql.Date.valueOf(co), java.util.Calendar.getInstance());
                    fuzzyQuery.setDate(i+2, java.sql.Date.valueOf(ci), java.util.Calendar.getInstance());
                }
                for (int i = 14; i < 27; i+=2)
                {
                    fuzzyQuery.setDate(i+1, java.sql.Date.valueOf(ci), java.util.Calendar.getInstance());
                    fuzzyQuery.setDate(i+2, java.sql.Date.valueOf(co), java.util.Calendar.getInstance());
                }
                if (bt.toUpperCase().equals("ANY"))
                {
                    bt = "%";
                }
                if (rc.toUpperCase().equals("ANY"))
                {
                    rc = "%";
                }
                fuzzyQuery.setInt(29, nc + na);
                fuzzyQuery.setString(30, bt.toUpperCase());
                fuzzyQuery.setString(31, rc.toUpperCase());

                
                ResultSet fuzzyResult = fuzzyQuery.executeQuery();

                int index = 1;
                System.out.format("\n%-12s | %-12s | %-12s | %12s | %12s\n", "Listing #", "Room Code", "Available From", "Available To", "Bed Type");
                while (fuzzyResult.next())
                {
                    String room = fuzzyResult.getString("Room");
                    String checkIn = fuzzyResult.getString("CheckIn");
                    String checkOut = fuzzyResult.getString("CheckOut");
                    int priority = fuzzyResult.getInt("Priority");
                    String bedType = fuzzyResult.getString("bedType");

                    System.out.format("\n%-12d | %-12s | %-12s | %12s | %12s\n", index, room, checkIn, checkOut, bedType);
                    index += 1;
                }


                System.out.println("Select the desired booking by listing #, enter 0 to cancel");
                int selection = scan.nextInt();
                if (selection == 0)
                {
                    return;
                }
                else if (fuzzyResult.absolute(selection))
                {
                    int reservationCode = ((int)(Math.random() * ((1000000000 - 10000000) + 1)) + 10000000);
                    PreparedStatement insertReservation = dbConnection.prepareStatement("insert into lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) value ( ?, ?, ?, ?, ?, ?, ?, ?, ? );");
                    
                    insertReservation.setInt(1, reservationCode);
                    insertReservation.setString(2, fuzzyResult.getString("Room"));

                    insertReservation.setDate(3, java.sql.Date.valueOf(fuzzyResult.getString("CheckIn")), java.util.Calendar.getInstance());
                    insertReservation.setDate(4, java.sql.Date.valueOf(fuzzyResult.getString("CheckOut")), java.util.Calendar.getInstance());
 
                    insertReservation.setInt(5, fuzzyResult.getInt("basePrice"));

                    insertReservation.setString(6, ln);
                    insertReservation.setString(7, fn);
                    insertReservation.setInt(8, na);
                    insertReservation.setInt(9, nc);
                    
                    System.out.println("Confirm the following reservation: Y/N");
                    System.out.format("\n%-12s | %-12s | %-12s | %12s | %12s | %12s | %12s | %12s | %12s\n",
                        "CODE", "Room", "CheckIn", "Checkout", "Rate", "LastName", "FirstName", "Adults", "Kids");

                    System.out.format("\n%-12d | %-12s | %-12s | %12s | %12d | %12s | %12s | %12d | %12d\n",
                    reservationCode, fuzzyResult.getString("Room"), fuzzyResult.getString("CheckIn"), fuzzyResult.getString("CheckOut"), fuzzyResult.getInt("basePrice"), ln, fn, na, nc);
                    String selection2 = scan.next();

                    int updated = 0;
                    if (selection2.toUpperCase().equals("Y") || selection2.toUpperCase().equals("YES")) 
                    {
                        updated = insertReservation.executeUpdate(); 
                    }
                    if (updated == 1)
                    {
                        dbConnection.rollback();
                    }
                    else
                    {
                        dbConnection.rollback();
                    }
                }
                else 
                {
                    System.out.println("Invalid Selection");
                    return;
                }
            }
            
       } catch(SQLException e) {
           e.printStackTrace();
       }
    }

    // update reservation
    public void funcReq3() throws SQLException {
        // Setup
        Scanner scanner = new Scanner(System.in);
        Connection dbConnection = connect();
        dbConnection.setAutoCommit(false);

        // reservationExistsQuery
        PreparedStatement reservationExistsQuery;
        ResultSet reservationExistsResult;
        String tempInput = "";
        String roomCode = "";
        java.sql.Date checkIn = java.sql.Date.valueOf("1900-01-01");
        java.sql.Date checkOut = java.sql.Date.valueOf("2100-01-01");
        String lastName = "";
        String firstName = "";
        int numAdults = 0;
        int numKids = 0;
        int reservationCode = 0;
        boolean validResrvation = false;

        PreparedStatement ovelapReservationQuery;
        ResultSet ovelapReservationResults;
        boolean ovelapReservation = false;

        PreparedStatement updateReservationQuery;
        int updateRowsCount = 0;

        // Get Reservation Number User Input
        System.out.println("Request Reservation Change");
        System.out.println("Enter your reservation code:");
        if (scanner.hasNextInt()) {
            reservationCode = scanner.nextInt();
        }
        if (reservationCode == 0) {
            System.out.println("[Error] Invalid Entry");
            scanner.close();
            return;
        }

        // Prepare reservation existance query
        reservationExistsQuery = dbConnection.prepareStatement(
                "select true as valid, Room, CheckIn, CheckOut, LastName, FirstName, Adults, Kids from lab7_reservations where Code = ?");
        reservationExistsQuery.setInt(1, reservationCode);

        // Perform reservation existance query
        try {
            reservationExistsResult = reservationExistsQuery.executeQuery();
            while (reservationExistsResult.next()) {
                validResrvation = reservationExistsResult.getBoolean("valid");
                roomCode = reservationExistsResult.getString("Room");
                checkIn = reservationExistsResult.getDate("CheckIn", java.util.Calendar.getInstance());
                checkOut = reservationExistsResult.getDate("CheckOut", java.util.Calendar.getInstance());
                lastName = reservationExistsResult.getString("LastName");
                firstName = reservationExistsResult.getString("FirstName");
                numAdults = reservationExistsResult.getInt("Adults");
                numKids = reservationExistsResult.getInt("Kids");

            }
        } catch (SQLException e) {
            dbConnection.rollback();
            dbConnection.close();
            scanner.close();
            e.printStackTrace();
        }

        // If Valid check dates
        if (validResrvation) {

            // Allow Spaces
            scanner.useDelimiter("\n");

            // Get updated info from user
            System.out.println("Enter updated first name (leave blank or enter \"no change\" to skip):");
            tempInput = scanner.next();
            if (!tempInput.equals("no change") && !tempInput.equals("")) {
                firstName = tempInput;
            }

            // Get updated info from user
            System.out.println("Enter updated last name (leave blank or enter \"no change\" to skip):");
            tempInput = scanner.next();
            if (!tempInput.equals("no change") && !tempInput.equals("")) {
                lastName = tempInput;
            }

            // Get updated info from user
            System.out.println("Enter updated check-in date (leave blank or enter \"no change\" to skip):");
            tempInput = scanner.next();
            if (!tempInput.equals("no change") && !tempInput.equals("")) {
                checkIn = java.sql.Date.valueOf(tempInput);
            }

            // Get updated info from user
            System.out.println("Enter updated check-out date (leave blank or enter \"no change\" to skip):");
            tempInput = scanner.next();
            if (!tempInput.equals("no change") && !tempInput.equals("")) {
                checkOut = java.sql.Date.valueOf(tempInput);
            }

            // Get updated info from user
            System.out.println("Enter updated number of children (leave blank or enter \"no change\" to skip):");
            tempInput = scanner.next();
            if (!tempInput.equals("no change") && !tempInput.equals("")) {
                numKids = Integer.parseInt(tempInput);
            }

            // Get updated info from user
            System.out.println("Enter updated number of adults (leave blank or enter \"no change\" to skip):");
            tempInput = scanner.next();
            if (!tempInput.equals("no change") && !tempInput.equals("")) {
                numAdults = Integer.parseInt(tempInput);
            }

            ovelapReservationQuery = dbConnection.prepareStatement("select true as overlap " + "from lab7_reservations "
                    + "where (Room like ? ) and ((CheckIn >= ? and CheckIn < ? ) or (CheckOut > ? and CheckOut <= ? )) "
                    + "and code <> ?");
            ovelapReservationQuery.setString(1, roomCode);
            ovelapReservationQuery.setDate(2, checkIn, java.util.Calendar.getInstance());
            ovelapReservationQuery.setDate(3, checkOut, java.util.Calendar.getInstance());
            ovelapReservationQuery.setDate(4, checkIn, java.util.Calendar.getInstance());
            ovelapReservationQuery.setDate(5, checkOut, java.util.Calendar.getInstance());
            ovelapReservationQuery.setInt(6, reservationCode);

            // Perform reservation existance query
            try {
                ovelapReservationResults = ovelapReservationQuery.executeQuery();
                while (ovelapReservationResults.next()) {
                    ovelapReservation = ovelapReservationResults.getBoolean("overlap");
                }
            } catch (SQLException e) {
                dbConnection.rollback();
                dbConnection.close();
                scanner.close();
                e.printStackTrace();
            }

            if (ovelapReservation) {
                System.out.println("New reservation dates overlap with an existing reservation.");
                System.out.println("Reservation update Failed...");
            } else {
                System.out.println("No Overlap Detected");
                updateReservationQuery = dbConnection.prepareStatement("update lab7_reservations "
                        + "set CheckIn = ?, CheckOut = ?, LastName = ?, FirstName = ?, Adults = ?, Kids = ? "
                        + "where code = ?");
                updateReservationQuery.setDate(1, checkIn, java.util.Calendar.getInstance());
                updateReservationQuery.setDate(2, checkOut, java.util.Calendar.getInstance());
                updateReservationQuery.setString(3, lastName);
                updateReservationQuery.setString(4, firstName);
                updateReservationQuery.setInt(5, numAdults);
                updateReservationQuery.setInt(6, numKids);
                updateReservationQuery.setInt(7, reservationCode);

                // Perform reservation update
                try {
                    updateRowsCount = updateReservationQuery.executeUpdate();
                    dbConnection.commit();
                } catch (SQLException e) {
                    dbConnection.rollback();
                    dbConnection.close();
                    scanner.close();
                    e.printStackTrace();
                }
                System.out.println(String.valueOf(updateRowsCount) + " reservations updated with the following:");
                System.out.println("reservationCode: " + String.valueOf(reservationCode));
                System.out.println("checkIn: " + checkIn.toString());
                System.out.println("checkOut: " + checkOut.toString());
                System.out.println("lastName: " + lastName);
                System.out.println("firstName: " + firstName);
                System.out.println("adults: " + String.valueOf(numAdults));
                System.out.println("kids: " + String.valueOf(numKids));
            }
        } else {
            System.out.println("Reservation Code Not Valid, Update Failed...");
        }

        // TearDown
        dbConnection.close();
        scanner.close();
    }

    // cancel reservation
    public void funcReq4() throws SQLException {
        // Setup
        Scanner scanner = new Scanner(System.in);
        Connection dbConnection = connect();
        dbConnection.setAutoCommit(false);

        // reservationExistsQuery
        PreparedStatement reservationExistsQuery;
        ResultSet reservationExistsResult;
        boolean validResrvation = false;
        int reservationCode = 0;
        String tempInput = "";

        PreparedStatement cancelReservationQuery;
        int updateRowsCount = 0;

        // Get Reservation Number User Input
        System.out.println("Request Reservation Cancelation");
        System.out.println("Enter your reservation code:");
        if (scanner.hasNextInt()) {
            reservationCode = scanner.nextInt();
        }
        if (reservationCode == 0) {
            System.out.println("[Error] Invalid Entry");
            scanner.close();
            return;
        }

        // Prepare reservation existance query
        reservationExistsQuery = dbConnection
                .prepareStatement("select true as valid from lab7_reservations where Code = ?");
        reservationExistsQuery.setInt(1, reservationCode);

        // Perform reservation existance query
        try {
            reservationExistsResult = reservationExistsQuery.executeQuery();
            while (reservationExistsResult.next()) {
                validResrvation = reservationExistsResult.getBoolean("valid");
            }
        } catch (SQLException e) {
            dbConnection.rollback();
            dbConnection.close();
            scanner.close();
            e.printStackTrace();
        }

        // If Valid check dates
        if (validResrvation) {
            System.out.println("Reservation Found. Are you sure you would like to cancel reservation: "
                    + String.valueOf(reservationCode) + "?");
            System.out.println("Enter \"yes\" or \"y\" to confirm cancelation.");

            // Allow Spaces
            scanner.useDelimiter("\n");

            // Get updated info from user
            tempInput = scanner.next();
            if (tempInput.toUpperCase().equals("YES") || tempInput.toUpperCase().equals("Y")) {

                cancelReservationQuery = dbConnection.prepareStatement("delete from lab7_reservations where code = ?");
                cancelReservationQuery.setInt(1, reservationCode);

                try {
                    updateRowsCount = cancelReservationQuery.executeUpdate();
                    System.out.println(String.valueOf(updateRowsCount) + " reservation(s) canceled: " + String.valueOf(reservationCode));
                    
                    // change to commit when testing is done
                    dbConnection.rollback();
                } catch (SQLException e) {
                    dbConnection.rollback();
                    dbConnection.close();
                    scanner.close();
                    e.printStackTrace();
                }
            }
            else
            {
                System.out.println("The reservation was not canceled.");
                dbConnection.rollback();
            }

        } else {
            System.out.println("Reservation not found.");
            dbConnection.rollback();
        }
        dbConnection.close();
        scanner.close();
    }

    // Detailed Reservation Information
    public void funcReq5() throws SQLException, IllegalArgumentException {
        String tempInput = "";
        
        // Step 1: Establish connection to RDBMS
        try (Connection dbConnection = DriverManager.getConnection(url, name, pass)) {

            // Step 2: Construct SQL statement
            Scanner scanner = new Scanner(System.in);
            System.out.print("Detailed Reservation Information \n");

            System.out.print("Enter a first name: ");
            String fname = "%";
            String tempString = scanner.nextLine();
            if (!tempString.equals("")){
                fname = tempString;
            }

            System.out.print("Enter a last name: ");
            String lname = "%";
            tempString = scanner.nextLine();
            if (!tempString.equals("")){
                lname = tempString;
            }

            System.out.print("Enter a room code: ");
            String rcode = "%";
            tempString = scanner.nextLine();
            if (!tempString.equals("")){
                rcode = tempString;
            }

            System.out.print("Enter a reservation code: ");
            String rescode = "%";
            tempString = scanner.nextLine();
            if (!tempString.equals("")){
                rescode = tempString;
            }

            System.out.print("Enter a start date (YYYY-MM-DD): ");
            String sdate = "1900-01-01";
            tempString = scanner.nextLine();
            if (!tempString.equals("")) {
                sdate = tempString;
            }

            System.out.print("Enter an end date (YYYY-MM-DD): ");
            String edate = "2100-01-01";
            tempString = scanner.nextLine();
            if (!tempString.equals("")) {
                edate = tempString;
            }
            

            String searchSql = "SELECT * FROM lab7_reservations, lab7_rooms WHERE RoomCode = Room AND FirstName like ? AND LastName like ? AND Room like ? AND Code like ? AND CheckIn <= ? AND ? < CheckOut";

            // Step 3: Start transaction
            dbConnection.setAutoCommit(false);

            try (PreparedStatement pstmt = dbConnection.prepareStatement(searchSql)) {
                    
                // Step 4: Send SQL statement to DBMS
                pstmt.setString(1, fname);
                pstmt.setString(2, lname);
                pstmt.setString(3, rcode);
                pstmt.setString(4, rescode);
                pstmt.setDate(5, java.sql.Date.valueOf(edate));
                pstmt.setDate(6, java.sql.Date.valueOf(sdate));
                ResultSet resInfoResult = pstmt.executeQuery();
                
                
                // Step 5: Handle results
                while (resInfoResult.next()) {
                    String fnameResult = resInfoResult.getString("FirstName");
                    String lnameResult = resInfoResult.getString("LastName");
                    int reservationCodeResult = resInfoResult.getInt("Code");
                    String roomCodeResult = resInfoResult.getString("Room");
                    String CheckIn = resInfoResult.getString("CheckIn");
                    String CheckOut = resInfoResult.getString("CheckOut");
                    int rateResult = resInfoResult.getInt("Rate");
                    int AdultsResult = resInfoResult.getInt("Adults");
                    int kidsResult = resInfoResult.getInt("Kids");
                    String RoomNameResult = resInfoResult.getString("RoomName");
                    
                    

                    System.out.format("%s %s Reservation Code: %d Room Code: %s Check-in: %s Check-out: %s Rate: %d Adults: %d Kids: %d Room Name: %s %n", fnameResult, lnameResult, reservationCodeResult, roomCodeResult, CheckIn, CheckOut, rateResult, AdultsResult, kidsResult, RoomNameResult);
                }


                // Step 6: Commit or rollback transaction
                dbConnection.commit();
            } catch (SQLException e) {
                dbConnection.rollback();
            }
            catch (IllegalArgumentException e){
                System.out.println("ill-formatted input!\n");
            }

        }
        // Step 7: Close connection (handled implcitly by try-with-resources syntax)
    }

    // Revenue
    public void funcReq6() throws SQLException {

        // Step 1: Establish connection to RDBMS
        try (Connection dbConnection = DriverManager.getConnection(url, name, pass)) {

            // Step 2: Construct SQL statement
            String RevenueSql = "with b as ( " +
                                    "with a as( " +
                                        "select room, MONTH(CheckIn) as month, count(*) as reservations, round(sum(DATEDIFF(Checkout, CheckIn) * Rate),0) AS TotalCharge " +
                                        "from lab7_reservations " +
                                        "group by room, month ) " +
                                    "select IFNULL(room, 'totals') AS room, " +
                                    "SUM(CASE WHEN month = 1 THEN TotalCharge END) January, " +
                                    "SUM(CASE WHEN month = 2 THEN TotalCharge END) Febuary, " +
                                    "SUM(CASE WHEN month = 3 THEN TotalCharge END) March, " +
                                    "SUM(CASE WHEN month = 4 THEN TotalCharge END) April, " +
                                    "SUM(CASE WHEN month = 5 THEN TotalCharge END) May, " +
                                    "SUM(CASE WHEN month = 6 THEN TotalCharge END) June, " +
                                    "SUM(CASE WHEN month = 7 THEN TotalCharge END) July, " +
                                    "SUM(CASE WHEN month = 8 THEN TotalCharge END) August, " +
                                    "SUM(CASE WHEN month = 9 THEN TotalCharge END) September, " +
                                    "SUM(CASE WHEN month = 10 THEN TotalCharge END) October, " +
                                    "SUM(CASE WHEN month = 11 THEN TotalCharge END) November, " +
                                    "SUM(CASE WHEN month = 12 THEN TotalCharge END) December " +
                                    "from a " +
                                    "group by room WITH ROLLUP) " +
                                "select *, January + Febuary + March + April + May + June + July + August + September + October + November + December as roomtot " +
                                "from b";

            // Step 3: Start transaction
            dbConnection.setAutoCommit(false);
            
            try (PreparedStatement pstmt = dbConnection.prepareStatement(RevenueSql)) {
                
                // Step 4: Send SQL statement to DBMS
                ResultSet resInfoResult = pstmt.executeQuery();

                // Step 4.5 formatting outut as table
                String roomCol = "Room";
                String JanuaryCol = "January";
                String febCol = "Febuary";
                String marchcol = "March";
                String aprilCol = "April";
                String mayCol = "May";
                String juneCol = "June";
                String julyCol = "July";
                String augcol = "August";
                String septcol = "September";
                String octCol = "October";
                String novCol = "November";
                String decCol = "December";
                String roomtotcol = "Room total";
                System.out.format("%-6s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %n", roomCol, JanuaryCol, febCol, marchcol, aprilCol, mayCol, juneCol, julyCol, augcol, septcol, octCol, novCol, decCol, roomtotcol);

                
                // Step 5: Handle results
                while (resInfoResult.next()) {
                    String room = resInfoResult.getString("room");
                    String jan = resInfoResult.getString("January");
                    String feb = resInfoResult.getString("Febuary");
                    String mar = resInfoResult.getString("March");
                    String apr = resInfoResult.getString("April");
                    String may = resInfoResult.getString("May");
                    String jun = resInfoResult.getString("June");
                    String july = resInfoResult.getString("July");
                    String aug = resInfoResult.getString("August");
                    String sept = resInfoResult.getString("September");
                    String oct = resInfoResult.getString("October");
                    String nov = resInfoResult.getString("November");
                    String dec = resInfoResult.getString("December");
                    String tot = resInfoResult.getString("roomtot");

                    System.out.format("%-6s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %n", room, jan, feb, mar, apr, may, jun, july, aug, sept, oct, nov, dec, tot);
                }

                // Step 6: Commit or rollback transaction
                dbConnection.commit();
            } catch (SQLException e) {
                dbConnection.rollback();
            }


        }
        // Step 7: Close connection (handled implcitly by try-with-resources syntax)
    }

    

    public Connection connect() throws SQLException {
        try {
            Connection dbConnection = DriverManager.getConnection(url, name, pass);
            return dbConnection;
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
        }
    }
}
