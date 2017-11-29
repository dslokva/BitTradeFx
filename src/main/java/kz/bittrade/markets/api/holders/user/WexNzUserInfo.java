package kz.bittrade.markets.api.holders.user;

public class WexNzUserInfo {
    private WexNzUserFunds funds;
    private WexNzKeyRights rights;
    private int transaction_count;
    private int open_orders;
    private long server_time;

    public WexNzUserFunds getFunds() {
        return funds;
    }

    public int getOpen_orders() {
        return open_orders;
    }

    public int getTransaction_count() {
        return transaction_count;
    }
}
