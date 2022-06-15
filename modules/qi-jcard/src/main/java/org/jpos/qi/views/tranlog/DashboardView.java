package org.jpos.qi.views.tranlog;
/*
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jpos.ee.TranLog;
import org.jpos.qi.QI;
import org.jpos.qi.services.TranLogHelper;
import org.jpos.qi.util.DateRange;
import org.jpos.qi.views.QIEntityView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
*/
public class DashboardView {} /*
extends QIEntityView<TranLog> {

	private static final long serialVersionUID = 1L;
	private Span noRecordFoundSpan;
	private Span transectionAmountsSpan;
	private Label transCount;
	private Label transAmt;
	
	private Chart responseCodePieChart;
	private Chart topMerchantBarChart;
	private Chart transactionTypeChart;
	private Chart responseTimeChart;
	private DataSeries responseCodeSeries;
	private DataSeries topMerchantSeries;
	private DataSeries transactionTypeSeries;
	private DataSeries responseTimeSeries;

	private ComboBox dateRangeCombo;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private Button refreshButton;

	private HorizontalLayout dashBoardGraphHL;
	
	private DateRange defaultDateRange = new DateRange(DateRange.TODAY);
	
	private static String CHART_WIDTH="90%";
	private static String CHART_HEIGHT="60%";
	private static String CHART_BORDER="2px solid gray";
	
	private List<TranLog> tlList;
	
	public DashboardView() {
		super(TranLog.class, "dashboard");
	}

	@Override
	public TranLogHelper createHelper() {
		return new TranLogHelper();
	}
	
	public TranLogHelper getHelper() {
		return (TranLogHelper) super.getHelper();
	}
	
	@Override
	public Component initContent() {
		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(getApp().getMessage(getName()));
		viewTitle.addClassNames("mt-s", "text-l");
		noRecordFoundSpan = new Span(new Icon(VaadinIcon.DATABASE), new Span(" No data found for specified duration"));
		noRecordFoundSpan.getStyle().set("font-weight", "bold");
		vl.add(viewTitle, getFilterBar(), noRecordFoundSpan);
		dashBoardGraphHL = getDashBoardGraphs();
		dashBoardGraphHL.setVisible(true);
		vl.add(dashBoardGraphHL);
		onRefreshButtonClick();
		return vl;
	}
	
	private HorizontalLayout getFilterBar() {
		HorizontalLayout row3 = new HorizontalLayout();
		row3.setWidth("100%");
		row3.setMargin(false);
		row3.setWidthFull();
		dateRangeCombo = createDateRange();
		startDatePicker = new DatePicker();
		endDatePicker = new DatePicker();
		startDatePicker.addValueChangeListener(e -> {
			endDatePicker.setMin(e.getValue());
			if (startDatePicker.getValue() != null) {
				dateRangeCombo.setValue(null);
			}
		});
		endDatePicker.addValueChangeListener(e -> {
			startDatePicker.setMax(e.getValue());
			if (endDatePicker.getValue() != null) {
				dateRangeCombo.setValue(null);
			}
		});

		refreshButton = new Button("Refresh");
		
		dateRangeCombo.getStyle().set("padding-right", "0px");
		startDatePicker.getStyle().set("padding-right", "5px");
		endDatePicker.getStyle().set("padding-right", "10px");
		
		transCount = new Label();
		transAmt = new Label();
		transectionAmountsSpan = new Span(new Icon(VaadinIcon.DATABASE), new Label(" Transactions: "),transCount,new Label(" "),new Icon(VaadinIcon.MONEY_EXCHANGE), new Label(" Amount: "),transAmt);
		transectionAmountsSpan.getStyle().set("margin-left", "20px");
		transectionAmountsSpan.getStyle().set("margin-top", "10px");
		transectionAmountsSpan.getStyle().set("font-weight", "bold");
		
		Label orLabel = new Label(" Or ");
		orLabel.getStyle().set("margin", "8px 5px");
		
		FlexLayout dateFieldLayout = new FlexLayout(dateRangeCombo, orLabel, startDatePicker,  endDatePicker, refreshButton, transectionAmountsSpan);
		refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		row3.add(dateFieldLayout);
		refreshButton.addClickListener(e -> {
			onRefreshButtonClick();
		});
		return row3;
	}

	private HorizontalLayout getDashBoardGraphs() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setMargin(false);
		hl.setWidthFull();

		initDefaultGraph();
		
		addDefaultGraph();

		populateGraphData();
		
		VerticalLayout row1VL = new VerticalLayout();
		VerticalLayout row2VL = new VerticalLayout();
		row1VL.add(new H5("Response Code"), responseCodePieChart );
		row1VL.add(new H5("Top Merchant"), topMerchantBarChart );
		row2VL.add(new H5("Transaction Type"), transactionTypeChart );
		row2VL.add(new H5("Response Time"), responseTimeChart );		
		hl.add(row1VL);
		hl.add(row2VL);
		
		return hl;
	}

	private void addDefaultGraph() {
		addDefaultGraph(responseCodePieChart, responseCodeSeries);
		addDefaultGraph(topMerchantBarChart, topMerchantSeries);
		addDefaultGraph(transactionTypeChart, transactionTypeSeries);
		addDefaultGraph(responseTimeChart, responseTimeSeries);
	}
	
	private void initDefaultGraph() {
		responseCodePieChart = new Chart(ChartType.PIE);
		topMerchantBarChart = new Chart(ChartType.COLUMN);
		transactionTypeChart = new Chart(ChartType.PIE);
		responseTimeChart = new Chart(ChartType.LINE);
		responseCodeSeries = new DataSeries();
		topMerchantSeries = new DataSeries();
		transactionTypeSeries = new DataSeries();
		responseTimeSeries = new DataSeries();
	}

	private void addDefaultGraph(Chart chart, DataSeries dataSeries) {
		chart.setWidth(CHART_WIDTH);
		chart.setHeight(CHART_HEIGHT);
		chart.getStyle().set("border", CHART_BORDER);
	}
	
	private void populateGraphData() {
		responseCodePieChart.getConfiguration().addSeries(responseCodeSeries);
		topMerchantBarChart .getConfiguration().addSeries(topMerchantSeries);
		transactionTypeChart.getConfiguration().addSeries(transactionTypeSeries);
		responseTimeChart.getConfiguration().addSeries(responseTimeSeries);
	}

	private void onRefreshButtonClick() {
		removeOldSeries();
		DateRange dr = null;
		if(dateRangeCombo.getValue()!=null) {
			dr =new DateRange(String.valueOf(dateRangeCombo.getValue()));
		}
		if(startDatePicker.getValue()!=null) {
			if(dr == null) {
				dr =new DateRange();
			}
			dr.setStart(parseLocalDate(startDatePicker.getValue()));
		}
		if(endDatePicker.getValue()!=null) {
			if(dr == null) {
				dr =new DateRange();
			}
			dr.setEnd(parseLocalDate(endDatePicker.getValue()));
		}
 		tlList = getHelper().getTlList(dr);
  		refreshGraphData(dr);
		return;
	}
    private Date parseLocalDate(LocalDate localDate) {
    	try {
        	return new SimpleDateFormat("yyyy-MM-dd").parse(localDate.toString());
		} catch (Exception e) {
        	return null;
		}
    }
	private void refreshGraphData(DateRange dr) {
		noRecordFoundSpan.setVisible(false);
		transectionAmountsSpan.setVisible(true);
		dashBoardGraphHL.setVisible(true);
		refreshAllChartData();
		if(tlList!=null && tlList.size()>0) {
			BigDecimal amt = new BigDecimal(0);
			for (TranLog tl : tlList) {
				if (tl.getAmount() != null) {
					amt = amt.add(tl.getAmount());
				}
			}
			transCount.setText(String.valueOf(tlList.size()));
			transAmt.setText(String.valueOf(amt));
		} else {
			noRecordFoundSpan.setVisible(true);
			transectionAmountsSpan.setVisible(false);
			dashBoardGraphHL.setVisible(false);
		}
	}

	private void refreshAllChartData() {
		refreshResponseCodeChartData(responseCodeSeries, tlList);
		refreshTransactionTypeChartData(transactionTypeSeries, tlList);
		refreshTopMerchantChartData(topMerchantSeries, tlList);
		refreshResponseTimeChartData(responseTimeSeries, tlList);
		responseCodePieChart.drawChart();
		topMerchantBarChart.drawChart();
		transactionTypeChart.drawChart();
		responseTimeChart.drawChart();
	}

	private void refreshResponseTimeChartData(DataSeries dataSeries, List<TranLog> tlList) {
		Set<Integer> cfgSet = new TreeSet<>();
		cfgSet.add(25);
		cfgSet.add(50);
		cfgSet.add(75);
		cfgSet.add(100);
		cfgSet.add(150);
		cfgSet.add(200);
		Map<Integer, Integer> mapCfg = new TreeMap<>();
		Map<String, Integer> map = new LinkedHashMap<>();
		Integer last = 0;
		for (Integer v : cfgSet) {
			if (last != 0) {
				last++;
			}
			mapCfg.put(v, last);
			String tag = getRspTimeTag(last, v);
			map.put(tag, 0);
			last = v;
		}

		String tag = getRspTimeTag(last, -1);
		map.put(tag, 0);
		for (TranLog tl : tlList) {
			int rspTime = tl.getDuration();
			tag = null;
			if (rspTime > last) {
				tag = getRspTimeTag(last, -1);
			} else {
				for (Map.Entry<Integer, Integer> e : mapCfg.entrySet()) {
					if (rspTime >= e.getValue() && rspTime <= e.getKey()) {
						tag = getRspTimeTag(e.getValue(), e.getKey());
						break;
					}
				}
			}

			if (tag != null) {
				Integer count = map.get(tag);
				if (count == null) {
					map.put(tag, 1);
				} else {
					map.put(tag, count + 1);
				}
			}
		}
		Iterator<String> itr = map.keySet().iterator();
		String keyName = null;
		while(itr.hasNext()) {
			keyName = itr.next();
			dataSeries.add(new DataSeriesItem(keyName, map.get(keyName)));
		}
	}
	private String getRspTimeTag(Integer low, Integer high) {
		StringBuilder bld = new StringBuilder();
		if (high != -1) {
			bld.append(low.toString());
			bld.append(" - ");
			bld.append(high.toString());
		} else {
			bld.append(" > ");
			bld.append(low.toString());
			bld.append(" ms");
		}
		return bld.toString();
	}
	private void refreshTopMerchantChartData(DataSeries dataSeries, List<TranLog> tlList) {
		Map<String, BigDecimal> map = new TreeMap<>();
		for (TranLog tl : tlList) {
			if (tl.getAmount() == null) {
				continue;
			}
			String merchant = tl.getMid();
			if (tl.getMid() == null) {
				merchant = "Absent";
			}
			BigDecimal count = map.get(merchant);
			if (count == null) {
				map.put(merchant, tl.getAmount());
			} else {
				map.put(merchant, count.add(tl.getAmount()));
			}
		}
		Iterator<String> itr = map.keySet().iterator();
		String keyName = null;
		while(itr.hasNext()) {
			keyName = itr.next();
			dataSeries.add(new DataSeriesItem(keyName, map.get(keyName)));
		}
	}

	private void refreshTransactionTypeChartData(DataSeries dataSeries, List<TranLog> tlList) {
		Map<String, Integer> map = new TreeMap<>();
		for (TranLog tl : tlList) {
			String tt = tl.getCurrencyCode();
			if (tl.getCurrencyCode() == null) {
				tt = "Absent";
			}
			Integer count = map.get(tt);
			if (count == null) {
				map.put(tt, 1);
			} else {
				map.put(tt, count + 1);
			}
		}
		Iterator<String> itr = map.keySet().iterator();
		String keyName = null;
		while(itr.hasNext()) {
			keyName = itr.next();
			dataSeries.add(new DataSeriesItem(keyName, map.get(keyName)));
		}		
	}

	private void refreshResponseCodeChartData(DataSeries dataSeries, List<TranLog> tlList) {
		Map<String, Integer> map = new TreeMap<>();
		for (TranLog tl : tlList) {
			String irc = tl.getIrc();
			if (tl.getIrc() == null) {
				irc = "Absent";
			}
			Integer count = map.get(irc);
			if (count == null) {
				map.put(irc, 1);
			} else {
				map.put(irc, count + 1);
			}
		}
		Iterator<String> itr = map.keySet().iterator();
		String keyName = null;
		while(itr.hasNext()) {
			keyName = itr.next();
			dataSeries.add(new DataSeriesItem(keyName, map.get(keyName)));
		}		
	}
	
	private void removeOldSeries() {
		removeOldSeries(responseCodeSeries);
		removeOldSeries(transactionTypeSeries);
		removeOldSeries(topMerchantSeries);
		removeOldSeries(responseTimeSeries);
	}
	private void removeOldSeries(DataSeries dataSeries) {
		if(dataSeries.size() > 0 ){
			while(dataSeries.size() > 0){
				dataSeries.remove(dataSeries.get(0));
			}
		}
	}
	
	private ComboBox createDateRange() {
		ComboBox field = new ComboBox();
		field.setItems(DateRange.ranges);
		field.setItemLabelGenerator((ItemLabelGenerator) item -> QI.getQI().getMessage(item.toString()));
		return field;
	}
	
}
*/