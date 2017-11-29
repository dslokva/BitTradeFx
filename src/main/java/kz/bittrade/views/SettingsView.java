package kz.bittrade.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import kz.bittrade.BitTradeFx;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.markets.api.lib.WexNzPrivateApiAccessLib;

import static com.vaadin.ui.Alignment.MIDDLE_LEFT;

public class SettingsView extends VerticalLayout implements View {
    private TextField txtWexApiKey;
    private PasswordField txtWexSecretKey;
    private TextField txtBitApiKey;
    private PasswordField txtBitSecretKey;
    private TextField txtKraApiKey;
    private PasswordField txtKraSecretKey;
    private static AppSettingsHolder settings;
    private CheckBox chkAutoSortByDeltaPercent;
    private BitTradeFx mainui;

    public SettingsView() {
        txtWexApiKey = new TextField();
        txtWexSecretKey = new PasswordField();
        txtWexApiKey.setWidth("400px");
        txtWexSecretKey.setWidth("400px");

        txtBitApiKey = new TextField();
        txtBitSecretKey = new PasswordField();
        txtBitApiKey.setWidth("400px");
        txtBitSecretKey.setWidth("400px");

        txtKraApiKey = new TextField();
        txtKraSecretKey = new PasswordField();
        txtKraApiKey.setWidth("400px");
        txtKraSecretKey.setWidth("400px");

        settings = AppSettingsHolder.getInstance();
        mainui = (BitTradeFx) UI.getCurrent();

        GridLayout wexnzSettingsGridLayout = new GridLayout(2, 2);
        wexnzSettingsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        wexnzSettingsGridLayout.addComponent(new Label("API key:"), 0, 0);
        wexnzSettingsGridLayout.addComponent(txtWexApiKey, 1, 0);
        wexnzSettingsGridLayout.addComponent(new Label("Secret:"), 0, 1);
        wexnzSettingsGridLayout.addComponent(txtWexSecretKey, 1, 1);
        wexnzSettingsGridLayout.setWidth("420px");
        wexnzSettingsGridLayout.setSpacing(true);

        GridLayout bitfinexSettingsGridLayout = new GridLayout(2, 2);
        bitfinexSettingsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        bitfinexSettingsGridLayout.addComponent(new Label("API key:"), 0, 0);
        bitfinexSettingsGridLayout.addComponent(txtBitApiKey, 1, 0);
        bitfinexSettingsGridLayout.addComponent(new Label("Secret:"), 0, 1);
        bitfinexSettingsGridLayout.addComponent(txtBitSecretKey, 1, 1);
        bitfinexSettingsGridLayout.setWidth("420px");
        bitfinexSettingsGridLayout.setSpacing(true);

        GridLayout coinbaseSettingsGridLayout = new GridLayout(2, 2);
        coinbaseSettingsGridLayout.setDefaultComponentAlignment(MIDDLE_LEFT);
        coinbaseSettingsGridLayout.addComponent(new Label("API key:"), 0, 0);
        coinbaseSettingsGridLayout.addComponent(txtKraApiKey, 1, 0);
        coinbaseSettingsGridLayout.addComponent(new Label("Secret:"), 0, 1);
        coinbaseSettingsGridLayout.addComponent(txtKraSecretKey, 1, 1);
        coinbaseSettingsGridLayout.setWidth("420px");
        coinbaseSettingsGridLayout.setSpacing(true);

        Button btnBack = new Button("<- Back");
        btnBack.addClickListener(
                e -> getUI().getNavigator().navigateTo("main"));

        Button btnSave = new Button("Save");
        btnSave.addClickListener(
                e -> {
                    if (!WexNzPrivateApiAccessLib.isValidAPIKey(txtWexApiKey.getValue()))
                        Notification.show("Wex.nz API key has invalid structure!", Notification.Type.ERROR_MESSAGE);
                    else if (!WexNzPrivateApiAccessLib.isValidSecret(txtWexSecretKey.getValue()))
                        Notification.show("Wex.nz Secret key has invalid structure!", Notification.Type.ERROR_MESSAGE);
                    else {
                        settings.setProperty(BFConstants.WEX_API_KEY, txtWexApiKey.getValue());
                        settings.setProperty(BFConstants.WEX_API_SECRET, txtWexSecretKey.getValue());
                        settings.setProperty(BFConstants.BIT_API_KEY, txtBitApiKey.getValue());
                        settings.setProperty(BFConstants.BIT_API_SECRET, txtBitSecretKey.getValue());
                        settings.setProperty(BFConstants.KRA_API_KEY, txtKraApiKey.getValue());
                        settings.setProperty(BFConstants.KRA_API_SECRET, txtKraSecretKey.getValue());
                        mainui.showNotification("Settings", "Values are sent to local storage.", 3000, Position.BOTTOM_RIGHT, "tray success");
                    }
                }
        );

        chkAutoSortByDeltaPercent = new CheckBox("Auto sort by \"Delta %\" column after refresh");
        chkAutoSortByDeltaPercent.addValueChangeListener(event ->
                settings.setProperty(BFConstants.AUTO_SORT, chkAutoSortByDeltaPercent.getValue().toString()));

        addComponent(new Label("<b><h2>User settings</h2></b>", ContentMode.HTML));
        addComponent(new Label("<b>User preferences page for [WEX.nz] market:</b>", ContentMode.HTML));
        addComponent(wexnzSettingsGridLayout);
        addComponent(new Label("<b>User preferences page for [Bitfinex.com] market:</b>", ContentMode.HTML));
        addComponent(bitfinexSettingsGridLayout);
        addComponent(new Label("<b>User preferences page for [Kraken.com] market:</b>", ContentMode.HTML));
        addComponent(coinbaseSettingsGridLayout);
        addComponent(chkAutoSortByDeltaPercent);
        addComponent(new HorizontalLayout(btnSave, btnBack));
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
    }

}
