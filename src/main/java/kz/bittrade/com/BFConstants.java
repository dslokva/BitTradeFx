package kz.bittrade.com;

import java.util.HashMap;

public final class BFConstants {
    private static HashMap mapCoinNames;
    private static HashMap mapCoinIds;

    public BFConstants() {
        mapCoinNames = new HashMap();
        mapCoinNames.put(BITCOIN, BTC_ID);
        mapCoinNames.put(BITCOIN_CASH, BCH_ID);
        mapCoinNames.put(LITECOIN, LTC_ID);
        mapCoinNames.put(ETHERIUM_COIN, ETH_ID);
        mapCoinNames.put(ZCASH_COIN, ZEC_ID);
        mapCoinNames.put(DASH_COIN, DASH_ID);
        mapCoinNames.put(RIPPLE_COIN, XRP_ID);

        mapCoinIds = new HashMap();
        mapCoinIds.put(BTC_ID, BITCOIN);
        mapCoinIds.put(BCH_ID, BITCOIN_CASH);
        mapCoinIds.put(LTC_ID, LITECOIN);
        mapCoinIds.put(ETH_ID, ETHERIUM_COIN);
        mapCoinIds.put(ZEC_ID, ZCASH_COIN);
        mapCoinIds.put(DASH_ID, DASH_COIN);
        mapCoinIds.put(XRP_ID, RIPPLE_COIN);
    }

    /* Simple markets Id's */
    public static final String WEX_ID = "1";
    public static final String BITFINEX_ID = "2";
    public static final String KRAKEN_ID = "3";
    public static final String CEX_ID = "4";

    public static final Integer BTC_ID = 1;
    public static final Integer BCH_ID = 2;
    public static final Integer LTC_ID = 3;
    public static final Integer ETH_ID = 4;
    public static final Integer ZEC_ID = 5;
    public static final Integer DASH_ID = 6;
    public static final Integer XRP_ID = 7;

    public static final String WEX = "WEX";
    public static final String BITFINEX = "Bitfinex";
    public static final String KRAKEN = "Kraken";
    public static final String CEX = "Cex";

    public static final String WEX_API_KEY = "wexApiKey";
    public static final String WEX_API_SECRET = "wexApiSecret";
    public static final String WEX_API_BASIC_URL = "https://wex.nz/api/3/";
    public static final String WEX_API_PRIVATE_URL = "https://wex.nz/tapi/";
    public static final String WEX_API_KEY_PATTERN = "([A-Z|0-9]{8}-){4}[A-Z|0-9]{8}";
    public static final String WEX_API_SECRET_PATTERN = "[a-f|0-9]{64}";

    public static final String BIT_API_KEY = "bitApiKey";
    public static final String BIT_API_SECRET = "bitApiSecret";
    public static final String BIT_API_BASIC_URL = "https://api.bitfinex.com/v1/";

    public static final String KRA_API_KEY = "kraApiKey";
    public static final String KRA_API_SECRET = "kraApiSecret";
    public static final String KRA_API_BASIC_URL = "https://api.kraken.com/0/public/";

    public static final String CEX_API_KEY = "cexApiKey";
    public static final String CEX_API_SECRET = "cexApiSecret";
    public static final String CEX_API_USERNAME = "cexApiUsername";
    public static final String CEX_API_BASIC_URL = "https://cex.io/api/";

    public static final String AUTO_SORT_COLUMN = "autoSortColumn";
    public static final String AUTO_REFRESH_TIME = "autoRefreshTime";

    public static final String ALGORITHM_HMACSHA384 = "HmacSHA384";
    public static final String ALGORITHM_HMACSHA512 = "HmacSHA512";

    public static final String USD = "US Dollar";
    public static final String BITCOIN = "Bitcoin";
    public static final String BITCOIN_CASH = "Bitcoin Cash";
    public static final String LITECOIN = "Litecoin";
    public static final String ETHERIUM_COIN = "Etherium";
    public static final String ZCASH_COIN = "ZCash";
    public static final String DASH_COIN = "Dash Coin";
    public static final String RIPPLE_COIN = "Ripple";

    public static final String MAIN_VIEW = "main";
    public static final String SETTINGS_VIEW = "settings";

    public static final String GRID_PAIR_NAME_COLUMN = "pair_name_column";
    public static final String GRID_DELTA_DOUBLE_COLUMN = "delta_double_column";
    public static final String GRID_DELTA_PERCENT_COLUMN = "delta_percent_column";
    public static final String GRID_WEX_COLUMN = "wex_column";
    public static final String GRID_BITFINEX_COLUMN = "bitfinex_column";
    public static final String GRID_KRAKEN_COLUMN = "kraken_column";
    public static final String GRID_CEX_COLUMN = "cex_column";

    public static final String NATIONAL_BANK_CURR_RATE_UPDATE_URL = "http://www.nationalbank.kz/rss/rates_all.xml";

    public static final String TOP_PANEL_FOLDED_AT_START = "topPanelFolded";

    public static Integer getCoinIdByName(String coinName) {
        return (Integer) mapCoinNames.get(coinName);
    }

    public static String getCoinNameById(Integer coinId) {
        return (String) mapCoinIds.get(coinId);
    }
}