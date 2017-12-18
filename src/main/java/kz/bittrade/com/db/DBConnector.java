package kz.bittrade.com.db;


import java.sql.*;


public class DBConnector {

    public DBConnector() {
        //  connect();
    }

    /**
     * Connect to the BitTradeFxDB database
     *
     * @return the Connection object
     */
    private Connection connect() {
//        String url = "java:comp/env/jdbc/bittradefx"; //for external file place + commented lines below
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
//            Context ctx = new InitialContext();
//            DataSource ds = (DataSource)ctx.lookup(url);
//            conn = ds.getConnection();
            conn = DriverManager.getConnection("jdbc:sqlite::resource:BitTradeFxDB.sqlite");

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Insert a new row into the warehouses table
     *
     * @param coinid
     * @param marketid
     * @param timestamp
     * @param rate
     */
    public void insert(Integer coinid, Integer marketid, Timestamp timestamp, double rate) {
        String sql = "INSERT INTO main.flat_usdcoins_data(coinid, marketid, timestamp, rate) VALUES(?,?,?,?)";

        try {
            Connection conn = this.connect();
            if (conn != null) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, coinid);
                pstmt.setInt(2, marketid);
                pstmt.setTimestamp(3, timestamp);
                pstmt.setDouble(4, rate);
                pstmt.executeUpdate();
                conn.close();
            } else {
                System.out.println("[SQLiteDB] Connection failed.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void selectAll() {
        String sql = "SELECT coinid, marketid, timestamp, rate FROM main.flat_usdcoins_data";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("coinid") + "\t" +
                        rs.getInt("marketid") + "\t" +
                        rs.getTimestamp("timestamp") + "\t" +
                        rs.getDouble("rate"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

