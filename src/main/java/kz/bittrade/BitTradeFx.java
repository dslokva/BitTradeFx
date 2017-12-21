package kz.bittrade;

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
import kz.bittrade.com.chartminer.MarketDataMiner;
import kz.bittrade.com.chartminer.MarketsRefresher;
import kz.bittrade.com.kzcurrrate.KztRateCurrencyUpdaterTimer;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;
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
@SuppressWarnings("serial")
public class BitTradeFx extends UI {
    private Navigator navigator;
    private List<CurrencyPairsHolder> currencyPairsHolderList;
    private MainView mainView;
    private SettingsView settingsView;
    private AppSettingsHolder settings;
    private GridRefreshThread gridRefreshThread;
    private MarketsRefresher marketsRefresher;
    private CountDownLatch waitForJSLoopback;

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("BitTradeFx");
        waitForJSLoopback = new CountDownLatch(1);

        currencyPairsHolderList = new ArrayList<>();
        settings = new AppSettingsHolder(waitForJSLoopback);

        mainView = new MainView();
        settingsView = new SettingsView();
        gridRefreshThread = new GridRefreshThread();
        marketsRefresher = MarketsRefresher.getInstance();

        navigator = new Navigator(this, this);
        navigator.addView("", mainView);
        navigator.addView(BFConstants.MAIN_VIEW, mainView);
        navigator.addView(BFConstants.SETTINGS_VIEW, settingsView);
        //Note: Read LocalStorage values at first app run (during first call in browser) available only after init() method fully complete.
        //I think that because registerRpc call completes only after extend(), but this magic not for me. So AfterInitThread - is a my own lifehack.
        new AfterInitThread().start();
    }

    public void refreshCurrencyGrid(CurrencyPairsHolder currencyPairRow) {
        if (!gridRefreshThread.isAlive()) {
            gridRefreshThread = new GridRefreshThread();
            if (currencyPairRow == null) gridRefreshThread.refreshAll();
            else gridRefreshThread.refreshOne(currencyPairRow);
        } else
            showNotification("Timer", "Refresh thread already running, so we skip this run", 3000, Position.BOTTOM_RIGHT, "tray failure");
    }

    private void refreshCurrencyInfo(CurrencyPairsHolder item) {
        boolean wexEnabled = settings.isPropertyEnabled(BFConstants.WEX);
        boolean bitEnabled = settings.isPropertyEnabled(BFConstants.BITFINEX);
        boolean kraEnabled = settings.isPropertyEnabled(BFConstants.KRAKEN);
        boolean cexEnabled = settings.isPropertyEnabled(BFConstants.CEX);

        if (wexEnabled) {
            marketsRefresher.refreshWexNzCurrencyInfo(item);
        }

        if (bitEnabled) {
            marketsRefresher.refreshBitfinexCurrencyInfo(item);
        }

        if (kraEnabled) {
            marketsRefresher.refreshKrakenCurrencyInfo(item);
        }

        if (cexEnabled) {
            marketsRefresher.refreshCexCurrencyInfo(item);
        }
        item.defineMinMaxPrice(settings.getEnabledMarketsMap());
    }

    private void initCurrencyPairs() {
        currencyPairsHolderList.clear();

        if (settings.isPropertyEnabled(BFConstants.BITCOIN))
            currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.BITCOIN));

        if (settings.isPropertyEnabled(BFConstants.BITCOIN_CASH))
            currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.BITCOIN_CASH));

        if (settings.isPropertyEnabled(BFConstants.LITECOIN))
            currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.LITECOIN));

        if (settings.isPropertyEnabled(BFConstants.ETHERIUM_COIN))
            currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.ETHERIUM_COIN));

        if (settings.isPropertyEnabled(BFConstants.ZCASH_COIN))
            currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.ZCASH_COIN));

        if (settings.isPropertyEnabled(BFConstants.DASH_COIN))
            currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.DASH_COIN));

        if (settings.isPropertyEnabled(BFConstants.RIPPLE_COIN))
            currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(BFConstants.RIPPLE_COIN));
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

            TimerTask kztRateTimerTask = new KztRateCurrencyUpdaterTimer();
            java.util.Timer kztRateTimer = new java.util.Timer(true);
            kztRateTimer.scheduleAtFixedRate(kztRateTimerTask, 0, 43200 * 1000); //12 hours

            TimerTask marketMinerTimerTask = new MarketDataMiner();
            java.util.Timer marketMinerTimer = new java.util.Timer(true);
            marketMinerTimer.scheduleAtFixedRate(marketMinerTimerTask, 0, 30 * 1000); //30 sec
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


    public AppSettingsHolder getSettings() {
        return settings;
    }

    public MainView getMainView() {
        return mainView;
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
                Thread.sleep(500);
                access(() -> {
                    settingsView.updateValuesToUI();
                    push();
                });

                while (waitForJSLoopback.getCount() != 0)
                    Thread.sleep(150);

                Thread.sleep(1000);
                access(() -> {
                    initCurrencyPairs();
                    mainView.initMarketColumns();
                    mainView.initBalanceStubLabels();
                    mainView.setMainGridCorrectRowCount();
                    mainView.finishUIInit();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class GridRefreshThread extends Thread {
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
            long startTime = System.currentTimeMillis();
            try {
                access(() -> {
                    refreshLabel.setValue("Refreshing, please wait");
                    refreshProgressBar.setVisible(true);
                    btnSettings.setEnabled(false);
                    push();
                });

                synchronized (GridRefreshThread.this) {
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
                while (waitForRefreshers.getCount() != 0)
                    Thread.sleep(50);

                access(() -> {
                    refreshProgressBar.setVisible(false);
                    refreshProgressBar.setValue(0);
                    btnSettings.setEnabled(true);

                    String sortColumn = settings.getProperty(BFConstants.AUTO_SORT_COLUMN);
                    if (!sortColumn.equals(""))
                        currInfoGrid.sort(currInfoGrid.getColumn(sortColumn), SortDirection.DESCENDING);
                    else
                        currInfoGrid.sort(currInfoGrid.getColumn(BFConstants.GRID_DELTA_PERCENT_COLUMN), SortDirection.DESCENDING);

                    currInfoGrid.getDataProvider().refreshAll();

                    if (currencyPairsToRefresh.size() > 1)
                        refreshLabel.setValue("Updated at: " + settings.getNowString() + " (duration: " + settings.getTimeDeltaString(startTime) + ")");
                    else refreshLabel.setValue("Partially updated at: " + settings.getNowString());
                    push();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}