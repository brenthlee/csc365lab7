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
        System.out.println("test");
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
        // System.out.println("How can we help. Please enter a number 1-6 or 0 to quit):
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
        Connection conn = connect();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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
        Connection conn = connect();
    }

    public void funcReq3() throws SQLException {
        int reservationCode = 0;
        int validResrvation = 0;
        String roomCode = "no change";
        String firstName = "no change";
        String lastName = "no change";
        String beginDate = "no change";
        String endDate = "no change";
        String numberOfCildren = "no change";
        String numberOfAdults = "no change";

        System.out.println("test2");

        Connection conn = connect();
        System.out.println("test3");
        
        Scanner scanner = new Scanner(System.in);
        PreparedStatement reservationExistsQuery;
        ResultSet returnVal;

        // Get Reservation Number
        System.out.println("Request Reservation Change");
        System.out.println("Enter your reservation code:");
        if (scanner.hasNextInt()) {
            reservationCode = scanner.nextInt();
        }
        System.out.println("Enter updated first name:");
        if (scanner.hasNext()) {
            firstName = scanner.next();
        }
        System.out.println("Enter updated last name:");
        if (scanner.hasNext()) {
            lastName = scanner.next();
        }
        System.out.println("Enter updated check-in date:");
        if (scanner.hasNext()) {
            beginDate = scanner.next();
        }
        System.out.println("Enter updated check-out date:");
        if (scanner.hasNext()) {
            endDate = scanner.next();
        }
        System.out.println("Enter updated number of children:");
        if (scanner.hasNext()) {
            numberOfCildren = scanner.next();
        }
        System.out.println("Enter updated number of adults");
        if (scanner.hasNext()) {
            numberOfAdults = scanner.next();
        }

        // Prepare reservation existance query
        reservationExistsQuery = conn.prepareStatement("select 1 as valid, Room from Reservations where Code = ?");
        reservationExistsQuery.setInt(1, reservationCode);

        // Perform reservation existance query
        try {
            returnVal = reservationExistsQuery.executeQuery();
            while (returnVal.next()) {
                validResrvation = returnVal.getInt("valid");
                roomCode = returnVal.getString("Room");
            }
            if (validResrvation == 1) {
                System.out.println("Reservation Code Valid, Updating Reservation...");
                System.out.println(roomCode);
            } else {
                System.out.println("Reservation Code Not Valid, Update Failed...");
            }
        } catch (SQLException e) {
            // TearDown
            scanner.close();
            throw new SQLException(e);
        }

        // If Valid check dates
        

        // TearDown
        scanner.close();
    }

    public void executeFR3() throws SQLException {

    }

    public Connection connect() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(url, name, pass);
            return conn;
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
        }
    }
}
