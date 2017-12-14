package kz.bittrade.markets.api.lib;

public abstract class ApiAccessLib {
    protected static String PRIVATE_API_URL = "";
    protected static String PUBLIC_API_URL = "";

    public static void setPrivateUrl(String url) {
        PRIVATE_API_URL = url;
    }

    public static void setBasicUrl(String url) {
        PUBLIC_API_URL = url;
    }

    public static void log(String str) {
        System.out.println("[API LIB]: ".concat(str));
    }

}
