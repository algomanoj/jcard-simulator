package org.jpos.qi.views.apex;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.FillBuilder;
import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.StrokeBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.helper.Series;

public class TopMerchantVerticalBarChart extends ApexChartsBuilder {

	public TopMerchantVerticalBarChart() {
		String[] labels = {"Absent"};
		String[] values = {"0.0"};
		new TopMerchantVerticalBarChart("",480.0,labels,values);
	}
	
    public TopMerchantVerticalBarChart(String chartLabel,Double maxScreenWidth, String[] labels, String[] values) {
    	TitleSubtitle topMerchantTitle = new TitleSubtitle();
    	topMerchantTitle.setText(chartLabel);
        withChart(ChartBuilder.get() 
        			.withType(Type.bar)
        			.withToolbar(ToolbarBuilder.get().withShow(false).build())
        			.build())
        		.withTitle(topMerchantTitle)
                .withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(false).withColumnWidth("55%").build()).build())
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withStroke(StrokeBuilder.get().withShow(true).withWidth(2.0).withColors("transparent").build())
                .withSeries(new Series<>(chartLabel,values))
                .withXaxis(XAxisBuilder.get().withCategories(labels).build())
                .withFill(FillBuilder.get().withOpacity(1.0).build());
    }
}
