package kz.bittrade.com;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.UI;
import kz.bittrade.com.localstorage.LocalStorageExtension;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

public final class AppSettingsHolder {
    private LocalStorageExtension localStorage;
    private Properties settings;
    private HashMap<String, Boolean> selectedCoinsMap;
    private HashMap<String, Boolean> enabledMarketsMap;

    public AppSettingsHolder(CountDownLatch waitForJSLoopback) {
        selectedCoinsMap = new HashMap<>();
        enabledMarketsMap = new HashMap<>();

        settings = new Properties();

        localStorage = new LocalStorageExtension();
        localStorage.extend(UI.getCurrent());

        readLocalStorage(waitForJSLoopback);
    }

    public synchronized void setProperty(String key, String value) {
        settings.setProperty(key, value);
        localStorage.set(key, value);
    }

    public synchronized String getProperty(String key) {
        String value = "";
        if (settings.containsKey(key))
            value = (String) settings.get(key);
        return value;
    }

    public Boolean isPropertyEnabled(String propName) {
        return Boolean.valueOf(getProperty(propName));
    }

    private void readLocalStorage(CountDownLatch waitForJSLoopback) {
        testCallback(waitForJSLoopback);

        getValueFromLocalStorage(BFConstants.WEX_API_KEY);
        getValueFromLocalStorage(BFConstants.WEX_API_SECRET);

        getValueFromLocalStorage(BFConstants.BIT_API_KEY);
        getValueFromLocalStorage(BFConstants.BIT_API_SECRET);

        getValueFromLocalStorage(BFConstants.KRA_API_KEY);
        getValueFromLocalStorage(BFConstants.KRA_API_SECRET);

        getValueFromLocalStorage(BFConstants.CEX_API_KEY);
        getValueFromLocalStorage(BFConstants.CEX_API_SECRET);
        getValueFromLocalStorage(BFConstants.CEX_API_USERNAME);

        getValueFromLocalStorage(BFConstants.AUTO_SORT_COLUMN);

        getValueFromLocalStorage(BFConstants.AUTO_REFRESH_TIME);

        getValueFromLocalStorage(BFConstants.BITCOIN);
        getValueFromLocalStorage(BFConstants.BITCOIN_CASH);
        getValueFromLocalStorage(BFConstants.LITECOIN);
        getValueFromLocalStorage(BFConstants.ETHERIUM_COIN);
        getValueFromLocalStorage(BFConstants.ZCASH_COIN);
        getValueFromLocalStorage(BFConstants.DASH_COIN);
        getValueFromLocalStorage(BFConstants.RIPPLE_COIN);

        getValueFromLocalStorage(BFConstants.WEX);
        getValueFromLocalStorage(BFConstants.BITFINEX);
        getValueFromLocalStorage(BFConstants.KRAKEN);
        getValueFromLocalStorage(BFConstants.CEX);

        getValueFromLocalStorage(BFConstants.TOP_PANEL_FOLDED_AT_START);
        getValueFromLocalStorage(BFConstants.MIDDLE_PANEL_FOLDED_AT_START);
        getValueFromLocalStorage(BFConstants.BOTTOM_PANEL_FOLDED_AT_START);
    }

    private void testCallback(CountDownLatch waitForJSLoopback) {
        localStorage.get("testJSCallback", value -> {
            waitForJSLoopback.countDown();
        });
    }

    private void getValueFromLocalStorage(String propName) {
        localStorage.get(propName, value -> {
            if (value != null) settings.setProperty(propName, value);
        });
    }

    public String getNowString() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        return sdf.format(date);
    }

    public String getTimeDeltaString(long oldTime) {
        long timeDelta = (System.currentTimeMillis() - oldTime) / 1000;
        return timeDelta + " sec.";
    }


    public void updateCoinSelectState(CheckBox... chkBoxes) {
        for (CheckBox chkBox : chkBoxes) {
            selectedCoinsMap.put(chkBox.getCaption(), chkBox.getValue());
        }
    }

    public void updateMarketSelectMap(CheckBox... chkBoxes) {
        for (CheckBox chkBox : chkBoxes) {
            enabledMarketsMap.put(chkBox.getCaption(), chkBox.getValue());
        }
    }

    public HashMap<String, Boolean> getCoinSelectStateMap() {
        return selectedCoinsMap;
    }

    public HashMap<String, Boolean> getEnabledMarketsMap() {
        return enabledMarketsMap;
    }
}
