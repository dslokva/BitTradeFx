package kz.bittrade.markets.api.holders.user.cexio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CexIoBalance {
    private String error;
    private String username;
    private long timestamp;
    private CexIoCoinBalance ZEC;
    private CexIoCoinBalance BTC;
    private CexIoCoinBalance USD;
    private CexIoCoinBalance ETH;
    private CexIoCoinBalance BCH;
    private CexIoCoinBalance BTG;
    private CexIoCoinBalance DASH;
    private CexIoCoinBalance XRP;
    private CexIoCoinBalance EUR;
    private CexIoCoinBalance GBP;
    private CexIoCoinBalance RUB;
    private CexIoCoinBalance GHS;

    public String getTimestamp() {
        Date date = new Date((timestamp * 1000L));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        return sdf.format(date);
    }

    public String getError() {
        return error;
    }

    public CexIoCoinBalance getZEC() {
        return ZEC;
    }

    public CexIoCoinBalance getBTC() {
        return BTC;
    }

    public CexIoCoinBalance getUSD() {
        return USD;
    }

    public CexIoCoinBalance getETH() {
        return ETH;
    }

    public CexIoCoinBalance getBCH() {
        return BCH;
    }

    public CexIoCoinBalance getBTG() {
        return BTG;
    }

    public CexIoCoinBalance getDASH() {
        return DASH;
    }

    public CexIoCoinBalance getXRP() {
        return XRP;
    }
}
