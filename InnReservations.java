import java.sql.*;
import java.util.*;

public class InnReservations {
    static String url;
    static String name;
    static String pass;
    static final String rooms = "blee96.lab7_rooms r";
    static final String reservations = "blee96.lab7_reservations res";

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
        funcReq1();
        System.exit(0);
        // int choice = -1;
        // String statement = "";
        // Scanner scanner = new Scanner(System.in);
        // System.out.println("Press 1 to view Rooms and Rates");
        // System.out.println("Press 2 to make a Reservation");
        // System.out.println("Press 3 to edit an existing Reservation");
        // System.out.println("Press 4 to cancel a Reservation");
        // System.out.println("Press 5 to see detailed reservation information");
        // System.out.println("Press 6 to see an Overview of Revenue");
        // System.out.println("Press 0 to quit");
        // System.out.println("How can we help. Please 1 a number 1-6 or 0 to quit):
        // ");
        // while (scanner.hasNext()) {
        // if (scanner.hasNextInt() && ((choice = scanner.nextInt()) >= 0) && choice <=
        // 6) {
        // if (choice == 1) {
        // } else if (choice == 2) {
        // } else if (choice == 3) {
        // } else if (choice == 4) {
        // } else if (choice == 5) {
        // } else if (choice == 6) {
        // } else if (choice == 0) {
        // System.out.println("Thank you! See you next time!");
        // System.exit(1);
        // }
        // } else {
        //
        // }
        // System.out.println("Please enter a valid command: 1-6 or 0 to quit!");
        // scanner.next();
        // }
        // choice = scanner.nextInt();
    }

    public void funcReq1() throws SQLException {
        String sql = " WITH occ180 AS" + " (SELECT Room,"
                + "ROUND(SUM(DATEDIFF(CheckOut,GREATEST(CheckIn,DATE_ADD(CURRENT_DATE(),INTERVAL -180 DAY))))/180,2) Popularity"
                + " FROM " + reservations + " WHERE CheckOut > DATE_ADD(CURRENT_DATE(), INTERVAL -180 DAY)"
                + " AND CheckIn < NOW()" + " GROUP BY Room)," + " nextAvail AS" + "(with r1 as "
                + " (select RoomName, CheckIn, CheckOut" + " from " + rooms + ", " + reservations
                + " where r.RoomCode = res.Room)," + " r2 as (select fir.RoomName as room,"
                + " DATEDIFF(sec.CheckIn, fir.CheckOut) as diff," + " fir.CheckOut as checkout, sec.CheckIn as checkin,"
                + " rank() over (partition by fir.RoomName order by checkout,"
                + " DATEDIFF(sec.CheckIn, fir.CheckOut) asc) as RANKING"
                + " FROM r1 fir JOIN r1 sec ON fir.RoomName=sec.RoomName"
                + " WHERE fir.Checkout < sec.CheckIn AND fir.CheckOut > NOW())" + " SELECT Room, CheckOut NextAvailable"
                +
                // " select room, DATE_ADD(checkout, INTERVAL 1 DAY) as Next_Available"
                " FROM r2 WHERE RANKING = 1 ORDER BY room)," + " recentRes AS"
                + " (SELECT Room,DATEDIFF(MAX(CheckOut),MAX(CheckIn)) lastLength, MAX(CheckOut) lastCheckOut" + " FROM "
                + reservations + " WHERE CheckOut <= NOW()" + " GROUP BY Room)"
                + " SELECT r.*, Popularity, NextAvailable, lastLength lastStayLength, lastCheckOut"
                + " FROM occ180 occ JOIN " + rooms + " ON RoomCode=occ.Room"
                + " JOIN recentRes res ON res.Room=r.RoomCode" + " JOIN nextAvail n ON res.Room=n.Room"
                + " ORDER BY Popularity DESC;";
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
            String pop = "Popularity";
            String lastLen = "lastStayLength";
            String lastCheckOut = "lastCheckOut";
            String nextAvail = "NextAvailable";
            System.out.format("\n%-8s | %-24s | %4s | %7s | %6s | %9s | %11s | %10s | %14s | %12s | %13s\n", rc, rn,
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
                System.out.format("%-8s | %-24s | %4s | %7s | %6s | %9s | %11s | %10s | %14s | %12s | %13s\n", Rc, Rn,
                        Beds, BedType, MaxOcc, BasePrice, Decor, popularity, LastLen, LastCheckOut, nextAvail);
            }
        }
        System.out.println("");
    }

    public void funcReq2() throws SQLException {
        // String sql =
        // ;
    }

    public void executeFR2(String sql) throws SQLException {
        Connection dbConnection = connect();
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
