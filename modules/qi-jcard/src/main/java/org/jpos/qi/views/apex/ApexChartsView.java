package org.jpos.qi.views.apex;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jpos.ee.TranLog;
import org.jpos.qi.QI;
import org.jpos.qi.services.TranLogHelper;
import org.jpos.qi.util.DateRange;
import org.jpos.qi.views.QIEntityView;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.TitleSubtitleBuilder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ApexChartsView extends QIEntityView<TranLog> {

	private static final long serialVersionUID = 8261558323972648811L;

	private Span noRecordFoundSpan;
	private Span transactionAmountsSpan;
	private Label transCount;
	private Label transAmt;

	private ComboBox dateRangeCombo;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private Button refreshButton;

	private HorizontalLayout dashBoardGraphHL;

	private List<TranLog> tlList;

    private static final Double MAX_SCREEN_WIDTH = 480.0;
    private static final Integer MAX_SCREEN_HEIGHT = 200;
     
    private ResponseCodePieChart responseCodePieChartBuilder; 
    private TransactionTypePieChart transactionTypePieChartBuilder;
    private TopMerchantVerticalBarChart topMerchantVerticalBarChartBuilder; 
    private ResponseTimeLineChart responseTimeLineChartBuilder;

    private ApexCharts responseCodePieChart;
    private ApexCharts transactionTypePieChart;
    private ApexCharts topMerchantVerticalBarChart;
    private ApexCharts responseTimeLineChart;

    private VerticalLayout leftVerticalColumn = new VerticalLayout();
    private VerticalLayout rightVerticalColumn = new VerticalLayout();
    
    private Label responseCodeLabel = new Label(QI.getQI().getMessage("dashboard.responseCode"));
    private Label topMerchantLabel = new Label(QI.getQI().getMessage("dashboard.topMerchant"));
    private Label responseTimeLabel = new Label(QI.getQI().getMessage("dashboard.responseTime"));
    private Label transactionTypeLabel = new Label(QI.getQI().getMessage("dashboard.transactionType"));
	
	private DecimalFormat decimalFormat = new DecimalFormat("###.##");

    public ApexChartsView() {
		super(TranLog.class, "dashboard");
	}

	@Override
	public Component initContent() {
		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(QI.getQI().getMessage("dashboard"));
		viewTitle.addClassNames("mt-s", "text-l");
		noRecordFoundSpan = new Span(
			new Icon(VaadinIcon.DATABASE), new Span(QI.getQI().getMessage("dashboard.noData"))
		);
		noRecordFoundSpan.getStyle().set("font-weight", "bold");
		vl.add(viewTitle, getFilterBar(), noRecordFoundSpan);
	    responseCodeLabel.getStyle().set("font-weight", "bold");
	    topMerchantLabel.getStyle().set("font-weight", "bold");
	    responseTimeLabel.getStyle().set("font-weight", "bold");
	    transactionTypeLabel.getStyle().set("font-weight", "bold");
		dashBoardGraphHL = getDashBoardGraphs();
		dashBoardGraphHL.setVisible(true);
		vl.add(dashBoardGraphHL);
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

		dateRangeCombo.addValueChangeListener(e -> {
			onRefreshButtonClick();
		});
		
		refreshButton = new Button("Refresh");
		
		dateRangeCombo.getStyle().set("padding-right", "0px");
		startDatePicker.getStyle().set("padding-right", "5px");
		endDatePicker.getStyle().set("padding-right", "10px");
		
		transCount = new Label();
		transAmt = new Label();
		transactionAmountsSpan = new Span(
			new Icon(VaadinIcon.DATABASE),
			new Label(" " + QI.getQI().getMessage("dashboard.transactions") + ": "),
			transCount,
			new Label(" "),
			new Icon(VaadinIcon.MONEY_EXCHANGE),
			new Label(" " + QI.getQI().getMessage("dashboard.amount") + ": "), transAmt);
		transactionAmountsSpan.getStyle().set("margin-left", "20px");
		transactionAmountsSpan.getStyle().set("margin-top", "10px");
		transactionAmountsSpan.getStyle().set("font-weight", "bold");
		
		Label orLabel = new Label(" " + QI.getQI().getMessage("dashboard.or") + " ");
		orLabel.getStyle().set("margin", "8px 5px");
		
		FlexLayout dateFieldLayout = new FlexLayout(dateRangeCombo, orLabel, startDatePicker,  endDatePicker, refreshButton, transactionAmountsSpan);
		refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		row3.add(dateFieldLayout);
		refreshButton.addClickListener(e -> {
			onRefreshButtonClick();
		});
		return row3;
	}

	private HorizontalLayout getDashBoardGraphs() {
		dashBoardGraphHL = new HorizontalLayout();
		dashBoardGraphHL.setWidth("100%");
		dashBoardGraphHL.setMargin(false);
		dashBoardGraphHL.setWidthFull();

	    tlList = getHelper().getTlList(new DateRange());
	    setTransactionAmountCount();
	    noRecordFoundSpan.setVisible(false);
	    refreshAllChartData();
	    
	    leftVerticalColumn.setWidth("50%");
	    leftVerticalColumn.getStyle().set("padding-left", "0px");
	    rightVerticalColumn.setWidth("50%");
	    
	    allAllChart();
		
		return dashBoardGraphHL;
	}
	
	private void refreshDashBoardGraphs() {
		removeAllChart();
		allAllChart();
	}

	private void allAllChart() {
		//leftVerticalColumn.add(responseCodeLabel);
	    leftVerticalColumn.add(responseCodePieChart); 
		//leftVerticalColumn.add(topMerchantLabel);
		leftVerticalColumn.add(topMerchantVerticalBarChart); 
		//rightVerticalColumn.add(transactionTypeLabel);
		rightVerticalColumn.add(transactionTypePieChart);
		//rightVerticalColumn.add(responseTimeLabel);
		rightVerticalColumn.add(responseTimeLineChart); 
		dashBoardGraphHL.add(leftVerticalColumn);
		dashBoardGraphHL.add(rightVerticalColumn);
	}

	private void removeAllChart() {
		leftVerticalColumn.removeAll();
		rightVerticalColumn.removeAll();
		dashBoardGraphHL.removeAll();
	}
	
    public ApexChartsBuilder[] getLeftCharts(Map<String, Double> responseCodeChartData, Map<String, Double> topMerchantChartData) {
    	String[] responseCodeLabels = responseCodeChartData.keySet().toArray(new String[responseCodeChartData.size()]);
    	Double[] responseCodeValues = responseCodeChartData.values().toArray(new Double[responseCodeChartData.size()]);
    	String[] topMerchantLabels = topMerchantChartData.keySet().toArray(new String[topMerchantChartData.size()]);
    	String[] topMerchantValues = topMerchantChartData.values().toArray(new String[topMerchantChartData.size()]);
    	return Arrays.stream(new ApexChartsBuilder[]{
    			new ResponseCodePieChart(getApp().getMessage("chart.responsecode.label"),MAX_SCREEN_WIDTH,responseCodeLabels,responseCodeValues),
    			new TopMerchantVerticalBarChart(getApp().getMessage("chart.topmerchant.label"),MAX_SCREEN_WIDTH,topMerchantLabels,topMerchantValues)
    	}).map(builder ->
        builder.withTitle(TitleSubtitleBuilder.get().withText(builder.getClass().getSimpleName()).build()))
    			.toArray(ApexChartsBuilder[]::new);
    }
	   
    public ApexChartsBuilder[] getRightCharts(Map<String, Double> transactionTypeChartData, Map<String, Double> responseTimeChartData) {
    	String[] transactionTypeLabels = transactionTypeChartData.keySet().toArray(new String[transactionTypeChartData.size()]);
    	Double[] transactionTypeValues = transactionTypeChartData.values().toArray(new Double[responseTimeChartData.size()]);
    	String[] responseTimeLabels = responseTimeChartData.keySet().toArray(new String[responseTimeChartData.size()]);
    	Double[] responseTimeValues = responseTimeChartData.values().toArray(new Double[responseTimeChartData.size()]);
    	return Arrays.stream(new ApexChartsBuilder[]{
    			new TransactionTypePieChart(getApp().getMessage("chart.transactiontype.label"),MAX_SCREEN_WIDTH,transactionTypeLabels,transactionTypeValues),
    			new ResponseTimeLineChart(getApp().getMessage("chart.responsetime.label"),MAX_SCREEN_WIDTH,responseTimeLabels,responseTimeValues)
    	}).map(builder ->
        builder.withTitle(TitleSubtitleBuilder.get().withText(builder.getClass().getSimpleName()).build()))
    			.toArray(ApexChartsBuilder[]::new);

    }

	
	private void onRefreshButtonClick() {
		//removeOldSeries();
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
  		refreshDashBoardGraphs();
		return;
	}
	private void refreshGraphData(DateRange dr) {
		noRecordFoundSpan.setVisible(false);
		transactionAmountsSpan.setVisible(true);
		dashBoardGraphHL.setVisible(true);
		refreshAllChartData();
		if(tlList!=null && tlList.size()>0) {
			setTransactionAmountCount();
		} else {
			noRecordFoundSpan.setVisible(true);
			transactionAmountsSpan.setVisible(false);
			dashBoardGraphHL.setVisible(false);
		}
	}

	private void setTransactionAmountCount() {
		BigDecimal amt = new BigDecimal(0);
		for (TranLog tl : tlList) {
			if (tl.getAmount() != null) {
				amt = amt.add(tl.getAmount());
			}
		}
		transCount.setText(String.valueOf(tlList.size()));
		transAmt.setText(String.valueOf(amt));
	}

	private void refreshAllChartData() {
		refreshResponseCodeChart();
		refreshTopMerchantChart();
		refreshTransactionTypeChart();
		refreshResponseTimeChart();
	}

	private void refreshResponseCodeChart() {
        Map<String, Double> chartData = getResponseCodeChartData(tlList);
    	String[] labels = chartData.keySet().toArray(new String[chartData.size()]);
    	Double[] values = chartData.values().toArray(new Double[chartData.size()]);
    	responseCodePieChartBuilder = new ResponseCodePieChart(getApp().getMessage("chart.responsecode.label"),MAX_SCREEN_WIDTH,labels,values);
        responseCodePieChart = responseCodePieChartBuilder.build();
        responseCodePieChart.getStyle().set("border", "2px solid gray");
        responseCodePieChart.getStyle().set("padding-bottom", "2px");
        responseCodePieChart.getStyle().set("margin-bottom", "8px");
        responseCodePieChart.setHeight((MAX_SCREEN_HEIGHT+15)+"px");
	}
	
	private void refreshTopMerchantChart() {
        Map<String, String> chartData = getTopMerchantChartData(tlList);
    	String[] labels = chartData.keySet().toArray(new String[chartData.size()]);
    	String[] values = chartData.values().toArray(new String[chartData.size()]);
    	topMerchantVerticalBarChartBuilder = new TopMerchantVerticalBarChart(getApp().getMessage("chart.topmerchant.label"),MAX_SCREEN_WIDTH,labels,values);
        topMerchantVerticalBarChart = topMerchantVerticalBarChartBuilder.build();
        topMerchantVerticalBarChart.getStyle().set("border", "2px solid gray");
        topMerchantVerticalBarChart.getStyle().set("padding-right", "2px");
        topMerchantVerticalBarChart.getStyle().set("margin-bottom", "2px");
        topMerchantVerticalBarChart.setHeight(MAX_SCREEN_HEIGHT+"px");
	}
	private void refreshTransactionTypeChart() {
        Map<String, Double> chartData = getTransactionTypeChartData(tlList);
    	String[] labels = chartData.keySet().toArray(new String[chartData.size()]);
    	Double[] values = chartData.values().toArray(new Double[chartData.size()]);
    	transactionTypePieChartBuilder = new TransactionTypePieChart(getApp().getMessage("chart.transactiontype.label"),MAX_SCREEN_WIDTH,labels,values);
        transactionTypePieChart = transactionTypePieChartBuilder.build();
        transactionTypePieChart.getStyle().set("border", "2px solid gray");
        transactionTypePieChart.getStyle().set("padding-bottom", "23px");
        transactionTypePieChart.setHeight(MAX_SCREEN_HEIGHT+"px");
	}
	private void refreshResponseTimeChart() {
        Map<String, Double> chartData = getResponseTimeChartData(tlList);
    	String[] labels = chartData.keySet().toArray(new String[chartData.size()]);
    	Double[] values = chartData.values().toArray(new Double[chartData.size()]);
    	responseTimeLineChartBuilder = new ResponseTimeLineChart(getApp().getMessage("chart.responsetime.label"),MAX_SCREEN_WIDTH,labels,values);
        responseTimeLineChart = responseTimeLineChartBuilder.build();
        responseTimeLineChart.getStyle().set("border", "2px solid gray");
        responseTimeLineChart.getStyle().set("padding-bottom", "10px");
        responseTimeLineChart.setHeight(MAX_SCREEN_HEIGHT+"px");
	}
	private Map<String, Double> getResponseCodeChartData(List<TranLog> tlList) {
		Map<String, Double> map = new TreeMap<>();
		if(tlList!=null && tlList.size()>0) {
			for (TranLog tl : tlList) {
				String irc = tl.getIrc();
				if (tl.getIrc() == null) {
					irc = "Absent";
				}
				Double count = map.get(irc);
				if (count == null) {
					map.put(irc, 1.0);
				} else {
					map.put(irc, count + 1.0);
				}
			}
		} else {
			map.put("Absent", 0.0);
		}
		return getTopTenRecords(map);		
	}
	private Map<String, String> getTopMerchantChartData(List<TranLog> tlList) {
		Map<String, Double> map = new TreeMap<>();
		if(tlList!=null && tlList.size()>0) {
			for (TranLog tl : tlList) {
				if (tl.getAmount() == null) {
					continue;
				}
				String merchant = tl.getMid();
				if (tl.getMid() == null) {
					merchant = "Absent";
				}
				Double count = map.get(merchant);
				if (count == null) {
					map.put(merchant, getRoundedValue(tl.getAmount()));
				} else {
					map.put(merchant, count + getRoundedValue(tl.getAmount()));
				}
			}
		} else {
			map.put("Absent", 0.0);
		}
		Map<String, Double> tmMap = getTopTenRecords(map);
		Map<String, String> topMerchantMap = new HashMap<>();
		for (String key : tmMap.keySet()) {
			topMerchantMap.put(key, getRoundedValueString(tmMap.get(key)));
		}
		return topMerchantMap;
	}
	private Map<String, Double> getTransactionTypeChartData(List<TranLog> tlList) {
		Map<String, Double> map = new TreeMap<>();
		if(tlList!=null && tlList.size()>0) {
			for (TranLog tl : tlList) {
				String tt = tl.getItc();
				if (tl.getCurrencyCode() == null) {
					tt = "Absent";
				}
				Double count = map.get(tt);
				if (count == null) {
					map.put(tt, 1.0);
				} else {
					map.put(tt, count + 1.0);
				}
			}
		} else {
			map.put("Absent", 0.0);
		}
		return getTopTenRecords(map);
	}
	private double getRoundedValue(BigDecimal inputValue) {
		Double doubleValue = 0.0;
		try {
			if(inputValue!=null) {
				doubleValue = Double.parseDouble(decimalFormat.format(inputValue));
			}
		} catch (Exception e) {
		}
		return doubleValue;
	}
	private String getRoundedValueString(Double inputValue) {
		try {
			return decimalFormat.format(inputValue);
		} catch (Exception e) {
			return "0.00";
		}
	}
	private Map<String, Double> getTopTenRecords(Map<String, Double> map) {
		Map<String, Double> topTenMap = new HashMap<>();
		TreeMap<String, Double> sortedMap = new TreeMap<>();
		if(map!=null && map.size()<5) {
			topTenMap = map;
		} else {
			map.entrySet().stream().sorted( Map.Entry.<String, Double>comparingByValue().reversed() )
		       .forEachOrdered(e -> sortedMap.put(e.getKey(), e.getValue()));
			
			int i = 0;
			for (String key : sortedMap.keySet()) {
				if(i<5) {
					topTenMap.put(key, sortedMap.get(key));
				} else {
					if(topTenMap.get("others")==null) {
						topTenMap.put("others", sortedMap.get(key));					
					} else {
						topTenMap.put("others", (topTenMap.get(key)==null?0.0:topTenMap.get(key))+topTenMap.get("others"));					
					}
				}
				i++;
			}
			System.out.println("sortedMap["+sortedMap.size()+"]:"+sortedMap);
		}
		System.out.println("topTenMap["+topTenMap.size()+"]:"+topTenMap);
		return topTenMap;
	}
	private Map<String, Double> getResponseTimeChartData(List<TranLog> tlList) {
		Set<Double> cfgSet = new TreeSet<>();
		cfgSet.add(25.0);
		cfgSet.add(50.0);
		cfgSet.add(75.0);
		cfgSet.add(100.0);
		cfgSet.add(150.0);
		cfgSet.add(200.0);
		Map<Double, Double> mapCfg = new TreeMap<>();
		Map<String, Double> map = new LinkedHashMap<>();
		Double last = 0.0;
		for (Double v : cfgSet) {
			if (last != 0) {
				last++;
			}
			mapCfg.put(v, last);
			String tag = getRspTimeTag(last, v);
			map.put(tag, 0.0);
			last = v;
		}

		String tag = getRspTimeTag(last, -1.0);
		map.put(tag, 0.0);
		for (TranLog tl : tlList) {
			int rspTime = tl.getDuration();
			tag = null;
			if (rspTime > last) {
				tag = getRspTimeTag(last, -1.0);
			} else {
				for (Map.Entry<Double, Double> e : mapCfg.entrySet()) {
					if (rspTime >= e.getValue() && rspTime <= e.getKey()) {
						tag = getRspTimeTag(e.getValue(), e.getKey());
						break;
					}
				}
			}

			if (tag != null) {
				Double count = map.get(tag);
				if (count == null) {
					map.put(tag, 1.0);
				} else {
					map.put(tag, count + 1.0);
				}
			}
		}
		return map;
	}
	private String getRspTimeTag(Double low, Double high) {
		StringBuilder bld = new StringBuilder();
		if (high != -1) {
			bld.append(low.toString());
			bld.append(" - ");
			bld.append(high);
		} else {
			bld.append(" > ");
			bld.append(low.toString());
			bld.append(" ms");
		}
		return bld.toString();
	}
	
    private Date parseLocalDate(LocalDate localDate) {
    	try {
        	return new SimpleDateFormat("yyyy-MM-dd").parse(localDate.toString());
		} catch (Exception e) {
        	return null;
		}
    }
	
	private ComboBox createDateRange() {
		ComboBox field = new ComboBox();
		field.setItems(DateRange.ranges);
		field.setItemLabelGenerator((ItemLabelGenerator) item -> QI.getQI().getMessage(item.toString()));
		field.addValueChangeListener(createValueChangeDateListener());
		field.setValue(DateRange.ALL_TIME);
		return field;
	}
	private HasValue.ValueChangeListener createValueChangeDateListener() {
		return event -> {
			if (event.getValue() != null) {
				if(endDatePicker!=null) 
					endDatePicker.setValue(null);
				if(startDatePicker!=null) 
					startDatePicker.setValue(null);
			}
		};
	}
	@Override
	public TranLogHelper createHelper() {
		return new TranLogHelper();
	}
	
	public TranLogHelper getHelper() {
		return (TranLogHelper) super.getHelper();
	}


}
