package kz.bittrade.com;

public final class BFConstants {
    /* Simple markets Id's */
    public static final String WEX = "1";
    public static final String BITFINEX = "2";
    public static final String KRAKEN = "3";

    public static final String WEX_API_KEY = "wexApiKey";
    public static final String WEX_API_SECRET = "wexApiSecret";
    public static final String WEX_API_NONCE = "wexApiNonce";
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

    public static final String AUTO_SORT = "autoSortDeltaPercent";

    public static final String ALGORITHM_HMACSHA384 = "HmacSHA384";
    public static final String ALGORITHM_HMACSHA512 = "HmacSHA512";

    public static final String BITCOIN = "Bitcoin";
    public static final String BITCOIN_CASH = "Bitcoin Cash";
    public static final String LITECOIN = "Litecoin";
    public static final String ETHERIUM = "Etherium";
    public static final String ZCASH = "ZCash";
    public static final String DASH_COIN = "Dash Coin";

    public static final String MAIN_VIEW = "main";
    public static final String SETTINGS_VIEW = "settings";
}
