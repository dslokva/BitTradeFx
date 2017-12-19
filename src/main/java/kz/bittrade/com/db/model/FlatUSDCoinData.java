package kz.bittrade.com.db.model;

import java.sql.Timestamp;

public class FlatUSDCoinData {
    private Integer coinid;
    private Integer marketid;
    private Timestamp timestamp;
    private Double rate;

    public FlatUSDCoinData(Integer coinid, Integer marketid, Timestamp timestamp, Double rate) {
        this.coinid = coinid;
        this.marketid = marketid;
        this.timestamp = timestamp;
        this.rate = rate;
    }

    public Integer getCoinid() {
        return coinid;
    }

    public void setCoinid(Integer coinid) {
        this.coinid = coinid;
    }

    public Integer getMarketid() {
        return marketid;
    }

    public void setMarketid(Integer marketid) {
        this.marketid = marketid;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
