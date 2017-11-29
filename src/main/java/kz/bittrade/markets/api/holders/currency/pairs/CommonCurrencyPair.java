package kz.bittrade.markets.api.holders.currency.pairs;

public abstract class CommonCurrencyPair {
    private String tickerName;
    private String marketId;
    private boolean lastPriceError;

    public String getTickerName() {
        return tickerName;
    }

    public void setTickerName(String tickerName) {
        this.tickerName = tickerName;
    }

    public boolean isLastPriceError() {
        return lastPriceError;
    }

    public void setLastPriceError(boolean lastPriceError) {
        this.lastPriceError = lastPriceError;
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
