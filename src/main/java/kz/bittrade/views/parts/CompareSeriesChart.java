package kz.bittrade.views.parts;


import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.ui.Component;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.com.db.DBConnector;
import kz.bittrade.com.db.model.FlatUSDCoinData;

import java.util.ArrayList;
import java.util.List;

public class CompareSeriesChart {
    private final DBConnector dbConnector;
    private List<FlatUSDCoinData> wexCoins;
    private List<FlatUSDCoinData> cexCoins;
    private List<FlatUSDCoinData> krakenCoins;
    private List<FlatUSDCoinData> bitfinexCoins;

    private DataSeries bitfinexSeries;
    private DataSeries krakenSeries;
    private DataSeries cexSeries;
    private DataSeries wexSeries;
    private Configuration chartConfiguration;
    private Chart chart;
    private AppSettingsHolder settings;
    private boolean wexEnabled;
    private boolean bitEnabled;
    private boolean kraEnabled;
    private boolean cexEnabled;

    public CompareSeriesChart(AppSettingsHolder settings) {
        this.settings = settings;

        dbConnector = new DBConnector();
        chart = new Chart();
        chartConfiguration = chart.getConfiguration();

        wexSeries = new DataSeries();
        bitfinexSeries = new DataSeries();
        cexSeries = new DataSeries();
        krakenSeries = new DataSeries();

        wexSeries.setName(BFConstants.WEX);
        bitfinexSeries.setName(BFConstants.BITFINEX);
        krakenSeries.setName(BFConstants.KRAKEN);
        cexSeries.setName(BFConstants.CEX);

        getDataFromDB(BFConstants.BTC_ID);
    }

    private void updateSettings(AppSettingsHolder settings) {
        wexEnabled = settings.isPropertyEnabled(BFConstants.WEX);
        bitEnabled = settings.isPropertyEnabled(BFConstants.BITFINEX);
        kraEnabled = settings.isPropertyEnabled(BFConstants.KRAKEN);
        cexEnabled = settings.isPropertyEnabled(BFConstants.CEX);
    }

    private void getDataFromDB(Integer coinId) {
        updateSettings(settings);

        if (wexEnabled) wexCoins = dbConnector.selectDataCoinMarketId(BFConstants.WEX_ID, coinId);
        if (cexEnabled) cexCoins = dbConnector.selectDataCoinMarketId(BFConstants.CEX_ID, coinId);
        if (kraEnabled) krakenCoins = dbConnector.selectDataCoinMarketId(BFConstants.KRAKEN_ID, coinId);
        if (bitEnabled) bitfinexCoins = dbConnector.selectDataCoinMarketId(BFConstants.BITFINEX_ID, coinId);

        chartConfiguration.getTitle().setText(BFConstants.getCoinNameById(coinId) + " price (comparig markets deviation for a period)");
    }

    public Component getChart() {
        chart.setHeight("450px");
        chart.setWidth("100%");
        chart.setTimeline(true);

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
//        legend.setFloating(true);
        legend.setShadow(true);
        legend.setEnabled(true);
        chartConfiguration.setLegend(legend);

        chart.drawChart(chartConfiguration);
        return chart;
    }

    private void extractCoinsData() {
        if (wexEnabled) extractCoinData(wexSeries, wexCoins);
        else wexSeries.clear();
        if (bitEnabled) extractCoinData(bitfinexSeries, bitfinexCoins);
        else bitfinexSeries.clear();
        if (kraEnabled) extractCoinData(krakenSeries, krakenCoins);
        else krakenSeries.clear();
        if (cexEnabled) extractCoinData(cexSeries, cexCoins);
        else cexSeries.clear();
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

//    public void removeListSeries(String seriesName) {
//        Configuration configuration = chart.getConfiguration();
//        List<Series> sc = configuration.getSeries();
//
//        Series[] aList = new Series[sc.size()];
//        int i = 0;
//        for (Series scq : sc) {
//            if (!scq.getName().equals(seriesName))
//                aList[i++] = scq;
//        }
//
//        chart.getConfiguration().setSeries(aList);
//        chart.drawChart();
//    }

    public void refreshDataByCoin(Integer coinId) {
        getDataFromDB(coinId);
        extractCoinsData();
        chart.drawChart(chartConfiguration);
    }
}