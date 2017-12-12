package kz.bittrade.com;

import java.util.HashMap;

public final class KztRateCurrencyHolder {
    private static KztRateCurrencyHolder instance;
    private HashMap<String, KztRateCurrency> kztRatesMap;

    public KztRateCurrencyHolder() {
        kztRatesMap = new HashMap<>();
    }

    public static synchronized KztRateCurrencyHolder getInstance() {
        if (instance == null) {
            instance = new KztRateCurrencyHolder();
        }
        return instance;
    }

    public void addKztCurrencyRate(KztRateCurrency rate) {
        kztRatesMap.put(rate.getCurrencyName(), rate);
    }

    public HashMap<String, KztRateCurrency> getKztRatesMap() {
        return kztRatesMap;
    }

}