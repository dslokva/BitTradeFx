package kz.bittrade.com.chartminer;

import kz.bittrade.com.BFConstants;
import kz.bittrade.com.db.DBConnector;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class MarketDataMiner extends TimerTask {
    private static List<CurrencyPairsHolder> currencyPairsHolderList;
    private static int refreshCount;
    private static int errorCount;
    private final MarketsRefresher marketsRefresher;
    private final DBConnector dbConnector;

    public MarketDataMiner() {
        currencyPairsHolderList = new ArrayList<>();
        marketsRefresher = MarketsRefresher.getInstance();
        dbConnector = new DBConnector();
        refreshCount = 0;
        errorCount = 0;
        initPairsToRefresh();
    }

    public static int getRefreshCount() {
        return refreshCount;
    }

    public static int getErrorCount() {
        return errorCount;
    }

    @Override
    public void run() {
        final CountDownLatch waitForRefreshers = new CountDownLatch(currencyPairsHolderList.size());
        try {
            for (CurrencyPairsHolder item : currencyPairsHolderList) {
                synchronized (this) {
                    Thread refresh = new Thread(() -> {
                        marketsRefresher.refreshWexNzCurrencyInfo(item);
                        marketsRefresher.refreshBitfinexCurrencyInfo(item);
                        marketsRefresher.refreshKrakenCurrencyInfo(item);
                        marketsRefresher.refreshCexCurrencyInfo(item);
                        waitForRefreshers.countDown();
                    });
                    refresh.start();
                }
            }
            while (waitForRefreshers.getCount() != 0)
                Thread.sleep(50);

            refreshCount++;
        } catch (Exception e) {
            e.printStackTrace();
            errorCount++;
        }
        insertDataToDB();
    }

    private void insertDataToDB() {
        Connection conn = dbConnector.getConnection();
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis()
                    - Calendar.getInstance().getTimeZone().getOffset(System.currentTimeMillis()));

            if (conn == null) {
                System.out.println("[ERROR] [INSERT] No DB connection.");
                return;
            }
            conn.setAutoCommit(false);
            for (CurrencyPairsHolder item : currencyPairsHolderList) {
                Integer coinid = item.getPairId();

                Integer marketid = Integer.parseInt(item.getBitfinexPair().getMarketId());
//                timestamp = item.getBitfinexPair().getTimestampDate();
                Double rate = item.getBitfinexPair().getLastPriceDouble();
                dbConnector.insert(conn, coinid, marketid, timestamp, rate);

                marketid = Integer.parseInt(item.getCexPair().getMarketId());
//                timestamp = item.getCexPair().getTimestampDate();
                rate = item.getCexPair().getLastPriceDouble();
                dbConnector.insert(conn, coinid, marketid, timestamp, rate);

                marketid = Integer.parseInt(item.getWexnzPair().getMarketId());
//                timestamp = item.getWexnzPair().getTimestampDate();
                rate = item.getWexnzPair().getLastPriceDouble();
                dbConnector.insert(conn, coinid, marketid, timestamp, rate);

                marketid = Integer.parseInt(item.getKrakenPair().getMarketId());
//                timestamp = new Timestamp(System.currentTimeMillis());
                rate = item.getKrakenPair().getLastPriceDouble();
                dbConnector.insert(conn, coinid, marketid, timestamp, rate);
            }
            conn.commit();
            conn.close();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void initPairsToRefresh() {
        currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.BITCOIN));
        currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.BITCOIN_CASH));
        currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.LITECOIN));
        currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.ETHERIUM_COIN));
        currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.ZCASH_COIN));
        currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.DASH_COIN));
        currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.RIPPLE_COIN));
    }

}