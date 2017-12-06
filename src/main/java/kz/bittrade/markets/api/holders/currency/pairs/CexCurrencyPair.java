package kz.bittrade.markets.api.holders.currency.pairs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CexCurrencyPair extends CommonCurrencyPair {

    private long timestamp;
    private double low;
    private double high;
    private double last;
    private double volume;
    private double volume30d;
    private double bid;
    private double ask;

    @Override
    public double getLastPriceDouble() {
        return last;
    }

    @Override
    public String getLastPrice() {
        return String.format("%.2f", last);
    }

    public String getTimestamp() {
        Date date = new Date((timestamp * 1000L));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        return sdf.format(date);
    }

    public void setLast(double last) {
        this.last = last;
    }
//    {
//            "timestamp":"1512530678",
//            "low":"12074.73",
//            "high":"12766.06",
//            "last":"12765.67",
//            "volume":"1398.98404483",
//            "volume30d":"72645.39746886",
//            "bid":12755.01,
//            "ask":12765.52
//    }
}
