package kz.bittrade.markets.api.holders.currency.pairs;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KrakenCurrencyTicker {
//    a = ask array(<price>, <whole lot volume>, <lot volume>),
//    b = bid array(<price>, <whole lot volume>, <lot volume>),
//    c = last trade closed array(<price>, <lot volume>),
//    v = volume array(<today>, <last 24 hours>),
//    p = volume weighted average price array(<today>, <last 24 hours>),
//    t = number of trades array(<today>, <last 24 hours>),
//    l = low array(<today>, <last 24 hours>),
//    h = high array(<today>, <last 24 hours>),
//    o = today's opening price

    private List<Double> a;
    private List<Double> b;
    @SerializedName(value = "c")
    private List<Double> last;
    private List<Double> v;
    private List<Double> p;
    private List<Double> t;
    private List<Double> l;
    private List<Double> h;
    private double o;

    public double getLast() {
        return last.size() > 0 ? last.get(0) : 0.0;
    }

}
