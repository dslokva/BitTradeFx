package kz.bittrade.markets.api.holders.currency.pairs;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KrakenCurrencyPair extends CommonCurrencyPair {
    @SerializedName(value = "result")
    private KrakenCurrencyPairInfo info;
    private List error;

    public KrakenCurrencyPairInfo getInfo() {
        return info;
    }

    public void setInfo(KrakenCurrencyPairInfo info) {
        this.info = info;
    }

    public String getLastPrice() {
        return info != null ? String.valueOf(info.getLastPrice()) : "0.0";
    }

    public double getLastPriceDouble() {
        return info != null ? info.getLastPrice() : 0.0;
    }
}
