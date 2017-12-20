package kz.bittrade.com.db;


import kz.bittrade.com.db.model.FlatUSDCoinData;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DBConnector {

    public DBConnector() {
        System.out.println("[!!!!!!!!!!!!!!!!!!!!!!!!]" + System.getProperty("user.home"));
    }

    /**
     * Connect to the BitTradeFxDB database
     *
     * @return the Connection object
     */
    private Connection connect() {
        String url = "java:comp/env/jdbc/bittradefx"; //for external file place + commented lines below
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(url);
            conn = ds.getConnection();
//            conn = DriverManager.getConnection("jdbc:sqlite::resource:BitTradeFxDB.sqlite");

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

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (rate > 0.0) {
                pstmt.setInt(1, coinid);
                pstmt.setInt(2, marketid);
                pstmt.setTimestamp(3, timestamp);
                pstmt.setDouble(4, rate);
                pstmt.executeUpdate();
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void selectBetween(Timestamp minDate, Timestamp maxDate) {
        String sql = "SELECT coinid, marketid, timestamp, rate FROM main.flat_usdcoins_data WHERE timestamp BETWEEN ? AND ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, minDate);
            pstmt.setTimestamp(2, maxDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println(
                        rs.getInt("coinid") + "\t" +
                                rs.getInt("marketid") + "\t" +
                                rs.getTimestamp("timestamp") + "\t" +
                                rs.getDouble("rate"));
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void selectMinMaxDateRange() {
        String sql = "SELECT MAX(timestamp) AS lastDate, MIN(timestamp) AS firstDate FROM main.flat_usdcoins_data";

        try (Connection conn = this.connect()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(
                        rs.getTimestamp("firstDate") + "\t" + rs.getTimestamp("lastDate")
                );
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<FlatUSDCoinData> selectDataCoinMarketId(String marketId, Integer coinId) {
        String sql = "SELECT coinid, marketid, timestamp, rate FROM main.flat_usdcoins_data WHERE marketid = ? AND coinid = ?";
        List<FlatUSDCoinData> coinDataArrayList = new ArrayList<>();

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.valueOf(marketId));
            pstmt.setInt(2, coinId);
            ResultSet rs = pstmt.executeQuery();


            while (rs.next()) {
                coinDataArrayList.add(new FlatUSDCoinData(
                        rs.getInt("coinid"),
                        rs.getInt("marketid"),
                        rs.getTimestamp("timestamp"),
                        rs.getDouble("rate")));
            }

            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return coinDataArrayList;
    }
}