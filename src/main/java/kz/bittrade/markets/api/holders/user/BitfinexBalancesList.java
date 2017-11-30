package kz.bittrade.markets.api.holders.user;

import java.util.HashMap;

public class BitfinexBalancesList {
    private HashMap<String, BitfinexBalance> balances;

    public BitfinexBalancesList() {
        balances = new HashMap<>();
    }

    public HashMap<String, BitfinexBalance> getBalancesMap() {
        return balances;
    }

    public double getUsd() {
        return returnAvailable("usd");
    }

    private double returnAvailable(String currency) {
        BitfinexBalance balance = balances.get(currency);
        if (balance != null) return balance.getAvailable();
        else return 0.0;
    }

    public double getBch() {
        return returnAvailable("bch");
    }

    public double getBtc() {
        return returnAvailable("btc");
    }

    public double getLtc() {
        return returnAvailable("ltc");
    }

    public double getZec() {
        return returnAvailable("zec");
    }

    public double getDsh() {
        return returnAvailable("dsh");
    }

    public double getEth() {
        return returnAvailable("eth");
    }

    public void add(BitfinexBalance bitfinexBalance) {
        balances.put(bitfinexBalance.getCurrency(), bitfinexBalance);
    }
}
