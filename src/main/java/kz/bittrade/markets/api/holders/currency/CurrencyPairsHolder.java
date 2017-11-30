package kz.bittrade.markets.api.holders.currency;

import kz.bittrade.com.BFConstants;
import kz.bittrade.markets.api.holders.currency.pairs.BitfinexCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.CommonCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.KrakenCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.WexNzCurrencyPair;

import java.util.HashMap;
import java.util.Iterator;

public class CurrencyPairsHolder {
    private String name;
    private HashMap<String, CommonCurrencyPair> currencyPairs;

    private String minPricePairMarketId;
    private String maxPricePairMarketId;

    private double minDoublePrice;
    private double maxDoublePrice;

    public CurrencyPairsHolder() {
        BitfinexCurrencyPair bitfinexPair = new BitfinexCurrencyPair();
        WexNzCurrencyPair wexnzPair = new WexNzCurrencyPair();
        KrakenCurrencyPair krakenPair = new KrakenCurrencyPair();

        bitfinexPair.setMarketId(BFConstants.BITFINEX);
        wexnzPair.setMarketId(BFConstants.WEX);
        krakenPair.setMarketId(BFConstants.KRAKEN);

        currencyPairs = new HashMap<>();
        currencyPairs.put(wexnzPair.getMarketId(), wexnzPair);
        currencyPairs.put(bitfinexPair.getMarketId(), bitfinexPair);
        currencyPairs.put(krakenPair.getMarketId(), krakenPair);
    }

    public void defineMinMaxPrice() {
        HashMap<String, Double> map = new HashMap<>();
        map.put(BFConstants.WEX, getWexnzPair().getLastPriceDouble());
        map.put(BFConstants.BITFINEX, getBitfinexPair().getLastPriceDouble());
        map.put(BFConstants.KRAKEN, getKrakenPair().getLastPriceDouble());

        //todo: Replace loops with analyse price value during setter call. А то шляпа пиздец, Маргулан ругается.
        maxPricePairMarketId = findMaxValueFromMap(map);
        minPricePairMarketId = findMinValueFromMap(map);

        minDoublePrice = currencyPairs.get(minPricePairMarketId).getLastPriceDouble();
        maxDoublePrice = currencyPairs.get(maxPricePairMarketId).getLastPriceDouble();
    }

    private String findMaxValueFromMap(HashMap<String, Double> map) {
        Iterator i = map.keySet().iterator();
        String lastKey = (String) i.next();
        Double candidate = map.get(lastKey);
        String next;

        while (i.hasNext()) {
            next = (String) i.next();
            Double nextValue = map.get(next);
            if (nextValue.compareTo(candidate) > 0 && nextValue > 0 && candidate >= 0) {
                candidate = nextValue;
                lastKey = next;
            }
        }
        return lastKey;
    }

    private String findMinValueFromMap(HashMap<String, Double> map) {
        Iterator i = map.keySet().iterator();
        String lastKey = (String) i.next();
        Double candidate = map.get(lastKey);
        String next;

        while (i.hasNext()) {
            next = (String) i.next();
            Double nextValue = map.get(next);
            if (nextValue.compareTo(candidate) < 0 && nextValue > 0 || candidate == 0) {
                candidate = nextValue;
                lastKey = next;
            }
        }
        return lastKey;
    }

    public BitfinexCurrencyPair getBitfinexPair() {
        return (BitfinexCurrencyPair) currencyPairs.get(BFConstants.BITFINEX);
    }

    public void setBitfinexPair(BitfinexCurrencyPair bitfinexPair) {
        currencyPairs.put(BFConstants.BITFINEX, bitfinexPair);
    }

    public WexNzCurrencyPair getWexnzPair() {
        return (WexNzCurrencyPair) currencyPairs.get(BFConstants.WEX);
    }

    public void setWexnzPair(WexNzCurrencyPair wexnzPair) {
        currencyPairs.put(BFConstants.WEX, wexnzPair);
    }

    public KrakenCurrencyPair getKrakenPair() {
        return (KrakenCurrencyPair) currencyPairs.get(BFConstants.KRAKEN);
    }

    public void setKrakenPair(KrakenCurrencyPair krakenPair) {
        currencyPairs.put(BFConstants.KRAKEN, krakenPair);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastPriceWex() {
        return colorizeStringPrice(getWexnzPair());
    }

    public String getLastPriceBitfinex() {
        return colorizeStringPrice(getBitfinexPair());
    }

    public String getLastPriceKraken() {
        return colorizeStringPrice(getKrakenPair());
    }

    private String colorizeStringPrice(CommonCurrencyPair pair) {
        String lastPrice = pair.getLastPrice();

        if (pair.getMarketId().equals(maxPricePairMarketId))
            return "<b><font color='red'>" + lastPrice + "</font></b>";
        else if (pair.getMarketId().equals(minPricePairMarketId))
            return "<b><font color='green'>" + lastPrice + "</font></b>";
        else return "<b><font color='gray'>" + lastPrice + "</font></b>";
    }

    public String getDeltaString() {
        String result = "?";
        if ((minPricePairMarketId != null) && (maxPricePairMarketId != null)) {
            result = "<b>" + String.format("%.4f", maxDoublePrice - minDoublePrice) + "</b>";
        }
        return result;
    }

    public double getDeltaDoublePercent() {
        return 100.0 - (minDoublePrice * 100) / maxDoublePrice;
    }

    public String getDeltaStringPercent() {
        String result = "?";
        if ((minPricePairMarketId != null) && (maxPricePairMarketId != null)) {
            double deltaPercent = 100.0 - (minDoublePrice * 100) / maxDoublePrice;
            String color = "red";
            if (deltaPercent > 2 && deltaPercent < 3) color = "orange";
            if (deltaPercent >= 3) color = "green";

            result = "<b><font color='" + color + "'>" + String.format("%.2f", deltaPercent) + "% </font></b>";
        }
        return result;
    }

    public String getUpdateDateWex() {
        return getWexnzPair().getTimestamp();
    }

    public String getUpdateDateBit() {
        return getBitfinexPair().getTimestamp();
    }


}
