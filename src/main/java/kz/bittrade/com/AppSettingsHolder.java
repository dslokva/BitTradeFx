package kz.bittrade.com;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.UI;
import kz.bittrade.com.localstorage.LocalStorageExtension;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;

public final class AppSettingsHolder {
    private LocalStorageExtension localStorage;
    private Properties settings;
    private HashMap<String, Boolean> selectedCoinsMap;
    private HashMap<String, Boolean> enabledMarketsMap;

    public AppSettingsHolder() {
        selectedCoinsMap = new HashMap<>();
        enabledMarketsMap = new HashMap<>();

        settings = new Properties();

        localStorage = new LocalStorageExtension();
        localStorage.extend(UI.getCurrent());

        readLocalStorage();
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

    private void readLocalStorage() {
        getValueFromLocalStorage(BFConstants.WEX_API_KEY);
        getValueFromLocalStorage(BFConstants.WEX_API_SECRET);

        getValueFromLocalStorage(BFConstants.BIT_API_KEY);
        getValueFromLocalStorage(BFConstants.BIT_API_SECRET);

        getValueFromLocalStorage(BFConstants.KRA_API_KEY);
        getValueFromLocalStorage(BFConstants.KRA_API_SECRET);

        getValueFromLocalStorage(BFConstants.AUTO_SORT_COLUMN);

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
