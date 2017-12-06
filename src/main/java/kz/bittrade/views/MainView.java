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
import kz.bittrade.markets.api.holders.user.BitfinexBalance;
import kz.bittrade.markets.api.holders.user.BitfinexBalancesList;
import kz.bittrade.markets.api.holders.user.WexNzGetInfo;
import kz.bittrade.markets.api.lib.BitfinexPrivateApiAccessLib;
import kz.bittrade.markets.api.lib.WexNzPrivateApiAccessLib;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import static com.vaadin.ui.Alignment.*;

public class MainView extends VerticalLayout implements View {
    private AppSettingsHolder settings;
    private BitTradeFx mainui;

    private Grid<CurrencyPairsHolder> currInfoGrid;
    private ProgressBar refreshProgressBar;
    private Label labelRefreshSec;
    private Label labelRefresh;
    private Button btnRefreshTable;
    private Button btnSettings;
    private CheckBox chkAutoRefresh;
    private Timer timer;
    private int refreshSec;
    private boolean initialized;

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

    private Label labelBitOrderUSD;
    private Label labelBitOrderBTC;
    private Label labelBitOrderBCH;
    private Label labelBitOrderETH;
    private Label labelBitOrderLTC;
    private Label labelBitOrderZEC;
    private Label labelBitOrderDSH;

    private VerticalLayout waitingStubPanel;

    public MainView() {
        addStyleName("content-common");

        mainui = (BitTradeFx) UI.getCurrent();
        settings = mainui.getSettings();

        initBalanceLabels();

        refreshProgressBar = new ProgressBar(0.0f);
        refreshProgressBar.setVisible(false);

        labelRefresh = new Label("");
        labelRefreshSec = new Label("");

        btnRefreshTable = new Button("Refresh all");
        btnRefreshTable.addClickListener(
                e -> {

                    mainui.refreshCurrencyGrid(null);
                });
        btnRefreshTable.addStyleName(ValoTheme.BUTTON_FRIENDLY);

        btnSettings = new Button("Settings");
        btnSettings.addClickListener(
                e -> getUI().getNavigator().navigateTo(BFConstants.SETTINGS_VIEW));

        Button btnRefreshUserInfo = new Button("Refresh all");
        btnRefreshUserInfo.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        btnRefreshUserInfo.addClickListener(
                e -> updateUserBalances());

        chkAutoRefresh = new CheckBox("Auto refresh every 30 sec");
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
        currInfoGrid.addComponentColumn(currencyPairRow -> {
            Button button = new Button("Click me!");
            button.setIcon(VaadinIcons.REFRESH);
            button.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
            button.addStyleName(ValoTheme.BUTTON_SMALL);
            button.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            button.addClickListener(click -> {
                mainui.refreshCurrencyGrid(currencyPairRow);
            });
            return button;
        }).setCaption("")
                .setWidth(60)
                .setResizable(false);

        currInfoGrid.addColumn(CurrencyPairsHolder::getDisplayName, new HtmlRenderer()).setCaption("Pair name")
                .setWidth(120)
                .setResizable(false);
        currInfoGrid.addColumn(CurrencyPairsHolder::getDeltaString, new HtmlRenderer())
                .setCaption("Delta $")
                .setWidth(100);
        currInfoGrid.addColumn(CurrencyPairsHolder::getDeltaStringPercent, new HtmlRenderer())
                .setCaption("Delta %")
                .setWidth(100)
                .setComparator(
                        new SerializableComparator<CurrencyPairsHolder>() {
                            @Override
                            public int compare(CurrencyPairsHolder a, CurrencyPairsHolder b) {
                                return Double.compare(a.getDeltaDoublePercent(), b.getDeltaDoublePercent());
                            }
                        }
                );
        currInfoGrid.addColumn(CurrencyPairsHolder::getLastPriceWex, new HtmlRenderer())
                .setCaption(BFConstants.WEX)
                .setWidth(138)
                .setId(BFConstants.WEX_GRID_COLUMN);
        currInfoGrid.addColumn(CurrencyPairsHolder::getLastPriceBitfinex, new HtmlRenderer())
                .setCaption(BFConstants.BITFINEX)
                .setWidth(138)
                .setId(BFConstants.BITFINEX_GRID_COLUMN);
        currInfoGrid.addColumn(CurrencyPairsHolder::getLastPriceKraken, new HtmlRenderer())
                .setCaption(BFConstants.KRAKEN)
                .setWidth(138)
                .setId(BFConstants.KRAKEN_GRID_COLUMN);
        currInfoGrid.addColumn(CurrencyPairsHolder::getLastPriceCex, new HtmlRenderer())
                .setCaption(BFConstants.CEX)
                .setWidth(138)
                .setId(BFConstants.CEX_GRID_COLUMN);

        currInfoGrid.addColumn(CurrencyPairsHolder -> "Calculate",
                new ButtonRenderer(clickEvent -> {
//                    mainui.refreshCurrencyInfo((CurrencyPairsHolder) clickEvent.getItem());
                })).setCaption("Actions");

        currInfoGrid.setWidth("100%");

        GridLayout wexBalanceLabelsGrid = new GridLayout(2, 7);
        wexBalanceLabelsGrid.addComponent(new Label("Total USD: "), 0, 0);
        wexBalanceLabelsGrid.addComponent(labelWexTotalUSD, 1, 0);
        wexBalanceLabelsGrid.addComponent(new Label("Total BTC: "), 0, 1);
        wexBalanceLabelsGrid.addComponent(labelWexTotalBTC, 1, 1);
        wexBalanceLabelsGrid.addComponent(new Label("Total BCH: "), 0, 2);
        wexBalanceLabelsGrid.addComponent(labelWexTotalBCH, 1, 2);
        wexBalanceLabelsGrid.addComponent(new Label("Total ETH: "), 0, 3);
        wexBalanceLabelsGrid.addComponent(labelWexTotalETH, 1, 3);
        wexBalanceLabelsGrid.addComponent(new Label("Total LTC: "), 0, 4);
        wexBalanceLabelsGrid.addComponent(labelWexTotalLTC, 1, 4);
        wexBalanceLabelsGrid.addComponent(new Label("Total ZEC: "), 0, 5);
        wexBalanceLabelsGrid.addComponent(labelWexTotalZEC, 1, 5);
        wexBalanceLabelsGrid.addComponent(new Label("Total DSH: "), 0, 6);
        wexBalanceLabelsGrid.addComponent(labelWexTotalDSH, 1, 6);
        wexBalanceLabelsGrid.setWidth("100%");

        VerticalLayout wexBalanceVertical = new VerticalLayout();
        wexBalanceVertical.setSpacing(true);
        wexBalanceVertical.addComponent(wexBalanceLabelsGrid);

        GridLayout bitBalanceAvailLabelsGrid = new GridLayout(2, 7);
        bitBalanceAvailLabelsGrid.addComponent(new Label("USD: "), 0, 0);
        bitBalanceAvailLabelsGrid.addComponent(labelBitTotalUSD, 1, 0);
        bitBalanceAvailLabelsGrid.addComponent(new Label("BTC: "), 0, 1);
        bitBalanceAvailLabelsGrid.addComponent(labelBitTotalBTC, 1, 1);
        bitBalanceAvailLabelsGrid.addComponent(new Label("BCH: "), 0, 2);
        bitBalanceAvailLabelsGrid.addComponent(labelBitTotalBCH, 1, 2);
        bitBalanceAvailLabelsGrid.addComponent(new Label("ETH: "), 0, 3);
        bitBalanceAvailLabelsGrid.addComponent(labelBitTotalETH, 1, 3);
        bitBalanceAvailLabelsGrid.addComponent(new Label("LTC: "), 0, 4);
        bitBalanceAvailLabelsGrid.addComponent(labelBitTotalLTC, 1, 4);
        bitBalanceAvailLabelsGrid.addComponent(new Label("ZEC: "), 0, 5);
        bitBalanceAvailLabelsGrid.addComponent(labelBitTotalZEC, 1, 5);
        bitBalanceAvailLabelsGrid.addComponent(new Label("DSH: "), 0, 6);
        bitBalanceAvailLabelsGrid.addComponent(labelBitTotalDSH, 1, 6);
        bitBalanceAvailLabelsGrid.setWidth("80%");
        bitBalanceAvailLabelsGrid.setDefaultComponentAlignment(MIDDLE_LEFT);

        GridLayout bitBalanceOnOrderLabelsGrid = new GridLayout(2, 7);
        bitBalanceOnOrderLabelsGrid.addComponent(new Label("USD: "), 0, 0);
        bitBalanceOnOrderLabelsGrid.addComponent(labelBitOrderUSD, 1, 0);
        bitBalanceOnOrderLabelsGrid.addComponent(new Label("BTC: "), 0, 1);
        bitBalanceOnOrderLabelsGrid.addComponent(labelBitOrderBTC, 1, 1);
        bitBalanceOnOrderLabelsGrid.addComponent(new Label("BCH: "), 0, 2);
        bitBalanceOnOrderLabelsGrid.addComponent(labelBitOrderBCH, 1, 2);
        bitBalanceOnOrderLabelsGrid.addComponent(new Label("ETH: "), 0, 3);
        bitBalanceOnOrderLabelsGrid.addComponent(labelBitOrderETH, 1, 3);
        bitBalanceOnOrderLabelsGrid.addComponent(new Label("LTC: "), 0, 4);
        bitBalanceOnOrderLabelsGrid.addComponent(labelBitOrderLTC, 1, 4);
        bitBalanceOnOrderLabelsGrid.addComponent(new Label("ZEC: "), 0, 5);
        bitBalanceOnOrderLabelsGrid.addComponent(labelBitOrderZEC, 1, 5);
        bitBalanceOnOrderLabelsGrid.addComponent(new Label("DSH: "), 0, 6);
        bitBalanceOnOrderLabelsGrid.addComponent(labelBitOrderDSH, 1, 6);
        bitBalanceOnOrderLabelsGrid.setWidth("80%");
        bitBalanceOnOrderLabelsGrid.setDefaultComponentAlignment(MIDDLE_LEFT);

        GridLayout bitBalanceGrid = new GridLayout(2, 2);
        bitBalanceGrid.addComponent(new Label("Available balance: "), 0, 0);
        bitBalanceGrid.addComponent(bitBalanceAvailLabelsGrid, 0, 1);
        bitBalanceGrid.addComponent(new Label("On orders: "), 1, 0);
        bitBalanceGrid.addComponent(bitBalanceOnOrderLabelsGrid, 1, 1);
        bitBalanceGrid.setWidth("100%");

        VerticalLayout bitBalanceVerticalStub = new VerticalLayout();
        bitBalanceVerticalStub.setSpacing(true);
        bitBalanceVerticalStub.addComponent(bitBalanceGrid);

        Button btnWexBalanceRefresh = getRefreshMiniButton((Button.ClickListener) clickEvent -> updateWexBalance());
        HorizontalLayout wexBalancePanelCaption = getPanelCaptionComponents(btnWexBalanceRefresh, BFConstants.WEX);

        Button btnBitBalanceRefresh = getRefreshMiniButton((Button.ClickListener) clickEvent -> updateBitfinexBalance());
        HorizontalLayout bitBalancePanelCaption = getPanelCaptionComponents(btnBitBalanceRefresh, BFConstants.BITFINEX);

        CssLayout wexBalancePanel = new CssLayout();
        wexBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        wexBalancePanel.addComponent(wexBalancePanelCaption);
        wexBalancePanel.addComponent(wexBalanceVertical);
        wexBalancePanel.setWidth("16em");

        CssLayout bitBalancePanel = new CssLayout();
        bitBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        bitBalancePanel.addComponent(bitBalancePanelCaption);
        bitBalancePanel.addComponent(bitBalanceVerticalStub);
        bitBalancePanel.setWidth("20em");

        GridLayout horizontalBalanceGrid = new GridLayout(5, 1);
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

        Panel topPanel = new Panel("User balance");
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

    private void initBalanceLabels() {
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

        labelBitOrderUSD = new Label("0", ContentMode.HTML);
        labelBitOrderBTC = new Label("0", ContentMode.HTML);
        labelBitOrderETH = new Label("0", ContentMode.HTML);
        labelBitOrderBCH = new Label("0", ContentMode.HTML);
        labelBitOrderLTC = new Label("0", ContentMode.HTML);
        labelBitOrderZEC = new Label("0", ContentMode.HTML);
        labelBitOrderDSH = new Label("0", ContentMode.HTML);
    }

    private HorizontalLayout getPanelCaptionComponents(Button btnBitBalanceRefresh, String caption) {
        HorizontalLayout bitBalancePanelCaption = new HorizontalLayout();
        bitBalancePanelCaption.addStyleName("v-panel-caption");
        bitBalancePanelCaption.setWidth("100%");
        Label labelb = new Label(caption);
        bitBalancePanelCaption.addComponent(labelb);
        bitBalancePanelCaption.setExpandRatio(labelb, 1);
        bitBalancePanelCaption.addComponent(btnBitBalanceRefresh);
        return bitBalancePanelCaption;
    }

    private Button getRefreshMiniButton(Button.ClickListener clickListener) {
        Button btnWexBalanceRefresh = new Button();
        btnWexBalanceRefresh.setIcon(VaadinIcons.REFRESH);
        btnWexBalanceRefresh.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        btnWexBalanceRefresh.addStyleName(ValoTheme.BUTTON_SMALL);
        btnWexBalanceRefresh.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnWexBalanceRefresh.addClickListener(clickListener);
        return btnWexBalanceRefresh;
    }

    private void initAutoRefreshTimer() {
        timer = new Timer();
        addExtension(timer);
        timer.run(() -> {
            if (refreshSec >= 30) {
                mainui.refreshCurrencyGrid(null);
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
            String result = bitLib.sendRequestV1Balances();
            JSONArray json = new JSONArray(result);
            BitfinexBalancesList bitfinexBalancesList = new BitfinexBalancesList();
            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = json.getJSONObject(i);
                BitfinexBalance bitfinexBalance = new Gson().fromJson(obj.toString(), BitfinexBalance.class);
                bitfinexBalancesList.add(bitfinexBalance);
            }

            if (bitfinexBalancesList.getBalancesMap().size() > 0) {
                labelBitTotalUSD.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getAvailUsd()) + "</b>");
                labelBitTotalBTC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getAvailBtc()) + "</b>");
                labelBitTotalBCH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getAvailBch()) + "</b>");
                labelBitTotalETH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getAvailEth()) + "</b>");
                labelBitTotalLTC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getAvailLtc()) + "</b>");
                labelBitTotalZEC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getAvailZec()) + "</b>");
                labelBitTotalDSH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getAvailDsh()) + "</b>");

                labelBitOrderUSD.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersUsd()) + "</b>");
                labelBitOrderBTC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersBtc()) + "</b>");
                labelBitOrderBCH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersBch()) + "</b>");
                labelBitOrderETH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersEth()) + "</b>");
                labelBitOrderLTC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersLtc()) + "</b>");
                labelBitOrderZEC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersZec()) + "</b>");
                labelBitOrderDSH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersDsh()) + "</b>");
            } else {
                setBitLabelsError();
            }
        } catch (Exception e) {
            setBitLabelsError();
            e.printStackTrace();
        }
    }

    private void setBitLabelsError() {
        labelBitTotalUSD.setValue("<b>error</b>");
        labelBitTotalBTC.setValue("<b>error</b>");
        labelBitTotalBCH.setValue("<b>error</b>");
        labelBitTotalETH.setValue("<b>error</b>");
        labelBitTotalLTC.setValue("<b>error</b>");
        labelBitTotalZEC.setValue("<b>error</b>");
        labelBitTotalDSH.setValue("<b>error</b>");
    }

    private void updateWexBalance() {
        WexNzPrivateApiAccessLib privateLib = new WexNzPrivateApiAccessLib(settings.getProperty(BFConstants.WEX_API_KEY),
                settings.getProperty(BFConstants.WEX_API_SECRET));

        ArrayList<NameValuePair> postData = new ArrayList<>();
        postData.add(new BasicNameValuePair("method", "getInfo"));
        privateLib.setPrivateUrl(BFConstants.WEX_API_PRIVATE_URL);

        JsonObject result = privateLib.performAuthorizedRequest(postData);
        if (result != null) {
            privateLib.log("getInfo result: ".concat(result.toString()));
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
                setWexLabelsError();
            }
        } else setWexLabelsError();
    }

    private void setWexLabelsError() {
        labelWexTotalUSD.setValue("<b>error</b>");
        labelWexTotalBTC.setValue("<b>error</b>");
        labelWexTotalBCH.setValue("<b>error</b>");
        labelWexTotalETH.setValue("<b>error</b>");
        labelWexTotalLTC.setValue("<b>error</b>");
        labelWexTotalZEC.setValue("<b>error</b>");
        labelWexTotalDSH.setValue("<b>error</b>");
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        System.out.println("enter");
        smartInitCurrencyPairs();
        initMarketColumns();
//        PublicApiAccessLib.setBasicUrl("https://poloniex.com/");
//        PublicApiAccessLib.clearHeaders();
//        JsonObject result = PublicApiAccessLib.performBasicRequest("public", "?command=returnTicker");
//        System.out.println(result.toString());
    }

    public void initMarketColumns() {
        currInfoGrid.getColumn(BFConstants.WEX_GRID_COLUMN).setHidden(!settings.isPropertyEnabled(BFConstants.WEX));
        currInfoGrid.getColumn(BFConstants.BITFINEX_GRID_COLUMN).setHidden(!settings.isPropertyEnabled(BFConstants.BITFINEX));
        currInfoGrid.getColumn(BFConstants.KRAKEN_GRID_COLUMN).setHidden(!settings.isPropertyEnabled(BFConstants.KRAKEN));
        currInfoGrid.getColumn(BFConstants.CEX_GRID_COLUMN).setHidden(!settings.isPropertyEnabled(BFConstants.CEX));
    }

    private void smartInitCurrencyPairs() {
        /* Remove section */
        List<CurrencyPairsHolder> currencyPairsHolderList = mainui.getCurrencyPairsHolderList();
        HashMap<String, Boolean> map = settings.getCoinSelectStateMap();

        currencyPairsHolderList.removeIf(new Predicate<CurrencyPairsHolder>() {
            @Override
            public boolean test(CurrencyPairsHolder pairHolder) {
                return map.get(pairHolder.getName()).equals(Boolean.FALSE);
            }
        });

        /* Add section */
        for (String name : map.keySet()) {
            if (!containsName(currencyPairsHolderList, name) && map.get(name).equals(Boolean.TRUE)) {
                currencyPairsHolderList.add(mainui.initNewCurrencyPair(name));
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
            if (isInitialized()) {
                labelRefresh.setValue("At least one Coin and Market must be selected for monitoring! (check settings)");
                labelRefresh.addStyleName(ValoTheme.LABEL_FAILURE);
            }
        }
        if (cphSize > 0) currInfoGrid.setHeightByRows(cphSize);
        else currInfoGrid.setHeightByRows(1);

        if (marketsCount == 0) currInfoGrid.setVisible(false);
        else currInfoGrid.setVisible(true);

        btnRefreshTable.setEnabled(!emptyTable);
        chkAutoRefresh.setEnabled(!emptyTable);
        currInfoGrid.getDataProvider().refreshAll();
    }

    private boolean containsName(final List<CurrencyPairsHolder> list, final String name) {
        return list.stream().map(CurrencyPairsHolder::getName).anyMatch(name::equals);
    }

    public void hideInitStub() {
        waitingStubPanel.setVisible(false);
        setInitialized(true);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public Button getBtnSettings() {
        return btnSettings;
    }
}