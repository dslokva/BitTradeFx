package kz.bittrade.markets.api.holders.user.balance;

public abstract class BalanceHolder {
    private String currency;
    private String amount;

    public BalanceHolder(String currency, double amount) {
        this.amount = String.format("%.6f", amount);
        this.currency = currency;
    }

    public String getCurrencyName() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }
}
