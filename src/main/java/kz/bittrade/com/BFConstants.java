package kz.bittrade.com;

public final class BFConstants {
    /* Simple markets Id's */
    public static final String WEX_ID = "1";
    public static final String BITFINEX_ID = "2";
    public static final String KRAKEN_ID = "3";
    public static final String CEX_ID = "4";

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
    public static final String CEX_API_BASIC_URL = "https://cex.io/api/";

    public static final String AUTO_SORT_COLUMN = "autoSortColumn";

    public static final String ALGORITHM_HMACSHA384 = "HmacSHA384";
    public static final String ALGORITHM_HMACSHA512 = "HmacSHA512";

    public static final String USD = "US Dollar";
    public static final String BITCOIN = "Bitcoin";
    public static final String BITCOIN_CASH = "Bitcoin Cash";
    public static final String LITECOIN = "Litecoin";
    public static final String ETHERIUM = "Etherium";
    public static final String ZCASH = "ZCash";
    public static final String DASH_COIN = "Dash Coin";

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
}