package kz.bittrade.views;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.data.ValueProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import kz.bittrade.BitTradeFx;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.com.chartminer.MarketsRefresher;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;
import kz.bittrade.markets.api.holders.user.bitfinex.BitfinexBalance;
import kz.bittrade.markets.api.holders.user.bitfinex.BitfinexBalancesList;
import kz.bittrade.markets.api.holders.user.cexio.CexIoBalance;
import kz.bittrade.markets.api.holders.user.holders.BalanceHolder;
import kz.bittrade.markets.api.holders.user.holders.BitfinexBalanceHolder;
import kz.bittrade.markets.api.holders.user.holders.CexIoBalanceHolder;
import kz.bittrade.markets.api.holders.user.holders.WexNzBalanceHolder;
import kz.bittrade.markets.api.holders.user.wexnz.WexNzGetInfo;
import kz.bittrade.markets.api.lib.BitfinexPrivateApiAccessLib;
import kz.bittrade.markets.api.lib.CexAPILib;
import kz.bittrade.markets.api.lib.WexNzPrivateApiAccessLib;
import kz.bittrade.views.parts.CoinActionsWindow;
import kz.bittrade.views.parts.CompareSeriesChart;
import kz.bittrade.views.parts.MainGrid;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.vaadin.addons.stackpanel.StackPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import static com.vaadin.ui.Alignment.*;

@SuppressWarnings("serial")
public class MainView extends VerticalLayout implements View {
    private final MarketsRefresher marketsRefresher;
    private AppSettingsHolder settings;
    private BitTradeFx mainui;

    private List<WexNzBalanceHolder> wexNzUserBalance;
    private List<BitfinexBalanceHolder> bitfinexUserBalance;
    private List<CexIoBalanceHolder> cexioUserBalance;

    private Grid<CurrencyPairsHolder> currInfoGrid;
    private Grid<BalanceHolder> wexBalanceGrid;
    private Grid<BalanceHolder> bitBalanceGrid;
    private Grid<BalanceHolder> cexioBalanceGrid;

    private CssLayout wexBalancePanel;
    private CssLayout bitBalancePanel;
    private CssLayout cexioBalancePanel;
    private Label bitBalanceStubLabel;
    private Label wexBalanceStubLabel;
    private Label cexioBalanceStubLabel;

    private int autoRefreshTime;
    private int nextRefreshSec;

    private ProgressBar refreshProgressBar;
    private Label labelRefreshSec;
    private Label labelRefresh;

    private Button btnRefreshTable;
    private Button btnSettings;
    private Button btnRefreshUserBalance;

    private CheckBox chkAutoRefresh;
    private Timer timer;
    private StackPanel topStackPanel;
    private StackPanel middleStackPanel;
    private StackPanel bottomStackPanel;

    private VerticalLayout waitingStubPanel;
    private CoinActionsWindow coinActionsWindow;

    private MenuBar coinSelect;
    private CompareSeriesChart compareSeriesChart;

    public MainView() {
        addStyleName("content-common");

        mainui = (BitTradeFx) UI.getCurrent();
        settings = mainui.getSettings();

        wexNzUserBalance = new ArrayList<>();
        bitfinexUserBalance = new ArrayList<>();
        cexioUserBalance = new ArrayList<>();

        refreshProgressBar = new ProgressBar(0.0f);
        refreshProgressBar.setVisible(false);

        labelRefresh = new Label("");
        labelRefresh.setVisible(false);
        labelRefreshSec = new Label("");

        bitBalanceStubLabel = getBalanceStubLabel();
        wexBalanceStubLabel = getBalanceStubLabel();
        cexioBalanceStubLabel = getBalanceStubLabel();

        coinActionsWindow = new CoinActionsWindow();

        marketsRefresher = MarketsRefresher.getInstance();

        btnRefreshTable = new Button("Refresh all");
        btnRefreshTable.addClickListener(e -> mainui.refreshCurrencyGrid(null));
        btnRefreshTable.addStyleName(ValoTheme.BUTTON_FRIENDLY);

        btnSettings = new Button("Settings");
        btnSettings.addClickListener(
                e -> getUI().getNavigator().navigateTo(BFConstants.SETTINGS_VIEW));

        btnRefreshUserBalance = new Button("Refresh all");
        btnRefreshUserBalance.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        btnRefreshUserBalance.addClickListener(
                e -> updateUserBalances());

        chkAutoRefresh = new CheckBox("Auto refresh every 0 sec");
        chkAutoRefresh.addValueChangeListener(event -> {
            boolean notChecked = !chkAutoRefresh.getValue();
            btnRefreshTable.setEnabled(notChecked);
            btnSettings.setEnabled(notChecked);
            if (notChecked) {
                timer.cancel();
                labelRefreshSec.setValue("");
                mainui.showNotification("Timer", "Auto-update disabled by user.", 3000, Position.BOTTOM_RIGHT, "tray dark");
            } else {
                nextRefreshSec = 0;
                timer.scheduleRepeatable(1000);
            }
        });

        wexBalanceGrid = initBalanceGrids(wexNzUserBalance);
        bitBalanceGrid = initBalanceGrids(bitfinexUserBalance);
        cexioBalanceGrid = initBalanceGrids(cexioUserBalance);

        currInfoGrid = new MainGrid(mainui, coinActionsWindow);

        VerticalLayout wexBalanceVerticalStub = new VerticalLayout();
        wexBalanceVerticalStub.setSpacing(true);
        wexBalanceVerticalStub.addComponent(wexBalanceGrid);
        wexBalanceVerticalStub.addComponent(wexBalanceStubLabel);
        wexBalanceVerticalStub.setComponentAlignment(wexBalanceStubLabel, MIDDLE_CENTER);

        VerticalLayout bitBalanceVerticalStub = new VerticalLayout();
        bitBalanceVerticalStub.setSpacing(true);
        bitBalanceVerticalStub.addComponent(bitBalanceGrid);
        bitBalanceVerticalStub.addComponent(bitBalanceStubLabel);
        bitBalanceVerticalStub.setComponentAlignment(bitBalanceStubLabel, MIDDLE_CENTER);

        VerticalLayout cexioBalanceVerticalStub = new VerticalLayout();
        cexioBalanceVerticalStub.setSpacing(true);
        cexioBalanceVerticalStub.addComponent(cexioBalanceGrid);
        cexioBalanceVerticalStub.addComponent(cexioBalanceStubLabel);
        cexioBalanceVerticalStub.setComponentAlignment(cexioBalanceStubLabel, MIDDLE_CENTER);

        Button btnWexBalanceRefresh = getRefreshMiniButton((Button.ClickListener) clickEvent -> updateWexBalance());
        Button btnWexBalanceClear = getClearMiniButton((Button.ClickListener) clickEvent -> hideBalancePanel(wexNzUserBalance, wexBalanceGrid, wexBalanceStubLabel));
        HorizontalLayout wexBalancePanelCaption = getPanelCaptionComponents(btnWexBalanceRefresh, btnWexBalanceClear, BFConstants.WEX);

        Button btnBitBalanceRefresh = getRefreshMiniButton((Button.ClickListener) clickEvent -> updateBitfinexBalance());
        Button btnBitBalanceClear = getClearMiniButton((Button.ClickListener) clickEvent -> hideBalancePanel(bitfinexUserBalance, bitBalanceGrid, bitBalanceStubLabel));
        HorizontalLayout bitBalancePanelCaption = getPanelCaptionComponents(btnBitBalanceRefresh, btnBitBalanceClear, BFConstants.BITFINEX);

        Button btnCexioBalanceRefresh = getRefreshMiniButton((Button.ClickListener) clickEvent -> updateCexIoBalance());
        Button btnCexioBalanceClear = getClearMiniButton((Button.ClickListener) clickEvent -> hideBalancePanel(cexioUserBalance, cexioBalanceGrid, cexioBalanceStubLabel));
        HorizontalLayout cexioBalancePanelCaption = getPanelCaptionComponents(btnCexioBalanceRefresh, btnCexioBalanceClear, BFConstants.CEX);

        wexBalancePanel = new CssLayout();
        wexBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        wexBalancePanel.addComponent(wexBalancePanelCaption);
        wexBalancePanel.addComponent(wexBalanceVerticalStub);
        wexBalancePanel.setWidth("16.5em");

        bitBalancePanel = new CssLayout();
        bitBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        bitBalancePanel.addComponent(bitBalancePanelCaption);
        bitBalancePanel.addComponent(bitBalanceVerticalStub);
        bitBalancePanel.setWidth("16.5em");

        cexioBalancePanel = new CssLayout();
        cexioBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        cexioBalancePanel.addComponent(cexioBalancePanelCaption);
        cexioBalancePanel.addComponent(cexioBalanceVerticalStub);
        cexioBalancePanel.setWidth("16.5em");

        HorizontalLayout horizontalBalanceGrid = new HorizontalLayout();
        horizontalBalanceGrid.addComponent(wexBalancePanel);
        horizontalBalanceGrid.addComponent(bitBalancePanel);
        horizontalBalanceGrid.addComponent(cexioBalancePanel);

        GridLayout secondLayer = new GridLayout(2, 1);
        secondLayer.setWidth("100%");
        secondLayer.addComponent(btnRefreshUserBalance, 0, 0);
        secondLayer.addComponent(btnSettings, 1, 0);
        secondLayer.setComponentAlignment(btnRefreshUserBalance, MIDDLE_LEFT);
        secondLayer.setComponentAlignment(btnSettings, MIDDLE_RIGHT);

        VerticalLayout topLayer = new VerticalLayout();
        topLayer.addComponent(horizontalBalanceGrid);
        topLayer.addComponent(secondLayer);

        HorizontalLayout refreshLayer = new HorizontalLayout(labelRefresh, refreshProgressBar);
        refreshLayer.setComponentAlignment(labelRefresh, MIDDLE_LEFT);
        refreshLayer.setComponentAlignment(refreshProgressBar, MIDDLE_LEFT);

        HorizontalLayout bottomLayer = new HorizontalLayout();
        bottomLayer.addComponent(btnRefreshTable);
        bottomLayer.addComponent(chkAutoRefresh);
        bottomLayer.addComponent(labelRefreshSec);
        bottomLayer.setComponentAlignment(btnRefreshTable, MIDDLE_LEFT);
        bottomLayer.setComponentAlignment(chkAutoRefresh, MIDDLE_LEFT);
        bottomLayer.setComponentAlignment(labelRefreshSec, MIDDLE_LEFT);

        Label waitingStubLabel = new Label();
        waitingStubLabel.setValue("Waiting for local storage JS callback...");
        waitingStubLabel.addStyleName(ValoTheme.LABEL_COLORED);
        waitingStubLabel.addStyleName(ValoTheme.LABEL_H2);

        waitingStubPanel = new VerticalLayout();
        waitingStubPanel.addComponent(waitingStubLabel);
        waitingStubPanel.setWidth("100%");
        waitingStubPanel.setComponentAlignment(waitingStubLabel, MIDDLE_CENTER);

        VerticalLayout middleLayer = new VerticalLayout();
        middleLayer.addComponent(waitingStubPanel);
        middleLayer.addComponent(currInfoGrid);
        middleLayer.addComponent(refreshLayer);
        middleLayer.addComponent(bottomLayer);

        Panel topPanel = new Panel("User related information");
        topPanel.setContent(topLayer);
        topPanel.setWidth("90%");
        topPanel.setIcon(VaadinIcons.DOLLAR);
        topStackPanel = StackPanel.extend(topPanel);

        topStackPanel.addToggleListener(s -> {
            String openAtStartup = Boolean.TRUE.toString();
            if (topStackPanel.isOpen()) {
                openAtStartup = Boolean.FALSE.toString();
            }
            settings.setProperty(BFConstants.TOP_PANEL_FOLDED_AT_START, openAtStartup);
        });

        Panel middlePanel = new Panel("Coin markets monitoring");
        middlePanel.setContent(middleLayer);
        middlePanel.setWidth("90%");
        middlePanel.setIcon(VaadinIcons.CHART_GRID);
        middleStackPanel = StackPanel.extend(middlePanel);
        middleStackPanel.addToggleListener(s -> {
            String openAtStartup = Boolean.TRUE.toString();
            if (middleStackPanel.isOpen()) {
                openAtStartup = Boolean.FALSE.toString();
            }
            settings.setProperty(BFConstants.MIDDLE_PANEL_FOLDED_AT_START, openAtStartup);
        });

        compareSeriesChart = new CompareSeriesChart(settings);

        coinSelect = new MenuBar();
        coinSelect.setCaption("Select coin:");

        GridLayout horizontalChartGrid = new GridLayout(1, 1);
        horizontalChartGrid.setWidth("10%");
        horizontalChartGrid.addComponent(coinSelect, 0, 0);

        VerticalLayout underChartLayer = new VerticalLayout();
        underChartLayer.addComponent(compareSeriesChart);
        underChartLayer.addComponent(horizontalChartGrid);
        underChartLayer.setComponentAlignment(compareSeriesChart, MIDDLE_CENTER);
        underChartLayer.setComponentAlignment(horizontalChartGrid, MIDDLE_LEFT);

        Panel chartPanel = new Panel("Coin markets cumulative chart");
        chartPanel.setContent(underChartLayer);
        chartPanel.setWidth("90%");
        chartPanel.setIcon(VaadinIcons.SPLINE_AREA_CHART);
        bottomStackPanel = StackPanel.extend(chartPanel);
        bottomStackPanel.addToggleListener(s -> {
            String openAtStartup = Boolean.TRUE.toString();
            if (bottomStackPanel.isOpen()) {
                openAtStartup = Boolean.FALSE.toString();
            }
            settings.setProperty(BFConstants.BOTTOM_PANEL_FOLDED_AT_START, openAtStartup);
        });

        setSpacing(true);
        addComponent(topPanel);
        addComponent(middlePanel);
        addComponent(chartPanel);
        setComponentAlignment(topPanel, Alignment.TOP_CENTER);
        setComponentAlignment(middlePanel, Alignment.MIDDLE_CENTER);
        setComponentAlignment(chartPanel, Alignment.BOTTOM_CENTER);

        initMainGridAutoRefreshTimer();
    }

    private Grid<BalanceHolder> initBalanceGrids(List userBalance) {
        Grid<BalanceHolder> balanceGrid = new Grid<>();
        balanceGrid.setSelectionMode(Grid.SelectionMode.NONE);
        balanceGrid.setCaption("Total balance");
        balanceGrid.setItems(userBalance);
        balanceGrid.addColumn(BalanceHolder::getCurrencyName, new HtmlRenderer())
                .setCaption("Currency")
                .setResizable(false)
                .setWidth(100);
        balanceGrid.addColumn(new ValueProvider<BalanceHolder, String>() {
            @Override
            public String apply(BalanceHolder balanceHolder) {
                return balanceHolder.getAmount();
            }
        }, new HtmlRenderer())
                .setCaption("Balance")
                .setResizable(false);
        balanceGrid.setStyleName(ValoTheme.TABLE_SMALL);
        balanceGrid.setWidth("15em");
        balanceGrid.setHeightByRows(1);
        balanceGrid.setVisible(false);

        return balanceGrid;
    }

    private Label getBalanceStubLabel() {
        Label stubLabel = new Label();
        stubLabel.setValue("Click refresh to show info");
        stubLabel.addStyleName(ValoTheme.LABEL_COLORED);
        stubLabel.addStyleName(ValoTheme.LABEL_LIGHT);
        stubLabel.addStyleName(ValoTheme.LABEL_H4);
        return stubLabel;
    }

    private HorizontalLayout getPanelCaptionComponents(Button btnBalanceRefresh, Button btnBalanceClear, String caption) {
        HorizontalLayout bitBalancePanelCaption = new HorizontalLayout();
        bitBalancePanelCaption.addStyleName("v-panel-caption");
        bitBalancePanelCaption.setWidth("100%");
        Label labelb = new Label(caption);
        bitBalancePanelCaption.addComponent(labelb);
        bitBalancePanelCaption.setExpandRatio(labelb, 1);
        bitBalancePanelCaption.addComponent(btnBalanceClear);
        bitBalancePanelCaption.addComponent(btnBalanceRefresh);
        return bitBalancePanelCaption;
    }

    private Button getRefreshMiniButton(Button.ClickListener clickListener) {
        Button btnRefresh = getMiniButton(clickListener);
        btnRefresh.setIcon(VaadinIcons.REFRESH);
        btnRefresh.setDescription("Refresh");
        return btnRefresh;
    }

    private Button getClearMiniButton(Button.ClickListener clickListener) {
        Button btnClear = getMiniButton(clickListener);
        btnClear.setIcon(VaadinIcons.EYE_SLASH);
        btnClear.setDescription("Clear");
        return btnClear;
    }

    private Button getMiniButton(Button.ClickListener clickListener) {
        Button btnClear = new Button();
        btnClear.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        btnClear.addStyleName(ValoTheme.BUTTON_SMALL);
        btnClear.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnClear.addClickListener(clickListener);
        return btnClear;
    }

    private void initMainGridAutoRefreshTimer() {
        timer = new Timer();
        addExtension(timer);
        timer.run(() -> {
            if (nextRefreshSec >= autoRefreshTime) {
                mainui.refreshCurrencyGrid(null);
                nextRefreshSec = 0;
                labelRefreshSec.setValue("");
            } else {
                nextRefreshSec++;
                labelRefreshSec.setValue("(next in: " + String.valueOf(autoRefreshTime - nextRefreshSec) + ")");
            }
        });
    }

    public ProgressBar getRefreshProgressBar() {
        return refreshProgressBar;
    }

    public Label getLabelRefresh() {
        return labelRefresh;
    }

    public Grid<CurrencyPairsHolder> getCurrInfoGrid() {
        return currInfoGrid;
    }

    private void updateUserBalances() {
        new Thread(() -> {
            if (settings.isPropertyEnabled(BFConstants.WEX))
                updateWexBalance();
        }).start();

        new Thread(() -> {
            if (settings.isPropertyEnabled(BFConstants.BITFINEX))
                updateBitfinexBalance();
        }).start();

        new Thread(() -> {
            if (settings.isPropertyEnabled(BFConstants.CEX))
                updateCexIoBalance();
        }).start();

//        try {
//            KrakenApi api = new KrakenApi();
//            api.setKey(settings.getProperty(BFConstants.KRA_API_KEY));
//            api.setSecret(settings.getProperty(BFConstants.KRA_API_SECRET));
//
//            String response = api.queryPrivate(KrakenApi.Method.BALANCE);
//            System.out.println(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void updateCexIoBalance() {
        if (isCexIoAPIKeyPresent()) {
            CexAPILib privateLib = new CexAPILib(settings.getProperty(BFConstants.CEX_API_USERNAME), settings.getProperty(BFConstants.CEX_API_KEY),
                    settings.getProperty(BFConstants.CEX_API_SECRET));

            cexioUserBalance.clear();
            cexioBalanceGrid.setVisible(true);
            cexioBalanceGrid.setHeightByRows(1);
            cexioBalanceGrid.setComponentError(null);
            cexioBalanceStubLabel.setVisible(false);
            String result = privateLib.balance();

            if (result != null) {
                CexIoBalance cexIoBalance = new Gson().fromJson(result, CexIoBalance.class);
                if (cexIoBalance != null) {
                    if (cexIoBalance.getError() == null) {
                        double amount = cexIoBalance.getUSD().getAvailable();
                        if (amount > 0) cexioUserBalance.add(new CexIoBalanceHolder(BFConstants.USD, amount));

                        amount = cexIoBalance.getBTC().getAvailable();
                        if (amount > 0) cexioUserBalance.add(new CexIoBalanceHolder(BFConstants.BITCOIN, amount));

                        amount = cexIoBalance.getBCH().getAvailable();
                        if (amount > 0) cexioUserBalance.add(new CexIoBalanceHolder(BFConstants.BITCOIN_CASH, amount));

                        amount = cexIoBalance.getETH().getAvailable();
                        if (amount > 0) cexioUserBalance.add(new CexIoBalanceHolder(BFConstants.ETHERIUM_COIN, amount));

                        amount = cexIoBalance.getZEC().getAvailable();
                        if (amount > 0) cexioUserBalance.add(new CexIoBalanceHolder(BFConstants.ZCASH_COIN, amount));

                        amount = cexIoBalance.getDASH().getAvailable();
                        if (amount > 0) cexioUserBalance.add(new CexIoBalanceHolder(BFConstants.DASH_COIN, amount));

                        amount = cexIoBalance.getXRP().getAvailable();
                        if (amount > 0) cexioUserBalance.add(new CexIoBalanceHolder(BFConstants.RIPPLE_COIN, amount));

                        cexioBalanceGrid.setCaption("Updated @" + cexIoBalance.getTimestamp());

                        if (cexioUserBalance.size() > 0) {
                            cexioBalanceGrid.setHeightByRows(cexioUserBalance.size());
                        } else {
                            cexioBalanceGrid.setHeightByRows(1);
                            cexioUserBalance.add(new CexIoBalanceHolder("Total balance", 0.0));
                        }
                    } else {
                        setCexIoLabelsError(cexIoBalance.getError());
                    }
                } else setCexIoLabelsError("JSON parse failed");
            } else setCexIoLabelsError("Request failed");
            cexioBalanceGrid.getDataProvider().refreshAll();
        }
    }

    private void setCexIoLabelsError(String errorText) {
        cexioBalanceGrid.setComponentError(new UserError(errorText));
    }

    private void updateBitfinexBalance() {
        if (isBitAPIKeyPresent()) {
            BitfinexPrivateApiAccessLib bitLib = new BitfinexPrivateApiAccessLib(settings.getProperty(BFConstants.BIT_API_KEY),
                    settings.getProperty(BFConstants.BIT_API_SECRET));
            bitfinexUserBalance.clear();
            bitBalanceGrid.setVisible(true);
            bitBalanceGrid.setHeightByRows(1);
            bitBalanceGrid.setComponentError(null);
            bitBalanceStubLabel.setVisible(false);

            try {
                String result = bitLib.sendRequestV1Balances();
                if (result != null) {
                    JSONArray json = new JSONArray(result);
                    BitfinexBalancesList bitfinexBalancesList = new BitfinexBalancesList();
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject obj = json.getJSONObject(i);
                        BitfinexBalance bitfinexBalance = new Gson().fromJson(obj.toString(), BitfinexBalance.class);
                        bitfinexBalancesList.add(bitfinexBalance);
                    }

                    if (bitfinexBalancesList.getBalancesMap().size() > 0) {
                        double amount = bitfinexBalancesList.getAvailUsd();
                        if (amount > 0) bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.USD, amount));

                        amount = bitfinexBalancesList.getAvailBtc();
                        if (amount > 0) bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.BITCOIN, amount));

                        amount = bitfinexBalancesList.getAvailBch();
                        if (amount > 0)
                            bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.BITCOIN_CASH, amount));

                        amount = bitfinexBalancesList.getAvailEth();
                        if (amount > 0)
                            bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.ETHERIUM_COIN, amount));

                        amount = bitfinexBalancesList.getAvailLtc();
                        if (amount > 0)
                            bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.LITECOIN, amount));

                        amount = bitfinexBalancesList.getAvailZec();
                        if (amount > 0)
                            bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.ZCASH_COIN, bitfinexBalancesList.getAvailZec()));

                        amount = bitfinexBalancesList.getAvailDsh();
                        if (amount > 0)
                            bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.DASH_COIN, bitfinexBalancesList.getAvailDsh()));

                        bitBalanceGrid.setCaption("Updated @" + settings.getNowString());
                        if (bitfinexUserBalance.size() > 0) {
                            bitBalanceGrid.setHeightByRows(bitfinexUserBalance.size());
                        } else {
                            bitBalanceGrid.setHeightByRows(1);
                            bitfinexUserBalance.add(new BitfinexBalanceHolder("Total balance", 0.0));
                        }
                    } else setBitLabelsError("JSON parse failed");
                } else {
                    setBitLabelsError("Request error");
                }
            } catch (Exception e) {
                setBitLabelsError("Request error");
                e.printStackTrace();
            }
            bitBalanceGrid.getDataProvider().refreshAll();
        }
    }

    private void setBitLabelsError(String errorText) {
        bitBalanceGrid.setComponentError(new UserError(errorText));
    }

    private void updateWexBalance() {
        if (isWexAPIKeyPresent()) {
            WexNzPrivateApiAccessLib privateLib = new WexNzPrivateApiAccessLib(settings.getProperty(BFConstants.WEX_API_KEY),
                    settings.getProperty(BFConstants.WEX_API_SECRET));

            wexNzUserBalance.clear();
            wexBalanceGrid.setVisible(true);
            wexBalanceGrid.setHeightByRows(1);
            wexBalanceGrid.setComponentError(null);
            wexBalanceStubLabel.setVisible(false);

            ArrayList<NameValuePair> postData = new ArrayList<>();
            postData.add(new BasicNameValuePair("method", "getInfo"));
            privateLib.setPrivateUrl(BFConstants.WEX_API_PRIVATE_URL);

            JsonObject result = privateLib.performAuthorizedRequest(postData);
            if (result != null) {
                WexNzGetInfo wexUserInfo = new Gson().fromJson(result, WexNzGetInfo.class);
                if (wexUserInfo != null) {
                    if (wexUserInfo.getSuccess() == 1) {
                        double amount = wexUserInfo.getInfo().getFunds().getUsd();
                        if (amount > 0) wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.USD, amount));

                        amount = wexUserInfo.getInfo().getFunds().getBtc();
                        if (amount > 0) wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.BITCOIN, amount));

                        amount = wexUserInfo.getInfo().getFunds().getBch();
                        if (amount > 0) wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.BITCOIN_CASH, amount));

                        amount = wexUserInfo.getInfo().getFunds().getEth();
                        if (amount > 0) wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.ETHERIUM_COIN, amount));

                        amount = wexUserInfo.getInfo().getFunds().getLtc();
                        if (amount > 0) wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.LITECOIN, amount));

                        amount = wexUserInfo.getInfo().getFunds().getZec();
                        if (amount > 0) wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.ZCASH_COIN, amount));

                        amount = wexUserInfo.getInfo().getFunds().getDsh();
                        if (amount > 0) wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.DASH_COIN, amount));

                        wexBalanceGrid.setCaption("Updated @" + wexUserInfo.getInfo().getTimestamp());

                        if (wexNzUserBalance.size() > 0) {
                            wexBalanceGrid.setHeightByRows(wexNzUserBalance.size());
                        } else {
                            wexBalanceGrid.setHeightByRows(1);
                            wexNzUserBalance.add(new WexNzBalanceHolder("Total balance", 0.0));
                        }
                    } else {
                        setWexLabelsError(wexUserInfo.getError());
                    }
                } else setWexLabelsError("JSON parse failed");
            } else setWexLabelsError("Request failed");
            wexBalanceGrid.getDataProvider().refreshAll();
        }
    }

    private void setWexLabelsError(String errorText) {
        wexBalanceGrid.setComponentError(new UserError(errorText));
    }

    private void hideBalancePanel(List balanceHolderList, Grid balanceHolderGrid, Label balanceStubLabel) {
        balanceHolderList.clear();
        balanceHolderGrid.setVisible(false);
        balanceHolderGrid.setHeightByRows(1);
        balanceStubLabel.setVisible(true);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        smartInitCurrencyPairs();
        initMarketColumns();
        initBalanceStubLabels();
        clearChartCoinSelection();
    }

    public void initBalanceStubLabels() {
        initBalanceStubLabel(wexBalanceStubLabel, isWexAPIKeyPresent());
        initBalanceStubLabel(bitBalanceStubLabel, isBitAPIKeyPresent());
        initBalanceStubLabel(cexioBalanceStubLabel, isCexIoAPIKeyPresent());
    }

    private void initBalanceStubLabel(Label label, boolean keyPresent) {
        if (keyPresent) {
            label.setValue("Click refresh to show info");
            label.setComponentError(null);
        } else {
            label.setValue("Set api key to get info");
            label.setComponentError(new UserError("key is absent"));
        }
    }

    private boolean isWexAPIKeyPresent() {
        return !settings.getProperty(BFConstants.WEX_API_KEY).equals("");
    }

    private boolean isBitAPIKeyPresent() {
        return !settings.getProperty(BFConstants.BIT_API_KEY).equals("");
    }

    private boolean isCexIoAPIKeyPresent() {
        return !settings.getProperty(BFConstants.CEX_API_KEY).equals("");
    }

    public void initMarketColumns() {
        boolean wexHidden = !settings.isPropertyEnabled(BFConstants.WEX);
        currInfoGrid.getColumn(BFConstants.GRID_WEX_COLUMN).setHidden(wexHidden);
        wexBalancePanel.setVisible(!wexHidden);

        boolean bitHidden = !settings.isPropertyEnabled(BFConstants.BITFINEX);
        currInfoGrid.getColumn(BFConstants.GRID_BITFINEX_COLUMN).setHidden(bitHidden);
        bitBalancePanel.setVisible(!bitHidden);

        boolean cexioHidden = !settings.isPropertyEnabled(BFConstants.CEX);
        currInfoGrid.getColumn(BFConstants.GRID_CEX_COLUMN).setHidden(!settings.isPropertyEnabled(BFConstants.CEX));
        cexioBalancePanel.setVisible(!cexioHidden);

        currInfoGrid.getColumn(BFConstants.GRID_KRAKEN_COLUMN).setHidden(!settings.isPropertyEnabled(BFConstants.KRAKEN));
    }

    private void smartInitCurrencyPairs() {
        List<CurrencyPairsHolder> currencyPairsHolderList = mainui.getCurrencyPairsHolderList();
        HashMap<String, Boolean> map = settings.getCoinSelectStateMap();

        /* Remove section */
        currencyPairsHolderList.removeIf(new Predicate<CurrencyPairsHolder>() {
            @Override
            public boolean test(CurrencyPairsHolder pairHolder) {
                return map.get(pairHolder.getName()).equals(Boolean.FALSE);
            }
        });

        /* Add section */
        for (String name : map.keySet()) {
            if (!containsName(currencyPairsHolderList, name) && map.get(name).equals(Boolean.TRUE)) {
                currencyPairsHolderList.add(marketsRefresher.initNewCurrencyPair(name));
            }
        }

        /* Refresh section */
        setMainGridCorrectRowCount();
    }

    public void setMainGridCorrectRowCount() {
        int marketsCount = Collections.frequency(settings.getEnabledMarketsMap().values(), Boolean.TRUE);
        int cphSize = Collections.frequency(settings.getCoinSelectStateMap().values(), Boolean.TRUE);

        boolean emptyTable = (cphSize == 0 || marketsCount == 0);

        if (!emptyTable) {
            labelRefresh.removeStyleName(ValoTheme.LABEL_FAILURE);
            labelRefresh.setValue("");
        } else {
            labelRefresh.setValue("At least one Coin and Market must be selected for monitoring! (check settings)");
            labelRefresh.addStyleName(ValoTheme.LABEL_FAILURE);
        }
        if (cphSize > 0) currInfoGrid.setHeightByRows(cphSize);
        else currInfoGrid.setHeightByRows(1);

        if (marketsCount == 0) currInfoGrid.setVisible(false);
        else currInfoGrid.setVisible(true);

        btnRefreshTable.setEnabled(!emptyTable);
        chkAutoRefresh.setEnabled(!emptyTable);
        btnRefreshUserBalance.setEnabled(marketsCount > 0);
        currInfoGrid.getDataProvider().refreshAll();
    }

    private boolean containsName(final List<CurrencyPairsHolder> list, final String name) {
        return list.stream().map(CurrencyPairsHolder::getName).anyMatch(name::equals);
    }

    public void finishUIInit() {
        waitingStubPanel.setVisible(false);
        labelRefresh.setVisible(true);

        if (settings.isPropertyEnabled(BFConstants.TOP_PANEL_FOLDED_AT_START))
            topStackPanel.close();

        if (settings.isPropertyEnabled(BFConstants.MIDDLE_PANEL_FOLDED_AT_START))
            middleStackPanel.close();

        if (settings.isPropertyEnabled(BFConstants.BOTTOM_PANEL_FOLDED_AT_START))
            bottomStackPanel.close();
    }

    public Button getBtnSettings() {
        return btnSettings;
    }

    public void setAutoRefreshTime(int autoRefreshTime) {
        this.autoRefreshTime = autoRefreshTime;
        chkAutoRefresh.setCaption("Auto refresh every " + autoRefreshTime + " sec");
    }

    public void prepareChart() {
        MenuBar.Command typeCommand = (MenuBar.Command) selectedItem -> {
            compareSeriesChart.refreshDataByCoin(BFConstants.getCoinIdByName(selectedItem.getText()));

            for (MenuBar.MenuItem item : coinSelect.getItems()) {
                item.setChecked(false);
            }
            selectedItem.setChecked(true);
        };

        if (settings.isPropertyEnabled(BFConstants.BITCOIN)) {
            MenuBar.MenuItem menuItemBTC = coinSelect.addItem(BFConstants.BITCOIN, typeCommand);
            menuItemBTC.setCheckable(true);
            menuItemBTC.setChecked(true);
        }
        if (settings.isPropertyEnabled(BFConstants.BITCOIN_CASH))
            coinSelect.addItem(BFConstants.BITCOIN_CASH, typeCommand).setCheckable(true);
        if (settings.isPropertyEnabled(BFConstants.ETHERIUM_COIN))
            coinSelect.addItem(BFConstants.ETHERIUM_COIN, typeCommand).setCheckable(true);
        if (settings.isPropertyEnabled(BFConstants.ZCASH_COIN))
            coinSelect.addItem(BFConstants.ZCASH_COIN, typeCommand).setCheckable(true);
        if (settings.isPropertyEnabled(BFConstants.LITECOIN))
            coinSelect.addItem(BFConstants.LITECOIN, typeCommand).setCheckable(true);
        if (settings.isPropertyEnabled(BFConstants.DASH_COIN))
            coinSelect.addItem(BFConstants.DASH_COIN, typeCommand).setCheckable(true);
        if (settings.isPropertyEnabled(BFConstants.RIPPLE_COIN))
            coinSelect.addItem(BFConstants.RIPPLE_COIN, typeCommand).setCheckable(true);

        clearChartCoinSelection();
    }

    private void clearChartCoinSelection() {
        List<MenuBar.MenuItem> coinSelectItems = coinSelect.getItems();
        for (MenuBar.MenuItem item : coinSelectItems) {
            item.setChecked(false);
        }
        if (coinSelectItems.iterator().hasNext()) {
            MenuBar.MenuItem first = coinSelectItems.iterator().next();
            if (first != null) {
                first.setChecked(true);
                compareSeriesChart.refreshDataByCoin(BFConstants.getCoinIdByName(first.getText()));
            }
        }
    }

}