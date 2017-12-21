package kz.bittrade.views.parts;


import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.com.db.DBConnector;
import kz.bittrade.com.db.model.FlatUSDCoinData;

import java.util.ArrayList;
import java.util.List;

public class CompareSeriesChart extends Chart {
    private final DBConnector dbConnector;
    private AppSettingsHolder settings;

    private Configuration chartConfiguration;
    private List<FlatUSDCoinData> wexCoins;
    private List<FlatUSDCoinData> cexCoins;
    private List<FlatUSDCoinData> krakenCoins;
    private List<FlatUSDCoinData> bitfinexCoins;

    private DataSeries bitfinexSeries;
    private DataSeries krakenSeries;
    private DataSeries cexSeries;
    private DataSeries wexSeries;

    private boolean wexEnabled;
    private boolean bitEnabled;
    private boolean kraEnabled;
    private boolean cexEnabled;

    public CompareSeriesChart(AppSettingsHolder settings) {
        super();
        this.settings = settings;

        dbConnector = new DBConnector();
        chartConfiguration = getConfiguration();

        wexSeries = new DataSeries();
        bitfinexSeries = new DataSeries();
        cexSeries = new DataSeries();
        krakenSeries = new DataSeries();

        wexCoins = new ArrayList<>();
        bitfinexCoins = new ArrayList<>();
        cexCoins = new ArrayList<>();
        krakenCoins = new ArrayList<>();

        wexSeries.setName(BFConstants.WEX);
        bitfinexSeries.setName(BFConstants.BITFINEX);
        krakenSeries.setName(BFConstants.KRAKEN);
        cexSeries.setName(BFConstants.CEX);

        initChart();
    }

    private void initChart() {
        setHeight("450px");
        setWidth("100%");
        setTimeline(true);

        chartConfiguration.getChart().setMarginLeft(120);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Price changes");
        Labels label = new Labels();
        label.setFormatter("(this.value > 0 ? ' + ' : '') + this.value + '%'");
        yAxis.setLabels(label);

        PlotLine plotLine = new PlotLine();
        plotLine.setValue(2);
        plotLine.setWidth(2);
        plotLine.setColor(SolidColor.SILVER);
        yAxis.setPlotLines(plotLine);
        chartConfiguration.addyAxis(yAxis);

        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat("<span style=\"color:{series.color}\">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>");
        tooltip.setValueDecimals(2);
        chartConfiguration.setTooltip(tooltip);

        extractCoinsData();

        chartConfiguration.addSeries(wexSeries);
        chartConfiguration.addSeries(bitfinexSeries);
        chartConfiguration.addSeries(krakenSeries);
        chartConfiguration.addSeries(cexSeries);

        PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
        plotOptionsSeries.setCompare(Compare.PERCENT);
        chartConfiguration.setPlotOptions(plotOptionsSeries);

        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setButtons(new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 10, "10 m"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 30, "30 m"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 60, "1 h"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 360, "6 h"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 720, "12 h"),
                new RangeSelectorButton(RangeSelectorTimespan.MINUTE, 1440, "24 h"));
        rangeSelector.setSelected(4);
        chartConfiguration.setRangeSelector(rangeSelector);

        Legend legend = new Legend();
        legend.getTitle().setText("Market:");
        legend.setFloating(false);
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setAlign(HorizontalAlign.LEFT);
        legend.setVerticalAlign(VerticalAlign.MIDDLE);
        legend.setX(0);
        legend.setY(0);
        legend.setShadow(true);
        legend.setEnabled(true);
        chartConfiguration.setLegend(legend);

        drawChart(chartConfiguration);
    }

    private void updateSettings(AppSettingsHolder settings) {
        wexEnabled = settings.isPropertyEnabled(BFConstants.WEX);
        bitEnabled = settings.isPropertyEnabled(BFConstants.BITFINEX);
        kraEnabled = settings.isPropertyEnabled(BFConstants.KRAKEN);
        cexEnabled = settings.isPropertyEnabled(BFConstants.CEX);
    }

    private void getDataFromDB(Integer coinId, int intervalInDays) {
        updateSettings(settings);

        if (wexEnabled) wexCoins = dbConnector.selectDataCoinMarketId(BFConstants.WEX_ID, coinId, intervalInDays);
        if (cexEnabled) cexCoins = dbConnector.selectDataCoinMarketId(BFConstants.CEX_ID, coinId, intervalInDays);
        if (kraEnabled) krakenCoins = dbConnector.selectDataCoinMarketId(BFConstants.KRAKEN_ID, coinId, intervalInDays);
        if (bitEnabled)
            bitfinexCoins = dbConnector.selectDataCoinMarketId(BFConstants.BITFINEX_ID, coinId, intervalInDays);

        chartConfiguration.getTitle().setText(BFConstants.getCoinNameById(coinId) + " price (comparig markets deviation for a period)");
    }

    private void extractCoinsData() {
        if (wexEnabled) extractCoinData(wexSeries, wexCoins);
        else wexSeries.clear();
        wexSeries.setVisible(wexEnabled);

        if (bitEnabled) extractCoinData(bitfinexSeries, bitfinexCoins);
        else bitfinexSeries.clear();
        bitfinexSeries.setVisible(bitEnabled);

        if (kraEnabled) extractCoinData(krakenSeries, krakenCoins);
        else krakenSeries.clear();
        krakenSeries.setVisible(kraEnabled);

        if (cexEnabled) extractCoinData(cexSeries, cexCoins);
        else cexSeries.clear();
        cexSeries.setVisible(cexEnabled);
    }

    private void extractCoinData(DataSeries chartSeries, List<FlatUSDCoinData> coinsList) {
        List<DataSeriesItem> dataList = new ArrayList<>();
        for (FlatUSDCoinData data : coinsList) {
            DataSeriesItem item = new DataSeriesItem();
            item.setX(data.getTimestamp());
            item.setY(data.getRate());
            dataList.add(item);
        }
        chartSeries.setData(dataList);
    }

    public void refreshDataByCoin(Integer coinId) {
        getDataFromDB(coinId, BFConstants.DAY_1);
        extractCoinsData();
        drawChart(chartConfiguration);
    }


    //    public void removeListSeries(String seriesName) {
    //        chart.drawChart();
    //        chart.getConfiguration().setSeries(aList);
    //
    //        }
    //                aList[i++] = scq;
    //            if (!scq.getName().equals(seriesName))
    //        for (Series scq : sc) {
    //        int i = 0;
    //        Series[] aList = new Series[sc.size()];
    //
    //        List<Series> sc = configuration.getSeries();
    //        Configuration configuration = chart.getConfiguration();
    //    }
}