package kz.bittrade.views;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.SerializableComparator;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.Timer;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import kz.bittrade.BitTradeFx;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;
import kz.bittrade.markets.api.holders.user.WexNzGetInfo;
import kz.bittrade.markets.api.lib.WexNzPrivateApiAccessLib;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.*;

import static com.vaadin.ui.Alignment.MIDDLE_LEFT;


public class MainView extends VerticalLayout implements View {
    private static AppSettingsHolder settings;
    private BitTradeFx mainui;

    private Grid<CurrencyPairsHolder> currInfoGrid;
    private ProgressBar refreshProgressBar;
    private Label labelWexTotalUSD;
    private Label labelRefreshSec;
    private Label labelRefresh;
    private Timer timer;
    private int refreshSec;

    public MainView() {
        settings = AppSettingsHolder.getInstance();
        mainui = (BitTradeFx) UI.getCurrent();
        setSizeFull();

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

        Button btnSettings = new Button("App settings");
        btnSettings.addClickListener(
                e -> getUI().getNavigator().navigateTo("settings"));

        Button btnRefreshUserInfo = new Button("Update user info");
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

        labelWexTotalUSD = new Label("0 $", ContentMode.HTML);
        VerticalLayout topLayer = new VerticalLayout();
        topLayer.addComponent(new Label("<b>Available user balance</b>", ContentMode.HTML));
        topLayer.addComponent(new HorizontalLayout(new Label("Total WexNz USD: "), labelWexTotalUSD));
        topLayer.setMargin(new MarginInfo(false, false, true, false));

        HorizontalLayout middleLayer = new HorizontalLayout();
        middleLayer.setDefaultComponentAlignment(MIDDLE_LEFT);
        middleLayer.addComponent(btnRefreshUserInfo);
        middleLayer.addComponent(btnSettings);
        middleLayer.setMargin(new MarginInfo(false, false, true, false));

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
        bottomLayer.setMargin(new MarginInfo(true, false, false, false));

        GridLayout marketsGridLayout = new GridLayout(1, 5);
        marketsGridLayout.setWidth("100%");
        marketsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        marketsGridLayout.addComponent(topLayer, 0, 0);
        marketsGridLayout.addComponent(middleLayer, 0, 1);
        marketsGridLayout.addComponent(currInfoGrid, 0, 2);
        marketsGridLayout.addComponent(refreshLayer, 0, 3);
        marketsGridLayout.addComponent(bottomLayer, 0, 4);
        marketsGridLayout.setMargin(new MarginInfo(false, false, false, true));

        addComponent(marketsGridLayout);
        initTimer();
    }



    private void initTimer() {
        timer = new Timer();
        addExtension(timer);
        timer.run(() -> {
            if (refreshSec >= 30) {
                mainui.refreshCurrencyGrid();
                refreshSec = 0;
                labelRefreshSec.setValue("");
            } else {
                refreshSec++;
                labelRefreshSec.setValue("(next in: "+String.valueOf(30-refreshSec)+")");
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
            privateLib.log("getInfo result: ".concat(result.toString()));
            WexNzGetInfo wexUserInfo = new Gson().fromJson(result, WexNzGetInfo.class);
            if (wexUserInfo != null && wexUserInfo.getSuccess() == 1) {
                labelWexTotalUSD.setValue("<b>" + String.format("%.6f", wexUserInfo.getInfo().getFunds().getUsd()) + "$</b>");
            } else {
                labelWexTotalUSD.setValue("<b> %-( error</b>");
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