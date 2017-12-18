package kz.bittrade.markets.api.holders.currency.pairs;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WexNzCurrencyPairInfo {
    private double high;
    private double low;
    private double avg;
    private double vol;
    private double vol_cur;
    private double last;
    private double buy;
    private double sell;
    private long updated;

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getAvg() {
        return avg;
    }

    public double getVol() {
        return vol;
    }

    public double getVol_cur() {
        return vol_cur;
    }

    public double getLast() {
        return last;
    }

    public double getBuy() {
        return buy;
    }

    public double getSell() {
        return sell;
    }

    public double getTimestampNative() {
        return updated;
    }

    public String getTimestamp() {
        Date date = new Date(updated * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        return sdf.format(date);
    }

    public Timestamp getTimestampDate() {
        return new Timestamp((long) (updated * 1000L));
    }


    /*
            "high":109.88,
            "low":91.14,
            "avg":100.51,
            "vol":1632898.2249,
            "vol_cur":16541.51969,
            "last":101.773,
            "buy":101.9,
            "sell":101.773,
            "updated":1370816308
    */
}
