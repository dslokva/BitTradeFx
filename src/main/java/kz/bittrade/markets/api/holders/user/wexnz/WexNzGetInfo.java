package kz.bittrade.markets.api.holders.user.wexnz;

import com.google.gson.annotations.SerializedName;

public class WexNzGetInfo {
    private int success;
    private String error;

    @SerializedName(value = "return")
    private WexNzUserInfo info;


    public int getSuccess() {
        return success;
    }

    public WexNzUserInfo getInfo() {
        return info;
    }

    public String getError() {
        return error;
    }
}

//    getInfo result:
//        {
//        "success":1,
//        "return":
//        {
//        "funds":{"usd":100.05908603,"btc":0,"ltc":0,"nmc":0,"rur":0,"eur":0,"nvc":0,"ppc":0,"dsh":0,"eth":0.0000176,"bch":0,"usdet":0,"btcet":0,"ltcet":0,"ethet":0,"nmcet":0,"nvcet":0,"ppcet":0,"dshet":0,"ruret":0,"euret":0,"bchet":0,"zec":0},
//
//        "rights":{"info":1,"trade":0,"withdraw":0},
//
//        "transaction_count":0,"open_orders":0,"server_time":1511636468
//        }
//
//        }