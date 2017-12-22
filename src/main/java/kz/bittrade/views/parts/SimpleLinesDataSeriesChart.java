package kz.bittrade.views.parts;

import com.vaadin.addon.charts.model.*;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import kz.bittrade.com.db.model.FlatUSDCoinData;

import java.util.ArrayList;
import java.util.List;

public class SimpleLinesDataSeriesChart extends BitTradeFxDataSeriesChart {

    public SimpleLinesDataSeriesChart(AppSettingsHolder settings) {
        super(settings);
    }

    @Override
    void initChart() {
        setHeight("450px");
        setWidth("100%");
        setTimeline(true);

        Tooltip tooltip = new Tooltip();
        tooltip.setShared(true);
        tooltip.setUseHTML(true);
        tooltip.setHeaderFormat("<small>{point.key}</small><table>");
        tooltip.setPointFormat("<tr><td style=\"color: {series.color}\">{series.name}: </td><td style=\"text-align: right\"><b>{point.y} EUR</b></td></tr>");
        tooltip.setFooterFormat("</table>");

        chartConfiguration.getChart().setType(ChartType.SPLINE);
        chartConfiguration.setTooltip(tooltip);
        chartConfiguration.getyAxis().setTitle("Price changes");
        extractCoinsData();

        chartConfiguration.addSeries(wexSeries);
        chartConfiguration.addSeries(bitfinexSeries);
        chartConfiguration.addSeries(krakenSeries);
        chartConfiguration.addSeries(cexSeries);

        RangeSelector rangeSelector = getRangeSelector();
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

    @Override
    public void refreshDataByCoin(Integer coinId) {
        getDataFromDB(coinId, BFConstants.DAY_1);
        extractCoinsData();
        drawChart(chartConfiguration);
    }

    private void getDataFromDB(Integer coinId, int intervalInDays) {
        updateSettings(settings);

        if (wexEnabled) wexCoins = dbConnector.selectDataCoinMarketId(BFConstants.WEX_ID, coinId, intervalInDays);
        if (cexEnabled) cexCoins = dbConnector.selectDataCoinMarketId(BFConstants.CEX_ID, coinId, intervalInDays);
        if (kraEnabled) krakenCoins = dbConnector.selectDataCoinMarketId(BFConstants.KRAKEN_ID, coinId, intervalInDays);
        if (bitEnabled)
            bitfinexCoins = dbConnector.selectDataCoinMarketId(BFConstants.BITFINEX_ID, coinId, intervalInDays);

        chartConfiguration.getTitle().setText(BFConstants.getCoinNameById(coinId) + " last markets prices for a period");
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

        wexSeries.setVisible(wexEnabled);
        bitfinexSeries.setVisible(bitEnabled);
        krakenSeries.setVisible(kraEnabled);
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

}
