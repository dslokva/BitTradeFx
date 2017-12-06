package kz.bittrade.markets.api.holders.user;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WexNzUserInfo {
    private WexNzUserFunds funds;
    private WexNzKeyRights rights;
    private int transaction_count;
    private int open_orders;
    private long server_time;

    public WexNzUserFunds getFunds() {
        return funds;
    }

    public int getOpen_orders() {
        return open_orders;
    }

    public int getTransaction_count() {
        return transaction_count;
    }

    public String getTimestamp() {
        Date date = new Date((server_time * 1000L));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        return sdf.format(date);
    }
}
