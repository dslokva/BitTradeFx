package kz.bittrade.com;

import com.vaadin.ui.UI;
import kz.bittrade.com.localstorage.LocalStorageExtension;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

public final class AppSettingsHolder {
    private static AppSettingsHolder instance;
    private static LocalStorageExtension localStorage;
    private static Properties settings;

    private AppSettingsHolder() {
        settings = new Properties();
        localStorage = new LocalStorageExtension();
        localStorage.extend(UI.getCurrent());

        readLocalStorage();
    }

    public static synchronized AppSettingsHolder getInstance() {
        if (instance == null) {
            instance = new AppSettingsHolder();
        }
        return instance;
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

        getValueFromLocalStorage(BFConstants.BTC_ENABLED);
        getValueFromLocalStorage(BFConstants.BCH_ENABLED);
        getValueFromLocalStorage(BFConstants.LTC_ENABLED);
        getValueFromLocalStorage(BFConstants.ETH_ENABLED);
        getValueFromLocalStorage(BFConstants.ZEC_ENABLED);
        getValueFromLocalStorage(BFConstants.DSH_ENABLED);
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
}
