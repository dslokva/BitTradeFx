package kz.bittrade.markets.api.holders.user;

import java.util.HashMap;

public class BitfinexBalancesList {
    private HashMap<String, BitfinexBalance> balances;
    private Object onOrdersUsd;

    public BitfinexBalancesList() {
        balances = new HashMap<>();
    }

    public HashMap<String, BitfinexBalance> getBalancesMap() {
        return balances;
    }

    private double returnAvailable(String currency) {
        BitfinexBalance balance = balances.get(currency);
        if (balance != null) return balance.getAvailable();
        else return 0.0;
    }

    private double returnOnOrder(String currency) {
        BitfinexBalance balance = balances.get(currency);
        if (balance != null) {
            return balance.getAvailable() - balance.getAmount();
        } else return 0.0;
    }

    public double getAvailUsd() {
        return returnAvailable("usd");
    }

    public double getAvailBch() {
        return returnAvailable("bch");
    }

    public double getAvailBtc() {
        return returnAvailable("btc");
    }

    public double getAvailLtc() {
        return returnAvailable("ltc");
    }

    public double getAvailZec() {
        return returnAvailable("zec");
    }

    public double getAvailDsh() {
        return returnAvailable("dsh");
    }

    public double getAvailEth() {
        return returnAvailable("eth");
    }

    public void add(BitfinexBalance bitfinexBalance) {
        balances.put(bitfinexBalance.getCurrency(), bitfinexBalance);
    }

    public double getOnOrdersUsd() {
        return returnOnOrder("usd");
    }

    public double getOnOrdersBtc() {
        return returnOnOrder("btc");
    }

    public double getOnOrdersBch() {
        return returnOnOrder("bch");
    }

    public double getOnOrdersLtc() {
        return returnOnOrder("ltc");
    }

    public double getOnOrdersEth() {
        return returnOnOrder("eth");
    }

    public double getOnOrdersZec() {
        return returnOnOrder("zec");
    }

    public double getOnOrdersDsh() {
        return returnOnOrder("dsh");
    }

}
