package kz.bittrade.views;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.SerializableComparator;
import com.vaadin.server.UserError;
import com.vaadin.shared.Position;
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
import kz.bittrade.markets.api.holders.user.balance.BitfinexBalanceHolder;
import kz.bittrade.markets.api.holders.user.balance.WexNzBalanceHolder;
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

    private List<WexNzBalanceHolder> wexNzUserBalance;
    private List<BitfinexBalanceHolder> bitfinexUserBalance;

    private Grid<CurrencyPairsHolder> currInfoGrid;
    private Grid<WexNzBalanceHolder> wexBalanceGrid;
    private Grid<BitfinexBalanceHolder> bitBalanceGrid;

    private CssLayout wexBalancePanel;
    private CssLayout bitBalancePanel;
    private Label bitBalanceStubLabel;
    private Label wexBalanceStubLabel;

    private ProgressBar refreshProgressBar;
    private Label labelRefreshSec;
    private Label labelRefresh;

    private Button btnRefreshTable;
    private Button btnSettings;
    private Button btnRefreshUserBalance;

    private CheckBox chkAutoRefresh;
    private Timer timer;
    private int refreshSec;
    private boolean initialized;

    private VerticalLayout waitingStubPanel;

    public MainView() {
        addStyleName("content-common");

        mainui = (BitTradeFx) UI.getCurrent();
        settings = mainui.getSettings();

        wexNzUserBalance = new ArrayList<>();
        bitfinexUserBalance = new ArrayList<>();

        refreshProgressBar = new ProgressBar(0.0f);
        refreshProgressBar.setVisible(false);

        labelRefresh = new Label("");
        labelRefreshSec = new Label("");

        bitBalanceStubLabel = getBalanceStubLabel();
        wexBalanceStubLabel = getBalanceStubLabel();

        btnRefreshTable = new Button("Refresh all");
        btnRefreshTable.addClickListener(
                e -> {

                    mainui.refreshCurrencyGrid(null);
                });
        btnRefreshTable.addStyleName(ValoTheme.BUTTON_FRIENDLY);

        btnSettings = new Button("Settings");
        btnSettings.addClickListener(
                e -> getUI().getNavigator().navigateTo(BFConstants.SETTINGS_VIEW));

        btnRefreshUserBalance = new Button("Refresh all");
        btnRefreshUserBalance.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        btnRefreshUserBalance.addClickListener(
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

        wexBalanceGrid = new Grid<>();
        wexBalanceGrid.setSelectionMode(Grid.SelectionMode.NONE);
        wexBalanceGrid.setCaption("Total balance");
        wexBalanceGrid.setItems(wexNzUserBalance);
        wexBalanceGrid.addColumn(WexNzBalanceHolder::getCurrencyName, new HtmlRenderer())
                .setCaption("Currency")
                .setResizable(false)
                .setWidth(110);
        wexBalanceGrid.addColumn(WexNzBalanceHolder::getAmount, new HtmlRenderer())
                .setCaption("Balance")
                .setResizable(false);
        wexBalanceGrid.setStyleName(ValoTheme.TABLE_SMALL);
        wexBalanceGrid.setWidth("17em");
        wexBalanceGrid.setHeightByRows(1);
        wexBalanceGrid.setVisible(false);

        bitBalanceGrid = new Grid<>();
        bitBalanceGrid.setSelectionMode(Grid.SelectionMode.NONE);
        bitBalanceGrid.setCaption("Total balance");
        bitBalanceGrid.setItems(bitfinexUserBalance);
        bitBalanceGrid.addColumn(BitfinexBalanceHolder::getCurrencyName, new HtmlRenderer())
                .setCaption("Currency")
                .setResizable(false)
                .setWidth(110);
        bitBalanceGrid.addColumn(BitfinexBalanceHolder::getAmount, new HtmlRenderer())
                .setCaption("Balance")
                .setResizable(false);
        bitBalanceGrid.setStyleName(ValoTheme.TABLE_SMALL);
        bitBalanceGrid.setWidth("17em");
        bitBalanceGrid.setHeightByRows(1);
        bitBalanceGrid.setVisible(false);


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

        Button btnWexBalanceRefresh = getRefreshMiniButton((Button.ClickListener) clickEvent -> updateWexBalance());
        Button btnWexBalanceClear = getClearMiniButton((Button.ClickListener) clickEvent -> hideBalancePanel(wexNzUserBalance, wexBalanceGrid, wexBalanceStubLabel));
        HorizontalLayout wexBalancePanelCaption = getPanelCaptionComponents(btnWexBalanceRefresh, btnWexBalanceClear, BFConstants.WEX);

        Button btnBitBalanceRefresh = getRefreshMiniButton((Button.ClickListener) clickEvent -> updateBitfinexBalance());
        Button btnBitBalanceClear = getClearMiniButton((Button.ClickListener) clickEvent -> hideBalancePanel(bitfinexUserBalance, bitBalanceGrid, bitBalanceStubLabel));
        HorizontalLayout bitBalancePanelCaption = getPanelCaptionComponents(btnBitBalanceRefresh, btnBitBalanceClear, BFConstants.BITFINEX);

        wexBalancePanel = new CssLayout();
        wexBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        wexBalancePanel.addComponent(wexBalancePanelCaption);
        wexBalancePanel.addComponent(wexBalanceVerticalStub);
        wexBalancePanel.setWidth("18.5em");

        bitBalancePanel = new CssLayout();
        bitBalancePanel.addStyleName(ValoTheme.LAYOUT_CARD);
        bitBalancePanel.addComponent(bitBalancePanelCaption);
        bitBalancePanel.addComponent(bitBalanceVerticalStub);
        bitBalancePanel.setWidth("18.5em");

        HorizontalLayout horizontalBalanceGrid = new HorizontalLayout();
        horizontalBalanceGrid.addComponent(wexBalancePanel);
        horizontalBalanceGrid.addComponent(bitBalancePanel);

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
                        bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.USD, bitfinexBalancesList.getAvailUsd()));
                        bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.BITCOIN, bitfinexBalancesList.getAvailBtc()));
                        bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.BITCOIN_CASH, bitfinexBalancesList.getAvailBch()));
                        bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.ETHERIUM, bitfinexBalancesList.getAvailEth()));
                        bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.LITECOIN, bitfinexBalancesList.getAvailLtc()));
                        bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.ZCASH, bitfinexBalancesList.getAvailZec()));
                        bitfinexUserBalance.add(new BitfinexBalanceHolder(BFConstants.DASH_COIN, bitfinexBalancesList.getAvailDsh()));
                        bitBalanceGrid.setCaption("Total balance @" + settings.getNowString());
                        bitBalanceGrid.setHeightByRows(7);
//                labelBitOrderUSD.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersUsd()) + "</b>");
//                labelBitOrderBTC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersBtc()) + "</b>");
//                labelBitOrderBCH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersBch()) + "</b>");
//                labelBitOrderETH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersEth()) + "</b>");
//                labelBitOrderLTC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersLtc()) + "</b>");
//                labelBitOrderZEC.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersZec()) + "</b>");
//                labelBitOrderDSH.setValue("<b>" + String.format("%.6f", bitfinexBalancesList.getOnOrdersDsh()) + "</b>");
                    }
                } else {
                    setBitLabelsError("");
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
//        labelBitTotalUSD.setValue("<b>error</b>");
//        labelBitTotalBTC.setValue("<b>error</b>");
//        labelBitTotalBCH.setValue("<b>error</b>");
//        labelBitTotalETH.setValue("<b>error</b>");
//        labelBitTotalLTC.setValue("<b>error</b>");
//        labelBitTotalZEC.setValue("<b>error</b>");
//        labelBitTotalDSH.setValue("<b>error</b>");
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
                privateLib.log("getInfo result: ".concat(result.toString()));
                WexNzGetInfo wexUserInfo = new Gson().fromJson(result, WexNzGetInfo.class);
                if (wexUserInfo != null) {
                    if (wexUserInfo.getSuccess() == 1) {
                        wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.USD, wexUserInfo.getInfo().getFunds().getUsd()));
                        wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.BITCOIN, wexUserInfo.getInfo().getFunds().getBtc()));
                        wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.BITCOIN_CASH, wexUserInfo.getInfo().getFunds().getBch()));
                        wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.ETHERIUM, wexUserInfo.getInfo().getFunds().getEth()));
                        wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.LITECOIN, wexUserInfo.getInfo().getFunds().getLtc()));
                        wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.ZCASH, wexUserInfo.getInfo().getFunds().getZec()));
                        wexNzUserBalance.add(new WexNzBalanceHolder(BFConstants.DASH_COIN, wexUserInfo.getInfo().getFunds().getDsh()));
                        wexBalanceGrid.setCaption("Total balance @" + wexUserInfo.getInfo().getTimestamp());
                        wexBalanceGrid.setHeightByRows(7);
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
        System.out.println("enter");
        smartInitCurrencyPairs();
        initMarketColumns();
        initBalanceStubLabels();
//        PublicApiAccessLib.setBasicUrl("https://poloniex.com/");
//        PublicApiAccessLib.clearHeaders();
//        JsonObject result = PublicApiAccessLib.performBasicRequest("public", "?command=returnTicker");
//        System.out.println(result.toString());
    }

    public void initBalanceStubLabels() {
        initBalanceStubLabel(wexBalanceStubLabel, isWexAPIKeyPresent());
        initBalanceStubLabel(bitBalanceStubLabel, isBitAPIKeyPresent());
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

    public void initMarketColumns() {
        boolean wexHidden = !settings.isPropertyEnabled(BFConstants.WEX);
        currInfoGrid.getColumn(BFConstants.WEX_GRID_COLUMN).setHidden(wexHidden);
        wexBalancePanel.setVisible(!wexHidden);

        boolean bitHidden = !settings.isPropertyEnabled(BFConstants.BITFINEX);
        currInfoGrid.getColumn(BFConstants.BITFINEX_GRID_COLUMN).setHidden(bitHidden);
        bitBalancePanel.setVisible(!bitHidden);

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