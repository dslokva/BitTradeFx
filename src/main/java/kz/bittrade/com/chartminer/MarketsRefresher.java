package kz.bittrade.com.chartminer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;
import kz.bittrade.markets.api.holders.currency.pairs.BitfinexCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.CexCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.KrakenCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.WexNzCurrencyPair;
import kz.bittrade.markets.api.lib.PublicApiAccessLib;

import static kz.bittrade.com.BFConstants.*;

public class MarketsRefresher {
    private static MarketsRefresher instance;

    public MarketsRefresher() {

    }

    public static synchronized MarketsRefresher getInstance() {
        if (instance == null) {
            instance = new MarketsRefresher();
        }
        return instance;
    }

    public void refreshWexNzCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        PublicApiAccessLib.setBasicUrl(WEX_API_BASIC_URL);

        String tickerName = currencyPairsHolder.getWexnzPair().getTickerName();
        String urlToMarket = currencyPairsHolder.getWexnzPair().getUrlToMarket();
        if (!tickerName.equals("---")) {
            JsonObject result = PublicApiAccessLib.performBasicRequest("ticker/", tickerName);

            if (result != null) {
                WexNzCurrencyPair wexNzCurrencyPair = new Gson().fromJson(result, WexNzCurrencyPair.class);
                if (wexNzCurrencyPair.getInfo() != null) {
                    wexNzCurrencyPair.setTickerName(tickerName);
                    wexNzCurrencyPair.setUrlToMarket(urlToMarket);
                    wexNzCurrencyPair.setMarketId(WEX_ID);
                    currencyPairsHolder.setWexnzPair(wexNzCurrencyPair);
                }
            }
        } else {
            WexNzCurrencyPair wexNzCurrencyPair = new WexNzCurrencyPair();
            wexNzCurrencyPair.setTickerName(tickerName);
            wexNzCurrencyPair.setUrlToMarket(urlToMarket);
            wexNzCurrencyPair.setMarketId(WEX_ID);
            currencyPairsHolder.setWexnzPair(wexNzCurrencyPair);
        }
    }

    public void refreshKrakenCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        PublicApiAccessLib.setBasicUrl(KRA_API_BASIC_URL);

        String tickerName = currencyPairsHolder.getKrakenPair().getTickerName();
        String urlToMarket = currencyPairsHolder.getKrakenPair().getUrlToMarket();
        JsonObject result = PublicApiAccessLib.performBasicRequest("Ticker?pair=", tickerName);

        if (result != null) {
            KrakenCurrencyPair krakenCurrencyPair = new Gson().fromJson(result, KrakenCurrencyPair.class);

            if (krakenCurrencyPair.getInfo() != null) {
                krakenCurrencyPair.setTickerName(tickerName);
                krakenCurrencyPair.setUrlToMarket(urlToMarket);
                krakenCurrencyPair.setMarketId(KRAKEN_ID);
                currencyPairsHolder.setKrakenPair(krakenCurrencyPair);
            }
        }
    }

    public void refreshBitfinexCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        PublicApiAccessLib.setBasicUrl(BIT_API_BASIC_URL);

        String tickerName = currencyPairsHolder.getBitfinexPair().getTickerName();
        String urlToMarket = currencyPairsHolder.getBitfinexPair().getUrlToMarket();
        JsonObject result = PublicApiAccessLib.performBasicRequest("pubticker/", tickerName);

        if (result != null) {
            BitfinexCurrencyPair bitfinexCurrencyPair = new Gson().fromJson(result, BitfinexCurrencyPair.class);
            if (bitfinexCurrencyPair != null) {
                bitfinexCurrencyPair.setTickerName(tickerName);
                bitfinexCurrencyPair.setUrlToMarket(urlToMarket);
                bitfinexCurrencyPair.setMarketId(BITFINEX_ID);
                currencyPairsHolder.setBitfinexPair(bitfinexCurrencyPair);
            }
        }
    }

    public void refreshCexCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        PublicApiAccessLib.setBasicUrl(CEX_API_BASIC_URL);

        String tickerName = currencyPairsHolder.getCexPair().getTickerName();
        String urlToMarket = currencyPairsHolder.getCexPair().getUrlToMarket();
        if (!tickerName.equals("---")) {
            JsonObject result = PublicApiAccessLib.performBasicRequest("ticker/", tickerName);

            if (result != null) {
                CexCurrencyPair cexCurrencyPair = new Gson().fromJson(result, CexCurrencyPair.class);
                if (cexCurrencyPair != null) {
                    cexCurrencyPair.setTickerName(tickerName);
                    cexCurrencyPair.setUrlToMarket(urlToMarket);
                    cexCurrencyPair.setMarketId(CEX_ID);
                    currencyPairsHolder.setCexPair(cexCurrencyPair);
                }
            }
        } else {
            CexCurrencyPair cexCurrencyPair = new CexCurrencyPair();
            cexCurrencyPair.setLast(0.0);
            cexCurrencyPair.setTickerName(tickerName);
            cexCurrencyPair.setMarketId(CEX_ID);
            currencyPairsHolder.setCexPair(cexCurrencyPair);
        }
    }

    public CurrencyPairsHolder initNewCurrencyPair(String pairName) {
        CurrencyPairsHolder ccp = new CurrencyPairsHolder();
        ccp.setName(pairName);

        String bitfinexTicker = "";
        String wexnzTicker = "";
        String krakenTicker = "";
        String cexTicker = "";

        String bitfinexUrl = "https://www.bitfinex.com/t/";
        String wexnzUrl = "https://wex.nz/exchange/";
        String krakenUrl = "https://www.kraken.com/u/trade";
        String cexUrl = "https://cex.io/trade/";

        switch (pairName) {
            case BITCOIN: {
                ccp.setPairId(BTC_ID);

                bitfinexTicker = "btcusd";
                bitfinexUrl += "BTC:USD";

                wexnzTicker = "btc_usd";
                wexnzUrl += "btc_usd";

                krakenTicker = "XBTUSD";

                cexTicker = "BTC/USD";
                cexUrl += "BTC-USD";
                break;
            }
            case BITCOIN_CASH: {
                ccp.setPairId(BCH_ID);

                bitfinexTicker = "bchusd";
                bitfinexUrl += "BCH:USD";

                wexnzTicker = "bch_usd";
                wexnzUrl += "bch_usd";

                krakenTicker = "BCHUSD";

                cexTicker = "BCH/USD";
                cexUrl += "BTC-USD";
                break;
            }
            case LITECOIN: {
                ccp.setPairId(LTC_ID);

                bitfinexTicker = "ltcusd";
                bitfinexUrl += "LTC:USD";

                wexnzTicker = "ltc_usd";
                wexnzUrl += "ltc_usd";

                krakenTicker = "LTCUSD";
                cexTicker = "---";///this ticker name define unsupported coin by market
                break;
            }
            case ETHERIUM_COIN: {
                ccp.setPairId(ETH_ID);

                bitfinexTicker = "ethusd";
                bitfinexUrl += "ETH:USD";

                wexnzTicker = "eth_usd";
                wexnzUrl += "eth_usd";

                krakenTicker = "ETHUSD";

                cexTicker = "ETH/USD";
                cexUrl += "ETH-USD";
                break;
            }
            case ZCASH_COIN: {
                ccp.setPairId(ZEC_ID);

                bitfinexTicker = "zecusd";
                bitfinexUrl += "ZEC:USD";

                wexnzTicker = "zec_usd";
                wexnzUrl += "zec_usd";

                krakenTicker = "ZECUSD";

                cexTicker = "ZEC/USD";
                cexUrl += "ZEC-USD";
                break;
            }
            case DASH_COIN: {
                ccp.setPairId(DASH_ID);

                bitfinexTicker = "dshusd";
                bitfinexUrl += "DASH:USD";

                wexnzTicker = "dsh_usd";
                wexnzUrl += "dsh_usd";

                krakenTicker = "DASHUSD";

                cexTicker = "DASH/USD";
                cexUrl += "DASH-USD";
                break;
            }
            case RIPPLE_COIN: {
                ccp.setPairId(XRP_ID);

                bitfinexTicker = "xrpusd";
                bitfinexUrl += "XRP:USD";

                wexnzTicker = "---";
                krakenTicker = "XRPUSD";

                cexTicker = "XRP/USD";
                cexUrl += "XRP-USD";
                break;
            }
            default: {
                bitfinexTicker = "";
                wexnzTicker = "";
                krakenTicker = "";
                cexTicker = "";
                System.out.println("Unknown coin name given in pairHolder init method.");
            }
        }
        BitfinexCurrencyPair bitfinexPair = ccp.getBitfinexPair();
        bitfinexPair.setTickerName(bitfinexTicker);
        bitfinexPair.setUrlToMarket(bitfinexUrl);

        WexNzCurrencyPair wexnzPair = ccp.getWexnzPair();
        wexnzPair.setTickerName(wexnzTicker);
        wexnzPair.setUrlToMarket(wexnzUrl);

        KrakenCurrencyPair krakenPair = ccp.getKrakenPair();
        krakenPair.setTickerName(krakenTicker);
        krakenPair.setUrlToMarket(krakenUrl);

        CexCurrencyPair cexPair = ccp.getCexPair();
        cexPair.setTickerName(cexTicker);
        cexPair.setUrlToMarket(cexUrl);

        return ccp;
    }

}
