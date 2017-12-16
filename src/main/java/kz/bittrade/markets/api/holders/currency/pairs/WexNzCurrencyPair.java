package kz.bittrade.markets.api.holders.currency.pairs;


import com.google.gson.annotations.SerializedName;

public class WexNzCurrencyPair extends CommonCurrencyPair {
    @SerializedName(value = "btc_usd", alternate = {"eth_usd", "zec_usd", "dsh_usd", "ltc_usd", "bch_usd"})
    private WexNzCurrencyPairInfo info;

    public WexNzCurrencyPair() {
        info = new WexNzCurrencyPairInfo();
    }

    public WexNzCurrencyPairInfo getInfo() {
        return info;
    }

    public  String getLastPrice() {
        if (info != null) {
            if (getTickerName().equals("---")) return "n/a";
            else return String.format("%.2f", info.getLast());
        }
        else return "0.00";
    }

    public double getLastPriceDouble() {
        if (info != null) return info.getLast();
        else return 0.0;
    }

    public String getTimestamp() {
        if (info != null) return info.getTimestamp();
        else return "---";
    }
}
