package kz.bittrade.views;

import com.vaadin.contextmenu.GridContextMenu;
import com.vaadin.contextmenu.Menu;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.SerializableComparator;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import kz.bittrade.BitTradeFx;
import kz.bittrade.com.BFConstants;
import kz.bittrade.markets.api.holders.currency.CurrencyPairsHolder;

import java.util.ArrayList;
import java.util.List;

import static com.vaadin.ui.Alignment.MIDDLE_LEFT;
import static com.vaadin.ui.Alignment.MIDDLE_RIGHT;

public class MainGrid extends Grid<CurrencyPairsHolder> {
    private BitTradeFx mainui;
    private CoinActionsWindow coinActionsWindow;

    public MainGrid(BitTradeFx mainui, CoinActionsWindow coinActionsWindow) {
        super();
        this.mainui = mainui;
        this.coinActionsWindow = coinActionsWindow;
        initMainGrid();
    }

    private void initMainGrid() {
        setSelectionMode(Grid.SelectionMode.NONE);
        setCaption("Currency information");
        setItems(mainui.getCurrencyPairsHolderList());

        addComponentColumn((CurrencyPairsHolder currencyPairRow) -> {
            Button buttonActions = new Button("");
            buttonActions.setIcon(VaadinIcons.ELLIPSIS_DOTS_H);
            buttonActions.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
            buttonActions.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            buttonActions.setDescription("Trade actions");
            buttonActions.setWidth("35px");
            buttonActions.addClickListener(click -> {
                UI.getCurrent().addWindow(coinActionsWindow);
            });

            Button buttonRefresh = new Button("");
            buttonRefresh.setIcon(VaadinIcons.REFRESH);
            buttonRefresh.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
            buttonRefresh.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            buttonRefresh.setDescription("Refresh row");
            buttonRefresh.addClickListener(click -> {
                mainui.refreshCurrencyGrid(currencyPairRow);
            });

            HorizontalLayout btnPanel = new HorizontalLayout();
            btnPanel.addComponents(buttonRefresh, buttonActions);
            btnPanel.setMargin(false);
            btnPanel.setSpacing(false);

            Label label = new Label(currencyPairRow.getDisplayName(), ContentMode.HTML);

            GridLayout glayout = new GridLayout(2, 1);
            glayout.addComponent(label, 0, 0);
            glayout.addComponent(btnPanel, 1, 0);
            glayout.setComponentAlignment(label, MIDDLE_LEFT);
            glayout.setComponentAlignment(btnPanel, MIDDLE_RIGHT);
            glayout.setWidth("170px");
            glayout.setHeight("100%");
            glayout.setSpacing(false);
            glayout.setMargin(false);

            return glayout;
        }).setCaption("Pair name")
                .setWidth(190)
                .setResizable(false)
                .setSortable(false)
                .setId(BFConstants.GRID_PAIR_NAME_COLUMN);

        addColumn(CurrencyPairsHolder::getDeltaStringPercent, new HtmlRenderer())
                .setCaption("Delta %")
                .setWidth(100)
                .setId(BFConstants.GRID_DELTA_PERCENT_COLUMN)
                .setComparator(
                        new SerializableComparator<CurrencyPairsHolder>() {
                            @Override
                            public int compare(CurrencyPairsHolder a, CurrencyPairsHolder b) {
                                return Double.compare(a.getDeltaDoublePercent(), b.getDeltaDoublePercent());
                            }
                        }
                );
        addColumn(CurrencyPairsHolder::getDeltaString, new HtmlRenderer())
                .setCaption("Delta $")
                .setWidth(100)
                .setId(BFConstants.GRID_DELTA_DOUBLE_COLUMN)
                .setComparator(
                        new SerializableComparator<CurrencyPairsHolder>() {
                            @Override
                            public int compare(CurrencyPairsHolder a, CurrencyPairsHolder b) {
                                return Double.compare(a.getDeltaDouble(), b.getDeltaDouble());
                            }
                        }
                );
        addColumn(CurrencyPairsHolder::getLastPriceWex, new HtmlRenderer())
                .setCaption(BFConstants.WEX)
                .setExpandRatio(2)
                .setResizable(false)
                .setMinimumWidth(127)
                .setId(BFConstants.GRID_WEX_COLUMN)
                .setComparator(
                        new SerializableComparator<CurrencyPairsHolder>() {
                            @Override
                            public int compare(CurrencyPairsHolder a, CurrencyPairsHolder b) {
                                return Double.compare(a.getWexnzPair().getLastPriceDouble(), b.getWexnzPair().getLastPriceDouble());
                            }
                        }
                );
        addColumn(CurrencyPairsHolder::getLastPriceBitfinex, new HtmlRenderer())
                .setCaption(BFConstants.BITFINEX)
                .setExpandRatio(2)
                .setResizable(false)
                .setMinimumWidth(127)
                .setId(BFConstants.GRID_BITFINEX_COLUMN)
                .setComparator(
                        new SerializableComparator<CurrencyPairsHolder>() {
                            @Override
                            public int compare(CurrencyPairsHolder a, CurrencyPairsHolder b) {
                                return Double.compare(a.getBitfinexPair().getLastPriceDouble(), b.getBitfinexPair().getLastPriceDouble());
                            }
                        }
                );
        addColumn(CurrencyPairsHolder::getLastPriceKraken, new HtmlRenderer())
                .setCaption(BFConstants.KRAKEN)
                .setExpandRatio(2)
                .setResizable(false)
                .setMinimumWidth(127)
                .setId(BFConstants.GRID_KRAKEN_COLUMN)
                .setComparator(
                        new SerializableComparator<CurrencyPairsHolder>() {
                            @Override
                            public int compare(CurrencyPairsHolder a, CurrencyPairsHolder b) {
                                return Double.compare(a.getKrakenPair().getLastPriceDouble(), b.getKrakenPair().getLastPriceDouble());
                            }
                        }
                );
        addColumn(CurrencyPairsHolder::getLastPriceCex, new HtmlRenderer())
                .setCaption(BFConstants.CEX)
                .setExpandRatio(2)
                .setResizable(false)
                .setMinimumWidth(127)
                .setId(BFConstants.GRID_CEX_COLUMN)
                .setComparator(
                        new SerializableComparator<CurrencyPairsHolder>() {
                            @Override
                            public int compare(CurrencyPairsHolder a, CurrencyPairsHolder b) {
                                return Double.compare(a.getCexPair().getLastPriceDouble(), b.getCexPair().getLastPriceDouble());
                            }
                        }
                );
        setSizeFull();

        final List<String> nonFunctionalColumns = new ArrayList<>();
        nonFunctionalColumns.add(BFConstants.GRID_DELTA_PERCENT_COLUMN);
        nonFunctionalColumns.add(BFConstants.GRID_DELTA_DOUBLE_COLUMN);
        nonFunctionalColumns.add(BFConstants.GRID_PAIR_NAME_COLUMN);

        final GridContextMenu<CurrencyPairsHolder> gridContextMenu = new GridContextMenu<>(this);

        gridContextMenu.addGridBodyContextMenuListener((GridContextMenu.GridContextMenuOpenListener<CurrencyPairsHolder>) grid -> {
            gridContextMenu.removeItems();
            if (grid.getItem() != null && !nonFunctionalColumns.contains(grid.getColumn().getId())) {
                gridContextMenu.addItem("Go to market website", VaadinIcons.GLOBE, (Menu.Command) e -> {
                    String url;

                    switch (grid.getColumn().getId()) {
                        case BFConstants.GRID_WEX_COLUMN: {
                            url = ((CurrencyPairsHolder) grid.getItem()).getWexnzPair().getUrlToMarket();
                            break;
                        }
                        case BFConstants.GRID_BITFINEX_COLUMN: {
                            url = ((CurrencyPairsHolder) grid.getItem()).getBitfinexPair().getUrlToMarket();
                            break;
                        }
                        case BFConstants.GRID_KRAKEN_COLUMN: {
                            url = ((CurrencyPairsHolder) grid.getItem()).getKrakenPair().getUrlToMarket();
                            break;
                        }
                        case BFConstants.GRID_CEX_COLUMN: {
                            url = ((CurrencyPairsHolder) grid.getItem()).getCexPair().getUrlToMarket();
                            break;
                        }
                        default: {
                            url = "https://www.google.kz/search?&q=internal error occured";
                        }
                    }
                    Page.getCurrent().open(url, "_blank", false);
                });
            }
        });
    }
}