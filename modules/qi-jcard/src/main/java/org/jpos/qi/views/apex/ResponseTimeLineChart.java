package org.jpos.qi.views.apex;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.ResponsiveBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.responsive.builder.OptionsBuilder;
import com.github.appreciated.apexcharts.helper.Series;

public class ResponseTimeLineChart extends ApexChartsBuilder {

	public ResponseTimeLineChart() {
		String[] labels = {"Absent"};
		Double[] values = {0.0};
		new ResponseTimeLineChart("",480.0,labels,values);
	}
	
    public ResponseTimeLineChart(String chartLabel, Double maxScreenWidth, String[] labels, Double[] values) {
    	TitleSubtitle responseTimeTitle = new TitleSubtitle();
    	responseTimeTitle.setText(chartLabel);
        withChart(ChartBuilder.get()
        			.withType(Type.line)
        			.withToolbar(ToolbarBuilder.get().withShow(false).build())
        			.build())
        		.withTitle(responseTimeTitle)
				.withLabels(labels)
                .withLegend(LegendBuilder.get().withPosition(Position.right).build())
				.withSeries(new Series<>(chartLabel,values))
                .withResponsive(ResponsiveBuilder.get()
						.withBreakpoint(maxScreenWidth)
                        .withOptions(OptionsBuilder.get().
                        		withLegend(LegendBuilder.get().withPosition(Position.bottom).build())
                        		.build())
                        .build());
    }
    
}
