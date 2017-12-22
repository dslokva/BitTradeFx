package kz.bittrade.views.parts;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.com.db.DBConnector;
import kz.bittrade.com.db.model.FlatUSDCoinData;

import java.util.ArrayList;
import java.util.List;

public abstract class BitTradeFxDataSeriesChart extends Chart {
    protected final DBConnector dbConnector;
    protected AppSettingsHolder settings;

    protected Configuration chartConfiguration;
    protected List<FlatUSDCoinData> wexCoins;
    protected List<FlatUSDCoinData> cexCoins;
    protected List<FlatUSDCoinData> krakenCoins;
    protected List<FlatUSDCoinData> bitfinexCoins;

    protected DataSeries bitfinexSeries;
    protected DataSeries krakenSeries;
    protected DataSeries cexSeries;
    protected DataSeries wexSeries;

    protected boolean wexEnabled;
    protected boolean bitEnabled;
    protected boolean kraEnabled;
    protected boolean cexEnabled;

    public BitTradeFxDataSeriesChart(AppSettingsHolder settings) {
        super();
        this.settings = settings;
        dbConnector = new DBConnector();
        chartConfiguration = getConfiguration();

        wexSeries = new DataSeries();
        bitfinexSeries = new DataSeries();
        cexSeries = new DataSeries();
        krakenSeries = new DataSeries();

        wexSeries.setName(BFConstants.WEX);
        bitfinexSeries.setName(BFConstants.BITFINEX);
        krakenSeries.setName(BFConstants.KRAKEN);
        cexSeries.setName(BFConstants.CEX);

        wexCoins = new ArrayList<>();
        bitfinexCoins = new ArrayList<>();
        cexCoins = new ArrayList<>();
        krakenCoins = new ArrayList<>();

        initChart();
    }

    protected void updateSettings(AppSettingsHolder settings) {
        wexEnabled = settings.isPropertyEnabled(BFConstants.WEX);
        bitEnabled = settings.isPropertyEnabled(BFConstants.BITFINEX);
        kraEnabled = settings.isPropertyEnabled(BFConstants.KRAKEN);
        cexEnabled = settings.isPropertyEnabled(BFConstants.CEX);
    }

    protected RangeSelector getRangeSelector() {
        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setButtons(new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 10, "10 m"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 30, "30 m"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 60, "1 h"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 360, "6 h"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 720, "12 h"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 1439, "24 h"));
        rangeSelector.setSelected(4);
        return rangeSelector;
    }

    abstract void initChart();

    public abstract void refreshDataByCoin(Integer coinId);

}
