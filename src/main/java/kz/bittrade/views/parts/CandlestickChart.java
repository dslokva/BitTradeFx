package kz.bittrade.views.parts;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.ui.Component;
import kz.bittrade.com.chartminer.OhlcData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CandlestickChart {

    public CandlestickChart() {
    }

    public Component getChart() {
        final Chart chart = new Chart(ChartType.CANDLESTICK);
        chart.setHeight("350px");
        chart.setWidth("50%");
        chart.setTimeline(true);

        Configuration configuration = chart.getConfiguration();
        configuration.getTitle().setText("Candlestick chart for 1 coin");

        DataGrouping grouping = new DataGrouping();
        grouping.addUnit(new TimeUnitMultiples(TimeUnit.MINUTE, 1, 2, 3, 4, 5, 6));
        grouping.addUnit(new TimeUnitMultiples(TimeUnit.HOUR, 1, 2, 3, 4, 6));

        PlotOptionsCandlestick plotOptionsCandlestick = new PlotOptionsCandlestick();
        plotOptionsCandlestick.setDataGrouping(grouping);
        plotOptionsCandlestick.setUpColor(SolidColor.LIMEGREEN);
        plotOptionsCandlestick.setColor(SolidColor.PALEVIOLETRED);

        DataSeries dataSeries = new DataSeries();
        dataSeries.setPlotOptions(plotOptionsCandlestick);

        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setSelected(4);

        for (OhlcData data : generateList()) {
            OhlcItem item = new OhlcItem();
            item.setX(data.getDate());
            item.setLow(data.getLow());
            item.setHigh(data.getHigh());
            item.setClose(data.getClose());
            item.setOpen(data.getOpen());
            dataSeries.add(item);
        }
        configuration.setSeries(dataSeries);
        configuration.setRangeSelector(rangeSelector);

        chart.drawChart(configuration);
        return chart;
    }

    private List<OhlcData> generateList() {
        List<OhlcData> data = new ArrayList<>();
        data.add(new OhlcData(new Date(System.currentTimeMillis() - 180000), 550.1, 570, 485.9, 500));
        data.add(new OhlcData(new Date(System.currentTimeMillis() - 120000), 500.1, 520, 485.9, 510));
        data.add(new OhlcData(new Date(System.currentTimeMillis() - 60000), 510, 519, 475.9, 520));
        data.add(new OhlcData(new Date(), 520.44, 539, 485.9, 530));
        data.add(new OhlcData(new Date(System.currentTimeMillis() + 60000), 530, 589, 485.9, 540));
        data.add(new OhlcData(new Date(System.currentTimeMillis() + 120000), 540, 564, 485.9, 550));
        data.add(new OhlcData(new Date(System.currentTimeMillis() + 180000), 550, 594, 425.9, 450.4));
        return data;
    }
}