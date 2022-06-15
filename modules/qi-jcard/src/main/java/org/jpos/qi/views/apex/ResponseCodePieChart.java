package org.jpos.qi.views.apex;

import java.util.Arrays;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.ResponsiveBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.responsive.builder.OptionsBuilder;

public class ResponseCodePieChart extends ApexChartsBuilder {

	public ResponseCodePieChart() { 
		String[] labels = {"Absent"};
		Double[] values = {0.0};
		new ResponseCodePieChart(null,480.0,labels,values);
	}
	
    public ResponseCodePieChart(String chartName, Double maxScreenWidth, String[] labels, Double[] values) {
    	TitleSubtitle responseCodeTitle = new TitleSubtitle();
    	responseCodeTitle.setText(chartName);
        withChart(ChartBuilder.get().withType(Type.pie).build())
				.withLabels(labels)
				.withTitle(responseCodeTitle)
                .withLegend(LegendBuilder.get().withPosition(Position.right).build())
				.withSeries(values).withColors(getColors(values.length))
                .withResponsive(ResponsiveBuilder.get()
						.withBreakpoint(maxScreenWidth)
                        .withOptions(OptionsBuilder.get()
                                .withLegend(LegendBuilder.get().withPosition(Position.bottom).build())
                                .build())
                        .build());
    }
    private String[] getColors(int colorLength) {
		String[] selectedColors = null;
		String[] colors = {"#F3B415", "#F27036", "#663F59", "#6A6E94", "#4E88B4", "#00A7C6", "#18D8D8", "#A9D794","#46AF78", "#A93F55", "#8C5E58"};
		if(colorLength<=11) {
			selectedColors = new String[colorLength];
			selectedColors=Arrays.asList(colors).toArray(selectedColors);
		}
		return selectedColors;
	}
}
