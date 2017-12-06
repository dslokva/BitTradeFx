package kz.bittrade.markets.api.holders.currency.pairs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BitfinexCurrencyPair extends CommonCurrencyPair {
    private double mid;
    private double bid;
    private double ask;
    private double last_price;
    private double low;
    private double high;
    private double volume;
    private double timestamp;

    public double getMid() {
        return mid;
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }

    public double getVolume() {
        return volume;
    }

    public String getTimestamp() {
        Date date = new Date((long) (timestamp * 1000L));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        return sdf.format(date);
    }

    public double getLastPriceDouble() {
        return last_price;
    }

    public String getLastPrice() {
        return String.format("%.2f", last_price);
    }


  /*
  "mid":"244.755",
  "bid":"244.75",
  "ask":"244.76",
  "last_price":"244.82",
  "low":"244.2",
  "high":"248.19",
  "volume":"7842.11542563",
  "timestamp":"1444253422.348340958"
  */
}
