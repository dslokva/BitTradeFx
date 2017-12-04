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
    private HashMap<String, Boolean> chkCoinSelectState;

    public AppSettingsHolder() {
        chkCoinSelectState = new HashMap<>();
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

    private void readLocalStorage() {
        getValueFromLocalStorage(BFConstants.WEX_API_KEY);
        getValueFromLocalStorage(BFConstants.WEX_API_SECRET);
        getValueFromLocalStorage(BFConstants.WEX_API_NONCE);

        getValueFromLocalStorage(BFConstants.BIT_API_KEY);
        getValueFromLocalStorage(BFConstants.BIT_API_SECRET);

        getValueFromLocalStorage(BFConstants.KRA_API_KEY);
        getValueFromLocalStorage(BFConstants.KRA_API_SECRET);

        getValueFromLocalStorage(BFConstants.AUTO_SORT);

        getValueFromLocalStorage(BFConstants.BITCOIN);
        getValueFromLocalStorage(BFConstants.BITCOIN_CASH);
        getValueFromLocalStorage(BFConstants.LITECOIN);
        getValueFromLocalStorage(BFConstants.ETHERIUM);
        getValueFromLocalStorage(BFConstants.ZCASH);
        getValueFromLocalStorage(BFConstants.DASH_COIN);
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

    public Boolean isPropertyEnabled(String propName) {
        return Boolean.valueOf(getProperty(propName));
    }

    public void updateCoinSelectState(CheckBox... chkBoxes) {
        for (CheckBox chkBox : chkBoxes) {
            chkCoinSelectState.put(chkBox.getCaption(), chkBox.getValue());
        }
    }

    public HashMap<String, Boolean> getCoinSelectStateMap() {
        return chkCoinSelectState;
    }
}
