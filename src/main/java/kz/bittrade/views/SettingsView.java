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

import static com.vaadin.ui.Alignment.*;

public class SettingsView extends VerticalLayout implements View {
    private TextField txtWexApiKey;
    private PasswordField txtWexSecretKey;
    private TextField txtBitApiKey;
    private PasswordField txtBitSecretKey;
    private TextField txtKraApiKey;
    private PasswordField txtKraSecretKey;
    private AppSettingsHolder settings;
    private CheckBox chkAutoSortByDeltaPercent;
    private BitTradeFx mainui;

    private CheckBox chkEnableBTC;
    private CheckBox chkEnableBCH;
    private CheckBox chkEnableLTC;
    private CheckBox chkEnableETH;
    private CheckBox chkEnableZEC;
    private CheckBox chkEnableDSH;

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

        chkEnableBTC = new CheckBox(BFConstants.BITCOIN);
        chkEnableBCH = new CheckBox(BFConstants.BITCOIN_CASH);
        chkEnableLTC = new CheckBox(BFConstants.LITECOIN);
        chkEnableETH = new CheckBox(BFConstants.ETHERIUM);
        chkEnableZEC = new CheckBox(BFConstants.ZCASH);
        chkEnableDSH = new CheckBox(BFConstants.DASH_COIN);

        chkAutoSortByDeltaPercent = new CheckBox("Auto sort by \"Delta %\" column after refresh");

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

        GridLayout coinbaseSettingsGridLayout = new GridLayout(2, 2);
        coinbaseSettingsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        coinbaseSettingsGridLayout.addComponent(new Label("API key:"), 0, 0);
        coinbaseSettingsGridLayout.addComponent(txtKraApiKey, 1, 0);
        coinbaseSettingsGridLayout.addComponent(new Label("Secret:"), 0, 1);
        coinbaseSettingsGridLayout.addComponent(txtKraSecretKey, 1, 1);
        coinbaseSettingsGridLayout.setSpacing(true);

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

                    settings.setProperty(BFConstants.BITCOIN, chkEnableBTC.getValue().toString());
                    settings.setProperty(BFConstants.BITCOIN_CASH, chkEnableBCH.getValue().toString());
                    settings.setProperty(BFConstants.LITECOIN, chkEnableLTC.getValue().toString());
                    settings.setProperty(BFConstants.ETHERIUM, chkEnableETH.getValue().toString());
                    settings.setProperty(BFConstants.ZCASH, chkEnableZEC.getValue().toString());
                    settings.setProperty(BFConstants.DASH_COIN, chkEnableDSH.getValue().toString());

                    settings.setProperty(BFConstants.AUTO_SORT, chkAutoSortByDeltaPercent.getValue().toString());

                    settings.updateCoinSelectState(chkEnableBTC, chkEnableBCH, chkEnableLTC, chkEnableETH, chkEnableZEC, chkEnableDSH);
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
        apiVerticalHolder.addComponent(coinbaseSettingsGridLayout);

        GridLayout coinCheckBoxesGrid = new GridLayout(6, 1);
        coinCheckBoxesGrid.addComponent(chkEnableBTC);
        coinCheckBoxesGrid.addComponent(chkEnableBCH);
        coinCheckBoxesGrid.addComponent(chkEnableLTC);
        coinCheckBoxesGrid.addComponent(chkEnableETH);
        coinCheckBoxesGrid.addComponent(chkEnableZEC);
        coinCheckBoxesGrid.addComponent(chkEnableDSH);
        coinCheckBoxesGrid.setWidth("100%");

        VerticalLayout verticalDumb = new VerticalLayout();
        verticalDumb.addComponent(coinCheckBoxesGrid);
        verticalDumb.setComponentAlignment(coinCheckBoxesGrid, MIDDLE_CENTER);

        Panel coinCheckBoxesPanel = new Panel("Enabled coins for monitoring");
        coinCheckBoxesPanel.addStyleName(ValoTheme.PANEL_WELL);
        coinCheckBoxesPanel.setContent(verticalDumb);

        VerticalLayout otherVerticalHolder = new VerticalLayout();
        otherVerticalHolder.addComponent(chkAutoSortByDeltaPercent);
        otherVerticalHolder.addComponent(coinCheckBoxesPanel);

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

    private void updateValuesToUI() {
        txtWexApiKey.setValue(settings.getProperty(BFConstants.WEX_API_KEY));
        txtWexSecretKey.setValue(settings.getProperty(BFConstants.WEX_API_SECRET));

        txtBitApiKey.setValue(settings.getProperty(BFConstants.BIT_API_KEY));
        txtBitSecretKey.setValue(settings.getProperty(BFConstants.BIT_API_SECRET));

        txtKraApiKey.setValue(settings.getProperty(BFConstants.KRA_API_KEY));
        txtKraSecretKey.setValue(settings.getProperty(BFConstants.KRA_API_SECRET));

        chkAutoSortByDeltaPercent.setValue(Boolean.valueOf(settings.getProperty(BFConstants.AUTO_SORT)));

        chkEnableBTC.setValue(Boolean.valueOf(settings.getProperty(BFConstants.BITCOIN)));
        chkEnableBCH.setValue(Boolean.valueOf(settings.getProperty(BFConstants.BITCOIN_CASH)));
        chkEnableLTC.setValue(Boolean.valueOf(settings.getProperty(BFConstants.LITECOIN)));
        chkEnableETH.setValue(Boolean.valueOf(settings.getProperty(BFConstants.ETHERIUM)));
        chkEnableZEC.setValue(Boolean.valueOf(settings.getProperty(BFConstants.ZCASH)));
        chkEnableDSH.setValue(Boolean.valueOf(settings.getProperty(BFConstants.DASH_COIN)));

        settings.updateCoinSelectState(chkEnableBTC, chkEnableBCH, chkEnableLTC, chkEnableETH, chkEnableZEC, chkEnableDSH);
    }

}