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
        funcReq3();
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

    public void executeFR2(String sql) throws SQLException{
        Connection dbConnection = connect();
    }

    public void funcReq3() throws SQLException 
    {
        // Setup
        Scanner scanner = new Scanner(System.in);
        Connection dbConnection = connect();
        
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

        // Get Reservation Number User Input
        System.out.println("Request Reservation Change");
        System.out.println("Enter your reservation code:");
        if (scanner.hasNextInt()) 
        {
            reservationCode = scanner.nextInt();
        }
        if (reservationCode == 0)
        {
            System.out.println("[Error] Invalid Entry");
            return;
        }

        // Prepare reservation existance query
        reservationExistsQuery = dbConnection.prepareStatement("select true as valid, Room, CheckIn, CheckOut, LastName, FirstName, Adults, Kids from lab7_reservations where Code = ?");
        reservationExistsQuery.setInt(1, reservationCode);

        // Perform reservation existance query
        try
        {
            reservationExistsResult = reservationExistsQuery.executeQuery();
            while (reservationExistsResult.next())
            {
                validResrvation = reservationExistsResult.getBoolean("valid");
                roomCode = reservationExistsResult.getString("Room");
                checkIn = reservationExistsResult.getDate("CheckIn");
                checkOut = reservationExistsResult.getDate("CheckOut");
                lastName = reservationExistsResult.getString("LastName");
                firstName = reservationExistsResult.getString("FirstName");
                numAdults = reservationExistsResult.getInt("Adults");
                numKids = reservationExistsResult.getInt("Kids");
                
            }
        } catch (SQLException e)
        {
            // TearDown
            scanner.close();
            throw new SQLException(e);
        }

        // If Valid check dates
        if (validResrvation)
        {
            // Allow Spaces
            scanner.useDelimiter("\n");
            
            // Get updated info from user
            System.out.println("Enter 'no change' or updated first name:");
            tempInput = scanner.next();
            if (!tempInput.equals("no change"))
            {
                firstName = tempInput;
            }

            // Get updated info from user
            System.out.println("Enter 'no change' or updated last name:");
            tempInput = scanner.next();
            if (!tempInput.equals("no change"))
            {
                lastName = tempInput;
            }

            // Get updated info from user
            System.out.println("Enter 'no change' or updated check-in date:");
            tempInput = scanner.next();
            if (!tempInput.equals("no change"))
            {
                checkIn = java.sql.Date.valueOf(tempInput);
            }

            // Get updated info from user
            System.out.println("Enter 'no change' or updated check-out date:");
            tempInput = scanner.next();
            if (!tempInput.equals("no change"))
            {
                checkOut = java.sql.Date.valueOf(tempInput);
            }

            // Get updated info from user
            System.out.println("Enter 'no change' or updated number of children:");
            tempInput = scanner.next();
            if (!tempInput.equals("no change"))
            {
                numKids = Integer.parseInt(tempInput);
            }

            // Get updated info from user
            System.out.println("Enter 'no change' or updated number of adults");
            tempInput = scanner.next();
            if (!tempInput.equals("no change"))
            {
                numAdults = Integer.parseInt(tempInput);
            }

            ovelapReservationQuery = dbConnection.prepareStatement(
                "select true as overlap" + 
                "from lab7_reservations" + 
                "where Room like ? " +
                "and ((CheckIn between ? and ?) or (CheckOut between ? and ?))"
            );
            ovelapReservationQuery.setString(1, roomCode);
            ovelapReservationQuery.setDate(2, checkIn);
            ovelapReservationQuery.setDate(3, checkOut);
            ovelapReservationQuery.setDate(4, checkIn);
            ovelapReservationQuery.setDate(5, checkOut);

            // Perform reservation existance query
            try
            {
                ovelapReservationResults = ovelapReservationQuery.executeQuery();
                while (ovelapReservationResults.next())
                {
                    ovelapReservation = ovelapReservationResults.getBoolean("overlap");
                }
            } catch (SQLException e)
            {
                // TearDown
                scanner.close();
                throw new SQLException(e);
            }

            if(ovelapReservation)
            {
                System.out.println("Overlap Detected");
            }
            
        } 
        else
        {
            System.out.println("Reservation Code Not Valid, Update Failed...");
        }



        

        

        // TearDown
        scanner.close();
    }

    public void executeFR3() throws SQLException {

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
