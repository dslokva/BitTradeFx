package kz.bittrade.views;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kz.bittrade.BitTradeFx;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.markets.api.lib.WexNzPrivateApiAccessLib;

import java.util.HashMap;
import java.util.Map;

import static com.vaadin.ui.Alignment.*;

public class SettingsView extends VerticalLayout implements View {
    private TextField txtWexApiKey;
    private PasswordField txtWexSecretKey;
    private TextField txtBitApiKey;
    private PasswordField txtBitSecretKey;
    private TextField txtKraApiKey;
    private PasswordField txtKraSecretKey;
    private TextField txtCexApiKey;
    private PasswordField txtCexSecretKey;

    private AppSettingsHolder settings;
    private BitTradeFx mainui;

    private CheckBox chkEnableBTC;
    private CheckBox chkEnableBCH;
    private CheckBox chkEnableLTC;
    private CheckBox chkEnableETH;
    private CheckBox chkEnableZEC;
    private CheckBox chkEnableDSH;

    private CheckBox chkEnableWex;
    private CheckBox chkEnableBit;
    private CheckBox chkEnableKra;
    private CheckBox chkEnableCex;

    private RadioButtonGroup sortOptions;
    private Map<String, String> sortColsMap;

    public SettingsView() {
        addStyleName("content-common");
        String txtBoxWidth = "500px";

        txtWexApiKey = new TextField();
        txtWexSecretKey = new PasswordField();
        txtWexApiKey.setWidth(txtBoxWidth);
        txtWexSecretKey.setWidth(txtBoxWidth);

        txtBitApiKey = new TextField();
        txtBitSecretKey = new PasswordField();
        txtBitApiKey.setWidth(txtBoxWidth);
        txtBitSecretKey.setWidth(txtBoxWidth);

        txtKraApiKey = new TextField();
        txtKraSecretKey = new PasswordField();
        txtKraApiKey.setWidth(txtBoxWidth);
        txtKraSecretKey.setWidth(txtBoxWidth);

        txtCexApiKey = new TextField();
        txtCexSecretKey = new PasswordField();
        txtCexApiKey.setWidth(txtBoxWidth);
        txtCexSecretKey.setWidth(txtBoxWidth);

        chkEnableBTC = new CheckBox(BFConstants.BITCOIN);
        chkEnableBCH = new CheckBox(BFConstants.BITCOIN_CASH);
        chkEnableLTC = new CheckBox(BFConstants.LITECOIN);
        chkEnableETH = new CheckBox(BFConstants.ETHERIUM);
        chkEnableZEC = new CheckBox(BFConstants.ZCASH);
        chkEnableDSH = new CheckBox(BFConstants.DASH_COIN);

        chkEnableWex = new CheckBox(BFConstants.WEX);
        chkEnableBit = new CheckBox(BFConstants.BITFINEX);
        chkEnableKra = new CheckBox(BFConstants.KRAKEN);
        chkEnableCex = new CheckBox(BFConstants.CEX);

        sortColsMap = new HashMap<>();
        sortColsMap.put("Delta %", BFConstants.GRID_DELTA_PERCENT_COLUMN);
        sortColsMap.put("Delta $", BFConstants.GRID_DELTA_DOUBLE_COLUMN);

        sortOptions = new RadioButtonGroup<>("Auto sort column after refresh:", sortColsMap.keySet());
        sortOptions.setItemCaptionGenerator(item -> "Column \"" + item + "\"");
        sortOptions.setValue(getKeyFromValue(sortColsMap, BFConstants.GRID_DELTA_PERCENT_COLUMN));

        mainui = (BitTradeFx) UI.getCurrent();
        settings = mainui.getSettings();

        GridLayout wexnzSettingsGridLayout = new GridLayout(2, 2);
        wexnzSettingsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        wexnzSettingsGridLayout.addComponent(new Label("API key:"), 0, 0);
        wexnzSettingsGridLayout.addComponent(txtWexApiKey, 1, 0);
        wexnzSettingsGridLayout.addComponent(new Label("Secret:"), 0, 1);
        wexnzSettingsGridLayout.addComponent(txtWexSecretKey, 1, 1);
        wexnzSettingsGridLayout.setSpacing(true);

        GridLayout bitfinexSettingsGridLayout = new GridLayout(2, 2);
        bitfinexSettingsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        bitfinexSettingsGridLayout.addComponent(new Label("API key:"), 0, 0);
        bitfinexSettingsGridLayout.addComponent(txtBitApiKey, 1, 0);
        bitfinexSettingsGridLayout.addComponent(new Label("Secret:"), 0, 1);
        bitfinexSettingsGridLayout.addComponent(txtBitSecretKey, 1, 1);
        bitfinexSettingsGridLayout.setSpacing(true);

        GridLayout krakenSettingsGridLayout = new GridLayout(2, 2);
        krakenSettingsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        krakenSettingsGridLayout.addComponent(new Label("API key:"), 0, 0);
        krakenSettingsGridLayout.addComponent(txtKraApiKey, 1, 0);
        krakenSettingsGridLayout.addComponent(new Label("Secret:"), 0, 1);
        krakenSettingsGridLayout.addComponent(txtKraSecretKey, 1, 1);
        krakenSettingsGridLayout.setSpacing(true);

        GridLayout cexSettingsGridLayout = new GridLayout(2, 2);
        cexSettingsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        cexSettingsGridLayout.addComponent(new Label("API key:"), 0, 0);
        cexSettingsGridLayout.addComponent(txtCexApiKey, 1, 0);
        cexSettingsGridLayout.addComponent(new Label("Secret:"), 0, 1);
        cexSettingsGridLayout.addComponent(txtCexSecretKey, 1, 1);
        cexSettingsGridLayout.setSpacing(true);

        Button btnBack = new Button("Back");
        btnBack.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnBack.addClickListener(
                e -> mainui.getNavigator().navigateTo(BFConstants.MAIN_VIEW));

        Button btnSave = new Button("Save");
        btnSave.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        btnSave.addClickListener(
                e -> {
                    String wexApiKey = txtWexApiKey.getValue();
                    String wexSecretKey = txtWexSecretKey.getValue();

                    if (!wexApiKey.equals("") && !WexNzPrivateApiAccessLib.isValidAPIKey(wexApiKey)) {
                        txtWexApiKey.setComponentError(new UserError("Wex.nz API key has invalid structure!"));
                    } else if (!wexSecretKey.equals("") && !WexNzPrivateApiAccessLib.isValidSecret(wexSecretKey))
                        txtWexSecretKey.setComponentError(new UserError("Wex.nz Secret key has invalid structure!"));
                    else {
                        settings.setProperty(BFConstants.WEX_API_KEY, wexApiKey);
                        settings.setProperty(BFConstants.WEX_API_SECRET, wexSecretKey);
                        txtWexApiKey.setComponentError(null);
                        txtWexSecretKey.setComponentError(null);
                    }

                    settings.setProperty(BFConstants.BIT_API_KEY, txtBitApiKey.getValue());
                    settings.setProperty(BFConstants.BIT_API_SECRET, txtBitSecretKey.getValue());
                    settings.setProperty(BFConstants.KRA_API_KEY, txtKraApiKey.getValue());
                    settings.setProperty(BFConstants.KRA_API_SECRET, txtKraSecretKey.getValue());
                    settings.setProperty(BFConstants.CEX_API_KEY, txtCexApiKey.getValue());
                    settings.setProperty(BFConstants.CEX_API_SECRET, txtCexSecretKey.getValue());

                    settings.setProperty(BFConstants.BITCOIN, chkEnableBTC.getValue().toString());
                    settings.setProperty(BFConstants.BITCOIN_CASH, chkEnableBCH.getValue().toString());
                    settings.setProperty(BFConstants.LITECOIN, chkEnableLTC.getValue().toString());
                    settings.setProperty(BFConstants.ETHERIUM, chkEnableETH.getValue().toString());
                    settings.setProperty(BFConstants.ZCASH, chkEnableZEC.getValue().toString());
                    settings.setProperty(BFConstants.DASH_COIN, chkEnableDSH.getValue().toString());

                    settings.setProperty(BFConstants.WEX, chkEnableWex.getValue().toString());
                    settings.setProperty(BFConstants.BITFINEX, chkEnableBit.getValue().toString());
                    settings.setProperty(BFConstants.KRAKEN, chkEnableKra.getValue().toString());
                    settings.setProperty(BFConstants.CEX, chkEnableCex.getValue().toString());

                    settings.setProperty(BFConstants.AUTO_SORT_COLUMN, sortColsMap.get(sortOptions.getValue()));

                    settings.updateCoinSelectState(chkEnableBTC, chkEnableBCH, chkEnableLTC, chkEnableETH, chkEnableZEC, chkEnableDSH);
                    settings.updateMarketSelectMap(chkEnableWex, chkEnableBit, chkEnableKra, chkEnableCex);

                    mainui.showNotification("Settings", "Settings are saved in browser local storage.", 3000, Position.BOTTOM_RIGHT, "tray success");
                }
        );

        GridLayout btnsGrid = new GridLayout(2, 1);
        btnsGrid.setWidth("100%");
        btnsGrid.addComponent(btnSave, 0, 0);
        btnsGrid.addComponent(btnBack, 1, 0);
        btnsGrid.setComponentAlignment(btnSave, MIDDLE_LEFT);
        btnsGrid.setComponentAlignment(btnBack, MIDDLE_RIGHT);

        VerticalLayout apiVerticalHolder = new VerticalLayout();
        apiVerticalHolder.addComponent(new Label("Keys for <b>[WEX.nz]</b> market:", ContentMode.HTML));
        apiVerticalHolder.addComponent(wexnzSettingsGridLayout);
        apiVerticalHolder.addComponent(new Label("Keys for <b>[Bitfinex.com]</b> market:", ContentMode.HTML));
        apiVerticalHolder.addComponent(bitfinexSettingsGridLayout);
        apiVerticalHolder.addComponent(new Label("Keys for <b>[Kraken.com]</b> market:", ContentMode.HTML));
        apiVerticalHolder.addComponent(krakenSettingsGridLayout);
        apiVerticalHolder.addComponent(new Label("Keys for <b>[CEX.io]</b> market:", ContentMode.HTML));
        apiVerticalHolder.addComponent(cexSettingsGridLayout);

        GridLayout coinCheckBoxesGrid = new GridLayout(6, 1);
        coinCheckBoxesGrid.addComponent(chkEnableBTC);
        coinCheckBoxesGrid.addComponent(chkEnableBCH);
        coinCheckBoxesGrid.addComponent(chkEnableLTC);
        coinCheckBoxesGrid.addComponent(chkEnableETH);
        coinCheckBoxesGrid.addComponent(chkEnableZEC);
        coinCheckBoxesGrid.addComponent(chkEnableDSH);
        coinCheckBoxesGrid.setWidth("100%");

        GridLayout marketCheckBoxesGrid = new GridLayout(4, 1);
        marketCheckBoxesGrid.addComponent(chkEnableWex);
        marketCheckBoxesGrid.addComponent(chkEnableBit);
        marketCheckBoxesGrid.addComponent(chkEnableKra);
        marketCheckBoxesGrid.addComponent(chkEnableCex);
        marketCheckBoxesGrid.setWidth("100%");

        VerticalLayout verticalDumbCoinChks = new VerticalLayout();
        verticalDumbCoinChks.addComponent(coinCheckBoxesGrid);
        verticalDumbCoinChks.setComponentAlignment(coinCheckBoxesGrid, MIDDLE_CENTER);

        VerticalLayout verticalDumbMarketChks = new VerticalLayout();
        verticalDumbMarketChks.addComponent(marketCheckBoxesGrid);
        verticalDumbMarketChks.setComponentAlignment(marketCheckBoxesGrid, MIDDLE_CENTER);

        Panel coinCheckBoxesPanel = new Panel("Enabled coins for monitoring");
        coinCheckBoxesPanel.addStyleName(ValoTheme.PANEL_WELL);
        coinCheckBoxesPanel.setContent(verticalDumbCoinChks);

        Panel marketsCheckBoxesPanel = new Panel("Enabled markets for monitoring");
        marketsCheckBoxesPanel.addStyleName(ValoTheme.PANEL_WELL);
        marketsCheckBoxesPanel.setContent(verticalDumbMarketChks);

        VerticalLayout otherVerticalHolder = new VerticalLayout();
        otherVerticalHolder.addComponent(sortOptions);
        otherVerticalHolder.addComponent(coinCheckBoxesPanel);
        otherVerticalHolder.addComponent(marketsCheckBoxesPanel);

        Panel keySettingsPanel = new Panel("User API");
        keySettingsPanel.setContent(apiVerticalHolder);
        keySettingsPanel.setWidth("100%");
        keySettingsPanel.setIcon(VaadinIcons.LOCK);

        Panel appSettingsPanel = new Panel("Other");
        appSettingsPanel.setContent(otherVerticalHolder);
        appSettingsPanel.setWidth("100%");
        appSettingsPanel.setIcon(VaadinIcons.COGS);

        VerticalLayout allPanelsHolder = new VerticalLayout();
        allPanelsHolder.addComponent(keySettingsPanel);
        allPanelsHolder.addComponent(appSettingsPanel);
        allPanelsHolder.addComponent(btnsGrid);

        Panel settingsPanel = new Panel("Application settings");
        settingsPanel.setContent(allPanelsHolder);
        settingsPanel.setWidth("50%");
        settingsPanel.setIcon(VaadinIcons.OPTIONS);

        addComponent(settingsPanel);
        setComponentAlignment(settingsPanel, Alignment.TOP_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        updateValuesToUI();
    }

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    public void updateValuesToUI() {
        txtWexApiKey.setValue(settings.getProperty(BFConstants.WEX_API_KEY));
        txtWexSecretKey.setValue(settings.getProperty(BFConstants.WEX_API_SECRET));

        txtBitApiKey.setValue(settings.getProperty(BFConstants.BIT_API_KEY));
        txtBitSecretKey.setValue(settings.getProperty(BFConstants.BIT_API_SECRET));

        txtKraApiKey.setValue(settings.getProperty(BFConstants.KRA_API_KEY));
        txtKraSecretKey.setValue(settings.getProperty(BFConstants.KRA_API_SECRET));

        txtCexApiKey.setValue(settings.getProperty(BFConstants.CEX_API_KEY));
        txtCexSecretKey.setValue(settings.getProperty(BFConstants.CEX_API_SECRET));

        sortOptions.setValue(getKeyFromValue(sortColsMap, settings.getProperty(BFConstants.AUTO_SORT_COLUMN)));
        if (!sortOptions.getSelectedItem().isPresent())
            sortOptions.setValue(getKeyFromValue(sortColsMap, BFConstants.GRID_DELTA_PERCENT_COLUMN));

        chkEnableBTC.setValue(Boolean.valueOf(settings.getProperty(BFConstants.BITCOIN)));
        chkEnableBCH.setValue(Boolean.valueOf(settings.getProperty(BFConstants.BITCOIN_CASH)));
        chkEnableLTC.setValue(Boolean.valueOf(settings.getProperty(BFConstants.LITECOIN)));
        chkEnableETH.setValue(Boolean.valueOf(settings.getProperty(BFConstants.ETHERIUM)));
        chkEnableZEC.setValue(Boolean.valueOf(settings.getProperty(BFConstants.ZCASH)));
        chkEnableDSH.setValue(Boolean.valueOf(settings.getProperty(BFConstants.DASH_COIN)));

        chkEnableWex.setValue(Boolean.valueOf(settings.getProperty(BFConstants.WEX)));
        chkEnableBit.setValue(Boolean.valueOf(settings.getProperty(BFConstants.BITFINEX)));
        chkEnableKra.setValue(Boolean.valueOf(settings.getProperty(BFConstants.KRAKEN)));
        chkEnableCex.setValue(Boolean.valueOf(settings.getProperty(BFConstants.CEX)));

        settings.updateCoinSelectState(chkEnableBTC, chkEnableBCH, chkEnableLTC, chkEnableETH, chkEnableZEC, chkEnableDSH);
        settings.updateMarketSelectMap(chkEnableWex, chkEnableBit, chkEnableKra, chkEnableCex);
    }
}