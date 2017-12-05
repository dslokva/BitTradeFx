package kz.bittrade.markets.api.holders.currency.pairs;

public abstract class CommonCurrencyPair {
    private String tickerName;
    private String marketId;

    public String getTickerName() {
        return tickerName;
    }

    public void setTickerName(String tickerName) {
        this.tickerName = tickerName;
    }

    public abstract double getLastPriceDouble();

    public abstract String getLastPrice();

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }
}
