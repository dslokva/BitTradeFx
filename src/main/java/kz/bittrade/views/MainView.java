package kz.bittrade.views;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.SerializableComparator;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import kz.bittrade.BitTradeFx;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;
import kz.bittrade.markets.api.holders.user.WexNzGetInfo;
import kz.bittrade.markets.api.lib.BitfinexPrivateApiAccessLib;
import kz.bittrade.markets.api.lib.WexNzPrivateApiAccessLib;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.vaadin.ui.Alignment.MIDDLE_LEFT;
import static com.vaadin.ui.Alignment.MIDDLE_RIGHT;

public class MainView extends VerticalLayout implements View {
    private static AppSettingsHolder settings;
    private BitTradeFx mainui;

    private Grid<CurrencyPairsHolder> currInfoGrid;
    private ProgressBar refreshProgressBar;
    private Label labelRefreshSec;
    private Label labelRefresh;
    private Timer timer;
    private int refreshSec;

    private Label labelWexTotalUSD;
    private Label labelWexTotalBTC;
    private Label labelWexTotalBCH;
    private Label labelWexTotalETH;
    private Label labelWexTotalLTC;
    private Label labelWexTotalZEC;
    private Label labelWexTotalDSH;

    private Label labelBitTotalUSD;
    private Label labelBitTotalBTC;
    private Label labelBitTotalBCH;
    private Label labelBitTotalETH;
    private Label labelBitTotalLTC;
    private Label labelBitTotalZEC;
    private Label labelBitTotalDSH;

    public MainView() {
        addStyleName("content-common");

        settings = AppSettingsHolder.getInstance();
        mainui = (BitTradeFx) UI.getCurrent();

        labelWexTotalUSD = new Label("0", ContentMode.HTML);
        labelWexTotalBTC = new Label("0", ContentMode.HTML);
        labelWexTotalETH = new Label("0", ContentMode.HTML);
        labelWexTotalBCH = new Label("0", ContentMode.HTML);
        labelWexTotalLTC = new Label("0", ContentMode.HTML);
        labelWexTotalZEC = new Label("0", ContentMode.HTML);
        labelWexTotalDSH = new Label("0", ContentMode.HTML);

        labelBitTotalUSD = new Label("0", ContentMode.HTML);
        labelBitTotalBTC = new Label("0", ContentMode.HTML);
        labelBitTotalETH = new Label("0", ContentMode.HTML);
        labelBitTotalBCH = new Label("0", ContentMode.HTML);
        labelBitTotalLTC = new Label("0", ContentMode.HTML);
        labelBitTotalZEC = new Label("0", ContentMode.HTML);
        labelBitTotalDSH = new Label("0", ContentMode.HTML);

        refreshProgressBar = new ProgressBar(0.0f);
        refreshProgressBar.setVisible(false);

        labelRefresh = new Label("");
        labelRefreshSec = new Label("");

        Button btnRefreshTable = new Button("Refresh all");
        btnRefreshTable.addClickListener(
                e -> {
                    labelRefresh.setValue("Refreshing, please wait");
                    refreshProgressBar.setVisible(true);
                    mainui.refreshCurrencyGrid();
                });
        btnRefreshTable.addStyleName(ValoTheme.BUTTON_FRIENDLY);

        Button btnSettings = new Button("API settings");
        btnSettings.addClickListener(
                e -> getUI().getNavigator().navigateTo("settings"));

        Button btnRefreshUserInfo = new Button("Refresh all");
        btnRefreshUserInfo.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        btnRefreshUserInfo.addClickListener(
                e -> updateUserBalances());

        CheckBox chkAutoRefresh = new CheckBox("Auto refresh every 30 sec");
        chkAutoRefresh.addValueChangeListener(event -> {
            boolean notChecked = !chkAutoRefresh.getValue();

            btnRefreshTable.setEnabled(notChecked);
            if (notChecked) {
                timer.cancel();
                labelRefreshSec.setValue("");
                mainui.showNotification("Timer", "Auto-update disabled by user.", 3000, Position.BOTTOM_RIGHT, "tray dark");
            } else {
                refreshSec = 0;
                timer.scheduleRepeatable(1000);
            }
        });

        currInfoGrid = new Grid<>();
        currInfoGrid.setSelectionMode(Grid.SelectionMode.NONE);
        currInfoGrid.setCaption("Currency information");
        currInfoGrid.setItems(mainui.getCurrencyPairsHolderList());

        currInfoGrid.addColumn(CurrencyPairsHolder::getName, new HtmlRenderer()).setCaption("Pair name")
                .setWidth(150)
                .setResizable(false);
        currInfoGrid.addColumn(CurrencyPairsHolder::getDeltaString, new HtmlRenderer())
                .setCaption("Delta $")
                .setWidth(110);
        currInfoGrid.addColumn(CurrencyPairsHolder::getDeltaStringPercent, new HtmlRenderer())
                .setCaption("Delta %")
                .setWidth(110)
                .setComparator(
                        new SerializableComparator<CurrencyPairsHolder>() {
                            @Override
                            public int compare(CurrencyPairsHolder a, CurrencyPairsHolder b) {
                                return Double.compare(a.getDeltaDoublePercent(), b.getDeltaDoublePercent());
                            }
                        }
                );
        currInfoGrid.addColumn(CurrencyPairsHolder::getLastPriceWex, new HtmlRenderer())
                .setCaption("Trade value WEX.nz")
                .setWidth(180);
        currInfoGrid.addColumn(CurrencyPairsHolder::getLastPriceBitfinex, new HtmlRenderer())
                .setCaption("Trade value Bitfinex.com")
                .setWidth(220);
        currInfoGrid.addColumn(CurrencyPairsHolder::getLastPriceKraken, new HtmlRenderer())
                .setCaption("Trade value Kraken.com")
                .setWidth(220);

        currInfoGrid.addColumn(CurrencyPairsHolder -> "Update row",
                new ButtonRenderer(clickEvent -> {
                    mainui.refreshCurrencyInfo((CurrencyPairsHolder) clickEvent.getItem());
                    currInfoGrid.getDataProvider().refreshAll();
                    labelRefresh.setValue("Partially updated at: " + settings.getNowString());
                })).setCaption("Actions");

        currInfoGrid.setWidth("100%");
        currInfoGrid.setHeightByRows(mainui.getCurrencyPairsHolderList().size());

        HorizontalLayout wexUSDLabels = new HorizontalLayout(new Label("Total USD: "), labelWexTotalUSD);
        HorizontalLayout wexBTCLabels = new HorizontalLayout(new Label("Total BTC: "), labelWexTotalBTC);
        HorizontalLayout wexBCHLabels = new HorizontalLayout(new Label("Total BCH: "), labelWexTotalBCH);
        HorizontalLayout wexETHLabels = new HorizontalLayout(new Label("Total ETH: "), labelWexTotalETH);
        HorizontalLayout wexLTCLabels = new HorizontalLayout(new Label("Total LTC: "), labelWexTotalLTC);
        HorizontalLayout wexZECLabels = new HorizontalLayout(new Label("Total ZEC: "), labelWexTotalZEC);
        HorizontalLayout wexDSHLabels = new HorizontalLayout(new Label("Total DSH: "), labelWexTotalDSH);

        VerticalLayout wexBalanceVertical = new VerticalLayout();
        wexBalanceVertical.setSpacing(true);
        wexBalanceVertical.addComponent(wexUSDLabels);
        wexBalanceVertical.addComponent(wexBTCLabels);
        wexBalanceVertical.addComponent(wexBCHLabels);
        wexBalanceVertical.addComponent(wexETHLabels);
        wexBalanceVertical.addComponent(wexLTCLabels);
        wexBalanceVertical.addComponent(wexZECLabels);
        wexBalanceVertical.addComponent(wexDSHLabels);

        HorizontalLayout bitUSDLabels = new HorizontalLayout(new Label("Total USD: "), labelBitTotalUSD);
        HorizontalLayout bitBTCLabels = new HorizontalLayout(new Label("Total BTC: "), labelBitTotalBTC);
        HorizontalLayout bitBCHLabels = new HorizontalLayout(new Label("Total BCH: "), labelBitTotalBCH);
        HorizontalLayout bitETHLabels = new HorizontalLayout(new Label("Total ETH: "), labelBitTotalETH);
        HorizontalLayout bitLTCLabels = new HorizontalLayout(new Label("Total LTC: "), labelBitTotalLTC);
        HorizontalLayout bitZECLabels = new HorizontalLayout(new Label("Total ZEC: "), labelBitTotalZEC);
        HorizontalLayout bitDSHLabels = new HorizontalLayout(new Label("Total DSH: "), labelBitTotalDSH);

        VerticalLayout bitBalanceVertical = new VerticalLayout();
        bitBalanceVertical.setSpacing(true);
        bitBalanceVertical.addComponent(bitUSDLabels);
        bitBalanceVertical.addComponent(bitBTCLabels);
        bitBalanceVertical.addComponent(bitBCHLabels);
        bitBalanceVertical.addComponent(bitETHLabels);
        bitBalanceVertical.addComponent(bitLTCLabels);
        bitBalanceVertical.addComponent(bitZECLabels);
        bitBalanceVertical.addComponent(bitDSHLabels);

        Button btnWexBalanceRefresh = new Button();
        btnWexBalanceRefresh.setIcon(VaadinIcons.REFRESH);
        btnWexBalanceRefresh.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        btnWexBalanceRefresh.addStyleName(ValoTheme.BUTTON_SMALL);
        btnWexBalanceRefresh.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnWexBalanceRefresh.addClickListener((Button.ClickListener) clickEvent -> updateWexBalance());

        Button btnBitBalanceRefresh = new Button();
        btnBitBalanceRefresh.setIcon(VaadinIcons.REFRESH);
        btnBitBalanceRefresh.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        btnBitBalanceRefresh.addStyleName(ValoTheme.BUTTON_SMALL);
        btnBitBalanceRefresh.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnBitBalanceRefresh.addClickListener((Button.ClickListener) clickEvent -> updateBitfinexBalance());

        HorizontalLayout wexBalancePanelCaption = new HorizontalLayout();
        wexBalancePanelCaption.addStyleName("v-panel-caption");
        wexBalancePanelCaption.setWidth("100%");
        Label labelw = new Label("WEX");
        wexBalancePanelCaption.addComponent(labelw);
        wexBalancePanelCaption.setExpandRatio(labelw, 1);
        wexBalancePanelCaption.addComponent(btnWexBalanceRefresh);

        HorizontalLayout bitBalancePanelCaption = new HorizontalLayout();
        bitBalancePanelCaption.addStyleName("v-panel-caption");
        bitBalancePanelCaption.setWidth("100%");
        Label labelb = new Label("Bitfinex");
        bitBalancePanelCaption.addComponent(labelb);
        bitBalancePanelCaption.setExpandRatio(labelb, 1);
        bitBalancePanelCaption.addComponent(btnBitBalanceRefresh);

        CssLayout wexBalancePanel = new CssLayout();
        wexBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        wexBalancePanel.addComponent(wexBalancePanelCaption);
        wexBalancePanel.addComponent(wexBalanceVertical);
        wexBalancePanel.setWidth("14em");

        CssLayout bitBalancePanel = new CssLayout();
        bitBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        bitBalancePanel.addComponent(bitBalancePanelCaption);
        bitBalancePanel.addComponent(bitBalanceVertical);
        bitBalancePanel.setWidth("14em");

        GridLayout horizontalBalanceGrid = new GridLayout(3, 1);
        horizontalBalanceGrid.addComponent(wexBalancePanel, 0, 0);
        horizontalBalanceGrid.addComponent(bitBalancePanel, 1, 0);
        horizontalBalanceGrid.setWidth("60%");

        GridLayout secondLayer = new GridLayout(2, 1);
        secondLayer.setWidth("100%");
        secondLayer.addComponent(btnRefreshUserInfo, 0, 0);
        secondLayer.addComponent(btnSettings, 1, 0);
        secondLayer.setComponentAlignment(btnRefreshUserInfo, MIDDLE_LEFT);
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

        VerticalLayout middleLayer = new VerticalLayout();
        middleLayer.addComponent(currInfoGrid);
        middleLayer.addComponent(refreshLayer);
        middleLayer.addComponent(bottomLayer);

        Panel topPanel = new Panel("Available user balance");
        topPanel.setContent(topLayer);
        topPanel.setWidth("80%");
        topPanel.setIcon(VaadinIcons.DOLLAR);

        Panel middlePanel = new Panel("Coin markets monitoring");
        middlePanel.setContent(middleLayer);
        middlePanel.setWidth("80%");
        middlePanel.setIcon(VaadinIcons.SPLINE_AREA_CHART);

        setSpacing(true);
        addComponent(topPanel);
        addComponent(middlePanel);
        setComponentAlignment(topPanel, Alignment.TOP_CENTER);
        setComponentAlignment(middlePanel, Alignment.MIDDLE_CENTER);

        initAutoRefreshTimer();
    }

    private void initAutoRefreshTimer() {
        timer = new Timer();
        addExtension(timer);
        timer.run(() -> {
            if (refreshSec >= 30) {
                mainui.refreshCurrencyGrid();
                refreshSec = 0;
                labelRefreshSec.setValue("");
            } else {
                refreshSec++;
                labelRefreshSec.setValue("(next in: " + String.valueOf(30 - refreshSec) + ")");
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
        updateWexBalance();
        updateBitfinexBalance();
    }

    private void updateBitfinexBalance() {
        BitfinexPrivateApiAccessLib bitLib = new BitfinexPrivateApiAccessLib(settings.getProperty(BFConstants.BIT_API_KEY),
                settings.getProperty(BFConstants.BIT_API_SECRET));
        try {
            String reslt = bitLib.sendRequestV1Balances();
            System.out.println(reslt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateWexBalance() {
        String nonce = settings.getProperty(BFConstants.WEX_API_NONCE);
        if (Objects.equals(nonce, "")) nonce = "-1";
        int intNonce = Integer.parseInt(nonce);

        WexNzPrivateApiAccessLib privateLib = new WexNzPrivateApiAccessLib(settings.getProperty(BFConstants.WEX_API_KEY),
                settings.getProperty(BFConstants.WEX_API_SECRET), intNonce);

        ArrayList<NameValuePair> postData = new ArrayList<>();
        postData.add(new BasicNameValuePair("method", "getInfo"));
        privateLib.setPrivateUrl(BFConstants.WEX_API_PRIVATE_URL);

        JsonObject result = privateLib.performAuthorizedRequest(postData);
        if (result != null) {
            //privateLib.log("getInfo result: ".concat(result.toString()));
            WexNzGetInfo wexUserInfo = new Gson().fromJson(result, WexNzGetInfo.class);
            if (wexUserInfo != null && wexUserInfo.getSuccess() == 1) {
                labelWexTotalUSD.setValue("<b>" + String.format("%.6f", wexUserInfo.getInfo().getFunds().getUsd()) + "</b>");
                labelWexTotalBTC.setValue("<b>" + String.format("%.6f", wexUserInfo.getInfo().getFunds().getBtc()) + "</b>");
                labelWexTotalBCH.setValue("<b>" + String.format("%.6f", wexUserInfo.getInfo().getFunds().getBch()) + "</b>");
                labelWexTotalETH.setValue("<b>" + String.format("%.6f", wexUserInfo.getInfo().getFunds().getEth()) + "</b>");
                labelWexTotalLTC.setValue("<b>" + String.format("%.6f", wexUserInfo.getInfo().getFunds().getLtc()) + "</b>");
                labelWexTotalZEC.setValue("<b>" + String.format("%.6f", wexUserInfo.getInfo().getFunds().getZec()) + "</b>");
                labelWexTotalDSH.setValue("<b>" + String.format("%.6f", wexUserInfo.getInfo().getFunds().getDsh()) + "</b>");
            } else {
                labelWexTotalUSD.setValue("<b>error</b>");
                labelWexTotalBTC.setValue("<b>error</b>");
                labelWexTotalBCH.setValue("<b>error</b>");
                labelWexTotalETH.setValue("<b>error</b>");
                labelWexTotalLTC.setValue("<b>error</b>");
                labelWexTotalZEC.setValue("<b>error</b>");
                labelWexTotalDSH.setValue("<b>error</b>");
            }
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
//        PublicApiAccessLib.setBasicUrl("https://poloniex.com/");
//        PublicApiAccessLib.clearHeaders();
//        JsonObject result = PublicApiAccessLib.performBasicRequest("public", "?command=returnTicker");
//        System.out.println(result.toString());
    }


}