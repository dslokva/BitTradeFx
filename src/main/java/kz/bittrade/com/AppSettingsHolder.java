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
        localStorage.get(BFConstants.WEX_API_KEY, value -> {
            if (value != null) {
                settings.setProperty(BFConstants.WEX_API_KEY, value);
            }
        });
        localStorage.get(BFConstants.WEX_API_SECRET, value -> {
            if (value != null) {
                settings.setProperty(BFConstants.WEX_API_SECRET, value);
            }
        });
        localStorage.get(BFConstants.WEX_API_NONCE, value -> {
            if (value == null) value = "-1";
            settings.setProperty(BFConstants.WEX_API_NONCE, value);
        });


        localStorage.get(BFConstants.BIT_API_KEY, value -> {
            if (value != null) {
                settings.setProperty(BFConstants.BIT_API_KEY, value);
            }
        });
        localStorage.get(BFConstants.BIT_API_SECRET, value -> {
            if (value != null) {
                settings.setProperty(BFConstants.BIT_API_SECRET, value);
            }
        });

        localStorage.get(BFConstants.KRA_API_KEY, value -> {
            if (value != null) {
                settings.setProperty(BFConstants.KRA_API_KEY, value);
            }
        });
        localStorage.get(BFConstants.KRA_API_SECRET, value -> {
            if (value != null) {
                settings.setProperty(BFConstants.KRA_API_SECRET, value);
            }
        });
        localStorage.get(BFConstants.AUTO_SORT, value -> {
            if (value != null) {
                settings.setProperty(BFConstants.AUTO_SORT, value);
            }
        });
    }

    public String getNowString() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        return sdf.format(date);
    }
}
