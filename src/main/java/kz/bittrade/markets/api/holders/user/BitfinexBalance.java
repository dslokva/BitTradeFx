package kz.bittrade.markets.api.holders.user;

public class BitfinexBalance {
    private String type;
    private String currency;
    private double amount;
    private double available;

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public double getAvailable() {
        return available;
    }

    //  {"type":"exchange","currency":"eth","amount":"0.0","available":"0.0"}
}
