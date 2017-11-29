package kz.bittrade.markets.api.holders.currency.pairs;

import com.google.gson.annotations.SerializedName;

public class KrakenCurrencyPairInfo {
    @SerializedName(value = "XETHZUSD", alternate = {"XXBTZUSD", "XLTCZUSD", "XZECZUSD", "DASHUSD", "BCHUSD"})
    private KrakenCurrencyTicker ticker;

    public double getLastPrice() {
        return ticker != null ? ticker.getLast() : 0.0;
    }
}
