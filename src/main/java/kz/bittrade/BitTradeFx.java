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
import kz.bittrade.com.KztRateCurrencyUpdaterTimer;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;
import kz.bittrade.markets.api.holders.currency.pairs.BitfinexCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.CexCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.KrakenCurrencyPair;
import kz.bittrade.markets.api.holders.currency.pairs.WexNzCurrencyPair;
import kz.bittrade.markets.api.lib.PublicApiAccessLib;
import kz.bittrade.views.MainView;
import kz.bittrade.views.SettingsView;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@Theme("mytheme")
@Push(PushMode.AUTOMATIC)
public class BitTradeFx extends UI {
    private Navigator navigator;
    private List<CurrencyPairsHolder> currencyPairsHolderList;
    private MainView mainView;
    private SettingsView settingsView;
    private AppSettingsHolder settings;
    private RefreshThread refreshThread;

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("BitTradeFx");
        currencyPairsHolderList = new ArrayList<>();
        settings = new AppSettingsHolder();

        mainView = new MainView();
        settingsView = new SettingsView();
        refreshThread = new RefreshThread();

        navigator = new Navigator(this, this);
        navigator.addView("", mainView);
        navigator.addView(BFConstants.MAIN_VIEW, mainView);
        navigator.addView(BFConstants.SETTINGS_VIEW, settingsView);
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
            System.out.println("BitTradeFx app started");

            TimerTask timerTask = new KztRateCurrencyUpdaterTimer();
            //running timer task as daemon thread
            java.util.Timer timer = new java.util.Timer(true);
            timer.scheduleAtFixedRate(timerTask, 0, 43200 * 1000);
        }

        @Override
        public void sessionInit(SessionInitEvent event) throws ServiceException {
            VaadinRequest request = event.getRequest();
            System.out.println("User session start, IP address: " + request.getRemoteAddr() + ", user-agent: " + request.getHeaders("user-agent").nextElement());
        }

        @Override
        public void sessionDestroy(SessionDestroyEvent event) {
            System.out.println("User session end");
        }
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
        boolean wexEnabled = settings.isPropertyEnabled(BFConstants.WEX);
        boolean bitEnabled = settings.isPropertyEnabled(BFConstants.BITFINEX);
        boolean kraEnabled = settings.isPropertyEnabled(BFConstants.KRAKEN);
        boolean cexEnabled = settings.isPropertyEnabled(BFConstants.CEX);

        if (wexEnabled) {
            refreshWexNzCurrencyInfo(item);
        }

        if (bitEnabled) {
            refreshBitfinexCurrencyInfo(item);
        }

        if (kraEnabled) {
            refreshKrakenCurrencyInfo(item);
        }

        if (cexEnabled) {
            refreshCexCurrencyInfo(item);
        }
        item.defineMinMaxPrice(settings.getEnabledMarketsMap());
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
    }

    private void refreshWexNzCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        PublicApiAccessLib.setBasicUrl(BFConstants.WEX_API_BASIC_URL);

        String tickerName = currencyPairsHolder.getWexnzPair().getTickerName();
        JsonObject result = PublicApiAccessLib.performBasicRequest("ticker/", tickerName);

        if (result != null) {
            WexNzCurrencyPair wexNzCurrencyPair = new Gson().fromJson(result, WexNzCurrencyPair.class);
            if (wexNzCurrencyPair.getInfo() != null) {
                wexNzCurrencyPair.setTickerName(tickerName);
                wexNzCurrencyPair.setMarketId(BFConstants.WEX_ID);
                currencyPairsHolder.setWexnzPair(wexNzCurrencyPair);
            }
        }
    }

    private void refreshKrakenCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        PublicApiAccessLib.setBasicUrl(BFConstants.KRA_API_BASIC_URL);

        String tickerName = currencyPairsHolder.getKrakenPair().getTickerName();
        JsonObject result = PublicApiAccessLib.performBasicRequest("Ticker?pair=", tickerName);

        if (result != null) {
            KrakenCurrencyPair krakenCurrencyPair = new Gson().fromJson(result, KrakenCurrencyPair.class);

            if (krakenCurrencyPair.getInfo() != null) {
                krakenCurrencyPair.setTickerName(tickerName);
                krakenCurrencyPair.setMarketId(BFConstants.KRAKEN_ID);
                currencyPairsHolder.setKrakenPair(krakenCurrencyPair);
            }
        }
    }

    private void refreshBitfinexCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        PublicApiAccessLib.setBasicUrl(BFConstants.BIT_API_BASIC_URL);

        String tickerName = currencyPairsHolder.getBitfinexPair().getTickerName();
        JsonObject result = PublicApiAccessLib.performBasicRequest("pubticker/", tickerName);

        if (result != null) {
            BitfinexCurrencyPair bitfinexCurrencyPair = new Gson().fromJson(result, BitfinexCurrencyPair.class);
            if (bitfinexCurrencyPair != null) {
                bitfinexCurrencyPair.setTickerName(tickerName);
                bitfinexCurrencyPair.setMarketId(BFConstants.BITFINEX_ID);
                currencyPairsHolder.setBitfinexPair(bitfinexCurrencyPair);
            }
        }
    }

    private void refreshCexCurrencyInfo(CurrencyPairsHolder currencyPairsHolder) {
        PublicApiAccessLib.setBasicUrl(BFConstants.CEX_API_BASIC_URL);

        String tickerName = currencyPairsHolder.getCexPair().getTickerName();
        if (!tickerName.equals("---")) {
            JsonObject result = PublicApiAccessLib.performBasicRequest("ticker/", tickerName);

            if (result != null) {
                CexCurrencyPair cexCurrencyPair = new Gson().fromJson(result, CexCurrencyPair.class);
                if (cexCurrencyPair != null) {
                    cexCurrencyPair.setTickerName(tickerName);
                    cexCurrencyPair.setMarketId(BFConstants.CEX_ID);
                    currencyPairsHolder.setCexPair(cexCurrencyPair);
                }
            }
        } else {
            CexCurrencyPair cexCurrencyPair = new CexCurrencyPair();
            cexCurrencyPair.setLast(0.0);
            cexCurrencyPair.setTickerName(tickerName);
            cexCurrencyPair.setMarketId(BFConstants.CEX_ID);
            currencyPairsHolder.setCexPair(cexCurrencyPair);
        }
    }

    public CurrencyPairsHolder initNewCurrencyPair(String pairName) {
        CurrencyPairsHolder ccp = new CurrencyPairsHolder();
        ccp.setName(pairName);
        ccp.setDisplayName(pairName);
        String bitfinexTicker = "";
        String wexnzTicker = "";
        String krakenTicker = "";
        String cexTicker = "";

        switch (pairName) {
            case BFConstants.BITCOIN: {
                bitfinexTicker = "btcusd";
                wexnzTicker = "btc_usd";
                krakenTicker = "XBTUSD";
                cexTicker = "BTC/USD";
                break;
            }
            case BFConstants.BITCOIN_CASH: {
                bitfinexTicker = "bchusd";
                wexnzTicker = "bch_usd";
                krakenTicker = "BCHUSD";
                cexTicker = "BCH/USD";
                break;
            }
            case BFConstants.LITECOIN: {
                bitfinexTicker = "ltcusd";
                wexnzTicker = "ltc_usd";
                krakenTicker = "LTCUSD";
                cexTicker = "---";
                break;
            }
            case BFConstants.ETHERIUM: {
                bitfinexTicker = "ethusd";
                wexnzTicker = "eth_usd";
                krakenTicker = "ETHUSD";
                cexTicker = "ETH/USD";
                break;
            }
            case BFConstants.ZCASH: {
                bitfinexTicker = "zecusd";
                wexnzTicker = "zec_usd";
                krakenTicker = "ZECUSD";
                cexTicker = "ZEC/USD";
                break;
            }
            case BFConstants.DASH_COIN: {
                bitfinexTicker = "dshusd";
                wexnzTicker = "dsh_usd";
                krakenTicker = "DASHUSD";
                cexTicker = "DASH/USD";
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
        ccp.getBitfinexPair().setTickerName(bitfinexTicker);
        ccp.getWexnzPair().setTickerName(wexnzTicker);
        ccp.getKrakenPair().setTickerName(krakenTicker);
        ccp.getCexPair().setTickerName(cexTicker);
        return ccp;
    }

    public AppSettingsHolder getSettings() {
        return settings;
    }

    public List<CurrencyPairsHolder> getCurrencyPairsHolderList() {
        return currencyPairsHolderList;
    }

    public void showNotification(String caption, String description, int delay, Position position, String styleName) {
        Notification notification = new Notification(caption, description);
        notification.setDelayMsec(delay);
        notification.setPosition(position);
        notification.setStyleName(styleName);
        notification.show(Page.getCurrent());
    }

    public class AfterInitThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1500);
                access(() -> {
                    settingsView.updateValuesToUI();
                    push();
                });

                Thread.sleep(200);

                access(() -> {
                    initCurrencyPairs();
                    mainView.initMarketColumns();
                    mainView.initBalanceStubLabels();
                    mainView.setMainGridCorrectRowCount();
                    mainView.hideInitStub();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class RefreshThread extends Thread {
        Grid<CurrencyPairsHolder> currInfoGrid = mainView.getCurrInfoGrid();
        ProgressBar refreshProgressBar = mainView.getRefreshProgressBar();
        Label refreshLabel = mainView.getLabelRefresh();
        Button btnSettings = mainView.getBtnSettings();
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
            final CountDownLatch waitForRefreshers = new CountDownLatch(currencyPairsToRefresh.size());

            try {
                access(() -> {
                    refreshLabel.setValue("Refreshing, please wait");
                    refreshProgressBar.setVisible(true);
                    btnSettings.setEnabled(false);
                    push();
                });

                synchronized (RefreshThread.this) {
                    for (CurrencyPairsHolder currencyPairsHolder : currencyPairsToRefresh) {
                        String oldName = currencyPairsHolder.getDisplayName();
                        currencyPairsHolder.setDisplayName("<b><font color ='#000066'> * " + oldName + "</font></b>");
                        Thread.sleep(100);
                        access(() -> {
                            try {
                                Thread refresh = new Thread(() -> {
                                    refreshCurrencyInfo(currencyPairsHolder);
                                    currencyPairsHolder.setDisplayName(oldName);
                                    waitForRefreshers.countDown();
                                    synchronized (this) {
                                        currInfoGrid.getDataProvider().refreshAll();
                                        float current = refreshProgressBar.getValue();
                                        refreshProgressBar.setValue(current + (1.0f / currencyPairsToRefresh.size()));
                                        push();
                                    }
                                });
                                refresh.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                    }
                }
                while (waitForRefreshers.getCount() != 0) //Проверяем, собрались ли все автомобили
                    Thread.sleep(50);

                currInfoGrid.getDataProvider().refreshAll(); //to end of all threads
                access(() -> {
                    if (currencyPairsToRefresh.size() > 1)
                        refreshLabel.setValue("Updated at: " + settings.getNowString());
                    else refreshLabel.setValue("Partially updated at: " + settings.getNowString());

                    refreshProgressBar.setVisible(false);
                    refreshProgressBar.setValue(0);

                    btnSettings.setEnabled(true);

                    if (settings.isPropertyEnabled(BFConstants.AUTO_SORT))
                        currInfoGrid.sort(currInfoGrid.getColumns().get(3), SortDirection.DESCENDING);
                    push();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}