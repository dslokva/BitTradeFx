package kz.bittrade;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.*;
import com.vaadin.shared.Position;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;
import kz.bittrade.markets.api.holders.currency.pairs.BitfinexCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.KrakenCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.WexNzCurrencyPair;
import kz.bittrade.markets.api.lib.PublicApiAccessLib;
import kz.bittrade.views.MainView;
import kz.bittrade.views.SettingsView;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

@Theme("mytheme")
@Push(PushMode.AUTOMATIC)
public class BitTradeFx extends UI {
    Navigator navigator;
    private List<CurrencyPairsHolder> currencyPairsHolderList;
    private MainView mainView;
    private AppSettingsHolder settings;
    private RefreshThread refreshThread;

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("BitTradeFx");
        currencyPairsHolderList = new ArrayList<>();
        settings = new AppSettingsHolder();

        mainView = new MainView();
        refreshThread = new RefreshThread();

        navigator = new Navigator(this, this);
        navigator.addView("", mainView);
        navigator.addView(BFConstants.MAIN_VIEW, mainView);
        navigator.addView(BFConstants.SETTINGS_VIEW, new SettingsView());
        //Note: Read LocalStorage values at first app run (during first call in browser) available only after init() method fully complete.
        //I think that because registerRpc call completes only after extend(), but this magic not for me. So AfterInitThread - is a my own lifehack.
        new AfterInitThread().start();
    }

    @WebServlet(urlPatterns = "/*", name = "BitTradeFxServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = BitTradeFx.class, productionMode = true)
    public static class MyUIServlet extends VaadinServlet implements SessionInitListener, SessionDestroyListener {
        @Override
        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            getService().addSessionInitListener(this);
            getService().addSessionDestroyListener(this);
        }

        @Override
        public void sessionInit(SessionInitEvent event)
                throws ServiceException {
            System.out.println("session start");
        }

        @Override
        public void sessionDestroy(SessionDestroyEvent event) {
            System.out.println("session end");
        }
    }

    public AppSettingsHolder getSettings() {
        return settings;
    }

    public List<CurrencyPairsHolder> getCurrencyPairsHolderList() {
        return currencyPairsHolderList;
    }

    public void refreshCurrencyGrid(CurrencyPairsHolder currencyPairRow) {
        if (!refreshThread.isAlive()) {
            refreshThread = new RefreshThread();
            if (currencyPairRow == null) refreshThread.refreshAll();
            else refreshThread.refreshOne(currencyPairRow);
        } else
            showNotification("Timer", "Refresh thread already running, so we skip this run", 3000, Position.BOTTOM_RIGHT, "tray failure");
    }

    private void refreshCurrencyInfo(CurrencyPairsHolder item) {
        refreshBitfinexCurrencyInfo(item);
        refreshWexNzCurrencyInfo(item);
        refreshKrakenCurrencyInfo(item);
        item.defineMinMaxPrice();
    }

    private void initCurrencyPairs() {
        currencyPairsHolderList.clear();

        if (settings.isPropertyEnabled(BFConstants.BITCOIN))
            currencyPairsHolderList.add(initNewCurrencyPair(BFConstants.BITCOIN));
        if (settings.isPropertyEnabled(BFConstants.BITCOIN_CASH))
            currencyPairsHolderList.add(initNewCurrencyPair(BFConstants.BITCOIN_CASH));
        if (settings.isPropertyEnabled(BFConstants.LITECOIN))
            currencyPairsHolderList.add(initNewCurrencyPair(BFConstants.LITECOIN));
        if (settings.isPropertyEnabled(BFConstants.ETHERIUM))
            currencyPairsHolderList.add(initNewCurrencyPair(BFConstants.ETHERIUM));
        if (settings.isPropertyEnabled(BFConstants.ZCASH))
            currencyPairsHolderList.add(initNewCurrencyPair(BFConstants.ZCASH));
        if (settings.isPropertyEnabled(BFConstants.DASH_COIN))
            currencyPairsHolderList.add(initNewCurrencyPair(BFConstants.DASH_COIN));

        mainView.setMainGridRowCount(currencyPairsHolderList.size());
    }

    public class RefreshThread extends Thread {
        Grid<CurrencyPairsHolder> currInfoGrid = mainView.getCurrInfoGrid();
        ProgressBar refreshProgressBar = mainView.getRefreshProgressBar();
        Label refreshLabel = mainView.getLabelRefresh();
        List<CurrencyPairsHolder> currencyPairsToRefresh = new ArrayList<>();

        void refreshAll() {
            currencyPairsToRefresh = currencyPairsHolderList;
            start();
        }

        void refreshOne(CurrencyPairsHolder currencyPairRow) {
            currencyPairsToRefresh.add(currencyPairRow);
            start();
        }

        @Override
        public void run() {
            try {
                synchronized (RefreshThread.this) {
                    for (CurrencyPairsHolder currencyPairsHolder : currencyPairsToRefresh) {
                        String oldName = currencyPairsHolder.getName();
                        currencyPairsHolder.setName("<b><font color ='#000066'> * " + oldName + "</font></b>");
                        Thread.sleep(60);
                        access(() -> {
                            float current = refreshProgressBar.getValue();
                            refreshProgressBar.setValue(current + (1.0f / currencyPairsToRefresh.size()));
                            currInfoGrid.getDataProvider().refreshAll();
                            push();
                        });
                        Thread.sleep(60);
                        refreshCurrencyInfo(currencyPairsHolder);
                        currencyPairsHolder.setName(oldName);
                    }
                }
                access(() -> {
                    if (currencyPairsToRefresh.size() > 1)
                        refreshLabel.setValue("Updated at: " + settings.getNowString());
                    else refreshLabel.setValue("Partially updated at: " + settings.getNowString());

                    refreshProgressBar.setVisible(false);
                    refreshProgressBar.setValue(0);

                    if (settings.isPropertyEnabled(BFConstants.AUTO_SORT))
                        currInfoGrid.sort(currInfoGrid.getColumns().get(3), SortDirection.DESCENDING);
                    push();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshWexNzCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        boolean resultParsedWell;
        PublicApiAccessLib.setBasicUrl(BFConstants.WEX_API_BASIC_URL);

        resultParsedWell = false;
        String tickerName = currencyPairsHolder.getWexnzPair().getTickerName();
        JsonObject result = PublicApiAccessLib.performBasicRequest("ticker/", tickerName);

        if (result != null) {
            WexNzCurrencyPair wexNzCurrencyPair = new Gson().fromJson(result, WexNzCurrencyPair.class);
            if (wexNzCurrencyPair.getInfo() != null) {
                resultParsedWell = true;
                wexNzCurrencyPair.setTickerName(tickerName);
                wexNzCurrencyPair.setMarketId(BFConstants.WEX);
                currencyPairsHolder.setWexnzPair(wexNzCurrencyPair);
            }
        }
        if (!resultParsedWell) currencyPairsHolder.getWexnzPair().setLastPriceError(true);
    }

    private void refreshKrakenCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        boolean resultParsedWell;
        PublicApiAccessLib.setBasicUrl(BFConstants.KRA_API_BASIC_URL);

        resultParsedWell = false;
        String tickerName = currencyPairsHolder.getKrakenPair().getTickerName();
        JsonObject result = PublicApiAccessLib.performBasicRequest("Ticker?pair=", tickerName);

        if (result != null) {
            KrakenCurrencyPair krakenCurrencyPair = new Gson().fromJson(result, KrakenCurrencyPair.class);

            if (krakenCurrencyPair.getInfo() != null) {
                resultParsedWell = true;
                krakenCurrencyPair.setTickerName(tickerName);
                krakenCurrencyPair.setMarketId(BFConstants.KRAKEN);
                currencyPairsHolder.setKrakenPair(krakenCurrencyPair);
            }
        }
        if (!resultParsedWell) currencyPairsHolder.getKrakenPair().setLastPriceError(true);
    }

    private void refreshBitfinexCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        boolean resultParsedWell;
        PublicApiAccessLib.setBasicUrl(BFConstants.BIT_API_BASIC_URL);
        resultParsedWell = false;

        String tickerName = currencyPairsHolder.getBitfinexPair().getTickerName();
        JsonObject result = PublicApiAccessLib.performBasicRequest("pubticker/", tickerName);

        if (result != null) {
            BitfinexCurrencyPair bitfinexCurrencyPair = new Gson().fromJson(result, BitfinexCurrencyPair.class);
            if (bitfinexCurrencyPair != null) {
                resultParsedWell = true;
                bitfinexCurrencyPair.setTickerName(tickerName);
                bitfinexCurrencyPair.setMarketId(BFConstants.BITFINEX);
                currencyPairsHolder.setBitfinexPair(bitfinexCurrencyPair);
            }
        }
        if (!resultParsedWell) currencyPairsHolder.getBitfinexPair().setLastPriceError(true);
    }

    public CurrencyPairsHolder initNewCurrencyPair(String pairName) {
        CurrencyPairsHolder ccp = new CurrencyPairsHolder();
        ccp.setName(pairName);
        String bitfinexTicker = "";
        String wexnzTicker = "";
        String krakenTicker = "";

        switch (pairName) {
            case BFConstants.BITCOIN: {
                bitfinexTicker = "btcusd";
                wexnzTicker = "btc_usd";
                krakenTicker = "XBTUSD";
                break;
            }
            case BFConstants.BITCOIN_CASH: {
                bitfinexTicker = "bchusd";
                wexnzTicker = "bch_usd";
                krakenTicker = "BCHUSD";
                break;
            }
            case BFConstants.LITECOIN: {
                bitfinexTicker = "ltcusd";
                wexnzTicker = "ltc_usd";
                krakenTicker = "LTCUSD";
                break;
            }
            case BFConstants.ETHERIUM: {
                bitfinexTicker = "ethusd";
                wexnzTicker = "eth_usd";
                krakenTicker = "ETHUSD";
                break;
            }
            case BFConstants.ZCASH: {
                bitfinexTicker = "zecusd";
                wexnzTicker = "zec_usd";
                krakenTicker = "ZECUSD";
                break;
            }
            case BFConstants.DASH_COIN: {
                bitfinexTicker = "dshusd";
                wexnzTicker = "dsh_usd";
                krakenTicker = "DASHUSD";
                break;
            }
            default: {
                bitfinexTicker = "";
                wexnzTicker = "";
                krakenTicker = "";
                System.out.println("Unknown coin name given while pairHolder init method.");
            }
        }
        ccp.getBitfinexPair().setTickerName(bitfinexTicker);
        ccp.getWexnzPair().setTickerName(wexnzTicker);
        ccp.getKrakenPair().setTickerName(krakenTicker);
        return ccp;
    }

    public class AfterInitThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                System.out.println("inited: " + settings.isPropertyEnabled(BFConstants.ETHERIUM));
                initCurrencyPairs();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void showNotification(String caption, String description, int delay, Position position, String styleName) {
        Notification notification = new Notification(caption, description);
        notification.setDelayMsec(delay);
        notification.setPosition(position);
        notification.setStyleName(styleName);
        notification.show(Page.getCurrent());
    }
}