package kz.bittrade.views.parts;


import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.ui.Component;
import kz.bittrade.com.BFConstants;
import kz.bittrade.com.db.DBConnector;
import kz.bittrade.com.db.model.FlatUSDCoinData;

import java.util.List;

public class CompareSeriesChart {
    private final DBConnector dbConnector;
    private List<FlatUSDCoinData> wexCoins;
    private List<FlatUSDCoinData> cexCoins;
    private List<FlatUSDCoinData> krakenCoins;
    private List<FlatUSDCoinData> bitfinexCoins;

    public CompareSeriesChart() {
        dbConnector = new DBConnector();
        getDataFromDB();
    }

    private void getDataFromDB() {
        wexCoins = dbConnector.selectDataCoinMarketId(BFConstants.WEX_ID, BFConstants.BTC_ID);
        cexCoins = dbConnector.selectDataCoinMarketId(BFConstants.CEX_ID, BFConstants.BTC_ID);
        krakenCoins = dbConnector.selectDataCoinMarketId(BFConstants.KRAKEN_ID, BFConstants.BTC_ID);
        bitfinexCoins = dbConnector.selectDataCoinMarketId(BFConstants.BITFINEX_ID, BFConstants.BTC_ID);
    }

    public Component getChart() {
        final Chart chart = new Chart();
        chart.setHeight("450px");
        chart.setWidth("80%");
        chart.setTimeline(true);

        Configuration configuration = chart.getConfiguration();
        configuration.getTitle().setText("Bitcoin market price");

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
        configuration.addyAxis(yAxis);

        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat("<span style=\"color:{series.color}\">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>");
        tooltip.setValueDecimals(2);
        configuration.setTooltip(tooltip);

        DataSeries wexSeries = new DataSeries();
        wexSeries.setName(BFConstants.WEX);
        if (wexCoins != null)
            extractCoinData(wexSeries, wexCoins);

        DataSeries bitfinexSeries = new DataSeries();
        bitfinexSeries.setName(BFConstants.BITFINEX);
        extractCoinData(bitfinexSeries, bitfinexCoins);

        DataSeries krakenSeries = new DataSeries();
        krakenSeries.setName(BFConstants.KRAKEN);
        extractCoinData(krakenSeries, krakenCoins);

        DataSeries cexSeries = new DataSeries();
        cexSeries.setName(BFConstants.CEX);
        extractCoinData(cexSeries, cexCoins);

        configuration.addSeries(wexSeries);
        configuration.addSeries(bitfinexSeries);
        configuration.addSeries(krakenSeries);
        configuration.addSeries(cexSeries);

        PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
        plotOptionsSeries.setCompare(Compare.PERCENT);
        configuration.setPlotOptions(plotOptionsSeries);

        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setSelected(4);
        configuration.setRangeSelector(rangeSelector);

        Legend legend = new Legend();
        legend.getTitle().setText("Market");
        legend.setFloating(false);
        legend.setLayout(LayoutDirection.HORIZONTAL);
        legend.setAlign(HorizontalAlign.CENTER);
        legend.setVerticalAlign(VerticalAlign.MIDDLE);
        legend.setX(-250d);
        legend.setY(127d);
//        legend.setFloating(true);
        legend.setShadow(true);
        legend.setEnabled(true);
        configuration.setLegend(legend);

        chart.drawChart(configuration);
        return chart;
    }

    private void extractCoinData(DataSeries chartSeries, List<FlatUSDCoinData> coinsList) {
        for (FlatUSDCoinData data : coinsList) {
            DataSeriesItem item = new DataSeriesItem();
            item.setX(data.getTimestamp());
            item.setY(data.getRate());
            chartSeries.add(item);
        }
    }
}