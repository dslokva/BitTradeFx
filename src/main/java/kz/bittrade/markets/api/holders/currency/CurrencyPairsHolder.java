package kz.bittrade.markets.api.holders.currency;

import kz.bittrade.com.BFConstants;
import kz.bittrade.markets.api.holders.currency.pairs.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class CurrencyPairsHolder {
    private String name;
    private String displayName;
    private HashMap<String, CommonCurrencyPair> currencyPairs;

    private String minPricePairMarketId = "0";
    private String maxPricePairMarketId = "0";

    private double minDoublePrice;
    private double maxDoublePrice;

    public CurrencyPairsHolder() {
        BitfinexCurrencyPair bitfinexPair = new BitfinexCurrencyPair();
        WexNzCurrencyPair wexnzPair = new WexNzCurrencyPair();
        KrakenCurrencyPair krakenPair = new KrakenCurrencyPair();
        CexCurrencyPair cexPair = new CexCurrencyPair();

        bitfinexPair.setMarketId(BFConstants.BITFINEX_ID);
        wexnzPair.setMarketId(BFConstants.WEX_ID);
        krakenPair.setMarketId(BFConstants.KRAKEN_ID);
        cexPair.setMarketId(BFConstants.CEX_ID);

        currencyPairs = new HashMap<>();
        currencyPairs.put(wexnzPair.getMarketId(), wexnzPair);
        currencyPairs.put(bitfinexPair.getMarketId(), bitfinexPair);
        currencyPairs.put(krakenPair.getMarketId(), krakenPair);
        currencyPairs.put(cexPair.getMarketId(), cexPair);
    }

    public void defineMinMaxPrice(HashMap<String, Boolean> enabledMap) {
        HashMap<String, Double> map = new HashMap<>();
        Boolean wexEnabled = enabledMap.get(BFConstants.WEX);
        Boolean bitEnabled = enabledMap.get(BFConstants.BITFINEX);
        Boolean kraEnabled = enabledMap.get(BFConstants.KRAKEN);
        Boolean cexEnabled = enabledMap.get(BFConstants.CEX);
        String id = "0"; //temp id if only one market selected

        if (wexEnabled != null && wexEnabled.equals(Boolean.TRUE)) {
            map.put(BFConstants.WEX_ID, getWexnzPair().getLastPriceDouble());
            id = BFConstants.WEX_ID;
        }
        if (bitEnabled != null && bitEnabled.equals(Boolean.TRUE)) {
            map.put(BFConstants.BITFINEX_ID, getBitfinexPair().getLastPriceDouble());
            id = BFConstants.BITFINEX_ID;
        }
        if (kraEnabled != null && kraEnabled.equals(Boolean.TRUE)) {
            map.put(BFConstants.KRAKEN_ID, getKrakenPair().getLastPriceDouble());
            id = BFConstants.KRAKEN_ID;
        }
        if (cexEnabled != null && cexEnabled.equals(Boolean.TRUE)) {
            map.put(BFConstants.CEX_ID, getCexPair().getLastPriceDouble());
            id = BFConstants.CEX_ID;
        }

        int marketsCount = Collections.frequency(enabledMap.values(), Boolean.TRUE);

        if (marketsCount > 1) {
            maxPricePairMarketId = findMaxValueFromMap(map);
            minPricePairMarketId = findMinValueFromMap(map);
        }

        if (marketsCount == 1) {
            maxPricePairMarketId = id;
            minPricePairMarketId = id;
        }

        if (marketsCount == 0) {
            minDoublePrice = 0.0;
            maxDoublePrice = 0.0;
        } else {
            minDoublePrice = currencyPairs.get(minPricePairMarketId).getLastPriceDouble();
            maxDoublePrice = currencyPairs.get(maxPricePairMarketId).getLastPriceDouble();
        }
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
        return (BitfinexCurrencyPair) currencyPairs.get(BFConstants.BITFINEX_ID);
    }

    public void setBitfinexPair(BitfinexCurrencyPair bitfinexPair) {
        currencyPairs.put(BFConstants.BITFINEX_ID, bitfinexPair);
    }

    public WexNzCurrencyPair getWexnzPair() {
        return (WexNzCurrencyPair) currencyPairs.get(BFConstants.WEX_ID);
    }

    public void setWexnzPair(WexNzCurrencyPair wexnzPair) {
        currencyPairs.put(BFConstants.WEX_ID, wexnzPair);
    }

    public KrakenCurrencyPair getKrakenPair() {
        return (KrakenCurrencyPair) currencyPairs.get(BFConstants.KRAKEN_ID);
    }

    public void setKrakenPair(KrakenCurrencyPair krakenPair) {
        currencyPairs.put(BFConstants.KRAKEN_ID, krakenPair);
    }

    public CexCurrencyPair getCexPair() {
        return (CexCurrencyPair) currencyPairs.get(BFConstants.CEX_ID);
    }

    public void setCexPair(CexCurrencyPair cexPair) {
        currencyPairs.put(BFConstants.CEX_ID, cexPair);
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

    public String getLastPriceCex() {
        return colorizeStringPrice(getCexPair());
    }

    private String colorizeStringPrice(CommonCurrencyPair pair) {
        String lastPrice = pair.getLastPrice();
        if (maxPricePairMarketId.equals(minPricePairMarketId))
            return "<b><font color='gray'>" + lastPrice + "</font></b>";

        if (pair.getMarketId().equals(maxPricePairMarketId))
            return "<b><font color='red'>" + lastPrice + "</font></b>";
        else if (pair.getMarketId().equals(minPricePairMarketId))
            return "<b><font color='green'>" + lastPrice + "</font></b>";
        else return "<b><font color='gray'>" + lastPrice + "</font></b>";
    }

    public String getDeltaString() {
        String result = "0.00";
        if (!isLonelyMarket()) {
            result = "<b>" + String.format("%.3f", maxDoublePrice - minDoublePrice) + "</b>";
        }
        return result;
    }

    public double getDeltaDouble() {
        return maxDoublePrice - minDoublePrice;
    }

    public double getDeltaDoublePercent() {
        if (maxDoublePrice > 0)
            return 100.0 - (minDoublePrice * 100) / maxDoublePrice;
        else return 0.0;
    }

    public String getDeltaStringPercent() {
        if (!isLonelyMarket()) {
            double deltaPercent = 100.0 - (minDoublePrice * 100) / maxDoublePrice;
            String color = "red";
            if (deltaPercent > 2 && deltaPercent < 5) color = "orange";
            if (deltaPercent >= 5) color = "green";

            return "<b><font color='" + color + "'>" + String.format("%.2f", deltaPercent) + "% </font></b>";
        } else return "<b><font color='green'>0.0% </font></b>";
    }

    private boolean isLonelyMarket() {
        return minPricePairMarketId.equals(maxPricePairMarketId);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
