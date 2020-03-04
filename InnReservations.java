import java.sql.*;
import java.util.*;

public class InnReservations {
    static String url;
    static String name;
    static String pass;
    static final String rooms = "blee96.lab7_rooms";
    static final String reservations = "blee96.lab7_reservations";

    public static void main(String[] args) throws SQLException {
        try {
            InnReservations IR = new InnReservations();
            url = "jdbc:mysql://db.labthreesixfive.com/blee96?autoReconnect=true&useSSL=false";
            name = "blee96";
            pass = "WinterTwenty20_365_011115373";
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
//        int choice = -1;
//        String statement = "";
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Press 1 to view Rooms and Rates");
//        System.out.println("Press 2 to make a Reservation");
//        System.out.println("Press 3 to edit an existing Reservation");
//        System.out.println("Press 4 to cancel a Reservation");
//        System.out.println("Press 5 to see detailed reservation information");
//        System.out.println("Press 6 to see an Overview of Revenue");
//        System.out.println("Press 0 to quit");
//        System.out.println("How can we help. Please enter a number 1-6 or 0 to quit): ");
//        while (scanner.hasNext()) {
//            if (scanner.hasNextInt() && ((choice = scanner.nextInt()) >= 0) && choice <= 6) {
//                if (choice == 1) {
//                } else if (choice == 2) {
//                } else if (choice == 3) {
//                } else if (choice == 4) {
//                } else if (choice == 5) {
//                } else if (choice == 6) {
//                } else if (choice == 0) {
//                    System.out.println("Thank you! See you next time!");
//                    System.exit(1);
//                }
//            } else {
//                
//            }
//            System.out.println("Please enter a valid command: 1-6 or 0 to quit!");
//            scanner.next();
//        }
//        choice = scanner.nextInt();
    }

    public void funcReq1() throws SQLException {
        String sql =
            " WITH occ180 AS" +
                " (SELECT Room," +
                    "ROUND(SUM(DATEDIFF(CheckOut,GREATEST(CheckIn,DATE_ADD(NOW(),INTERVAL -180 DAY))))/180,2) Popularity" +
                " FROM " + reservations + 
                " WHERE CheckOut > DATE_ADD(NOW(), INTERVAL -180 DAY)" +
                " GROUP BY Room)," +
            " recentRes AS" +
                " (SELECT Room,DATEDIFF(MAX(CheckOut),MAX(CheckIn)) lastLength, MAX(CheckOut) lastCheckOut" +
                " FROM " + reservations +
                " WHERE CheckOut <= NOW()" +
                " GROUP BY Room)" +
            " SELECT r.*, Popularity, lastLength, lastCheckOut" +
            " FROM occ180 occ JOIN " + rooms + " r ON RoomCode=occ.Room" +
                " JOIN recentRes res ON res.Room=r.RoomCode" +
            " ORDER BY Popularity DESC;";
        try {
            connectFR1(sql);
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public void connectFR1(String sql) throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(url, name, pass);
            try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
                String rc = "RoomCode";
                String rn = "RoomName";
                String beds = "Beds";
                String bedType = "bedType";
                String maxOcc = "maxOcc";
                String basePrice = "basePrice";
                String decor = "decor";
                String pop = "Popularity";
                String lastLen = "lastLength";
                String lastCheckOut = "lastCheckOut";
                System.out.format("\n%-8s | %-24s | %4s | %7s | %6s | %9s | %11s | %-10s | %10s | %12s\n",
                    rc,rn,beds,bedType,maxOcc,basePrice,decor,pop,lastLen,lastCheckOut);
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
                    System.out.format("%-8s | %-24s | %4s | %7s | %6s | %9s | %11s | %-10s | %10s | %12s\n",
                        Rc,Rn,Beds,BedType,MaxOcc,BasePrice,Decor,popularity,LastLen,LastCheckOut);
                }
            }
            System.out.println("");
        } finally {}
    }
}
