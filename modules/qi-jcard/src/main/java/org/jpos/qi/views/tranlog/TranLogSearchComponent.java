package org.jpos.qi.views.tranlog;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.jpos.ee.CardProduct;
import org.jpos.ee.TranLogFilter;
import org.jpos.iso.ISOUtil;
//import org.jpos.qi.JCardQIHelper;
import org.jpos.qi.QI;
import org.jpos.qi.services.TranLogHelper;
import org.jpos.qi.util.DateRange;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.ContentAlignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public abstract class TranLogSearchComponent extends VerticalLayout {
	private static String SEARCH_PATTERN = "((id)|(itc)|(ID)|(ITC)|(Id)|(Itc)|(MID)|(mid)|(comercio))+\\s*:(\\s*)([\\w\\.\\:\\,\\s])+";
	private QI app;
	private TranLogHelper helper;
	private String defaultDateRangeKey;
	private Checkbox approvedCheckBox;
	private Checkbox rejectedCheckBox;
	private Checkbox pendingCheckBox;
	private Checkbox voidedCheckBox;
	private ComboBox layersCombo;
	private ComboBox<CardProduct> cardProductCombo;

	private ComboBox dateRangeCombo;
	// private DatePicker rangeDatePicker;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;

	private TextField searchField;
	private Button refreshButton;

	private short[] layers;

	private H2 pageTitle;

	private boolean selectedDateRangeDropdown;

	public TranLogSearchComponent(String defaultDateRangeKey, short[] layers, H2 pageTitle, TranLogHelper helper,
			VerticalLayout verticalLayout) {
		super();
		this.defaultDateRangeKey = defaultDateRangeKey;
		this.layers = layers;
		app = QI.getQI();
		this.helper = helper;
		this.pageTitle = pageTitle;
		setWidth("100%");
		createCheckBoxFilters(verticalLayout);
		setMargin(false);
	}

	private void createCheckBoxFilters(VerticalLayout verticalLayout) {
		HorizontalLayout row1 = new HorizontalLayout();
		HorizontalLayout row2 = new HorizontalLayout();
		HorizontalLayout row3 = new HorizontalLayout();
		row1.setWidth("100%");
		row2.setWidth("100%");
		row3.setWidth("100%");
		row1.setMargin(false);
		row2.setMargin(false);
		row3.setMargin(false);

		approvedCheckBox = createCheckBox(app.getMessage("approved"));
		rejectedCheckBox = createCheckBox(app.getMessage("rejected"));
		pendingCheckBox = createCheckBox(app.getMessage("pending"));
		voidedCheckBox = createCheckBox(app.getMessage("voided"));
		layersCombo = createLayersCombo();
		cardProductCombo = createCardProductCombo();
		searchField = new TextField();
		searchField.setPlaceholder(app.getMessage("search"));
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

		/*
		 * dateRangeComponent = new DateRangeComponent(defaultDateRangeKey, true) {
		 * 
		 * @Override protected Button.ClickListener createRefreshListener() { return
		 * createClickRefreshListener(); } }; dateRangeComponent.setWidth("100%");
		 * dateRangeComponent.setExpandRatio(dateRangeComponent.getRefreshBtn(), 1f);
		 * dateRangeComponent.setComponentAlignment(dateRangeComponent.getRefreshBtn(),
		 * Alignment.MIDDLE_RIGHT);
		 * dateRangeComponent.getRefreshBtn().addShortcutListener( new
		 * ShortcutListener("Refresh", ShortcutAction.KeyCode.ENTER, null) {
		 * 
		 * @Override public void handleAction(Object sender, Object target) { refresh();
		 * } } );
		 */
		row1.setWidthFull();
		row2.setWidthFull();
		row3.setWidthFull();

		FlexLayout row1Layout = new FlexLayout(layersCombo, approvedCheckBox, rejectedCheckBox, pendingCheckBox,
				voidedCheckBox);
		row1Layout.setAlignContent(ContentAlignment.START);
		row1Layout.setWidth("80%");
		FlexLayout searchFieldLayout = new FlexLayout(searchField);
		searchFieldLayout.setAlignContent(ContentAlignment.END);
		// row1.set
		row1.add(row1Layout, searchFieldLayout);
		row1.add();
		row1.setAlignSelf(FlexComponent.Alignment.START, row1Layout);
		row1.setAlignSelf(FlexComponent.Alignment.END, searchFieldLayout);

		FlexLayout row2FieldLayout = new FlexLayout(cardProductCombo);
		FlexLayout row2TextLayout = new FlexLayout(new Text("Search by ITC, ID or MID."));
		row2FieldLayout.setWidth("80%");
		row2.add(row2FieldLayout, row2TextLayout);
		Text dashLabel = new Text(" - ");
		dateRangeCombo.getStyle().set("padding-right", "20px");
		startDatePicker.getStyle().set("padding-right", "10px");
		endDatePicker.getStyle().set("padding-right", "10px");
		FlexLayout dateFieldLayout = new FlexLayout(/* rangeDatePicker */dateRangeCombo, startDatePicker, dashLabel,
				endDatePicker);
		refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		FlexLayout refreshBtnLayout = new FlexLayout(refreshButton);
		dateFieldLayout.setWidth("80%");
		row3.add(dateFieldLayout, refreshBtnLayout);
		row3.setAlignSelf(FlexComponent.Alignment.END, refreshButton);
		refreshButton.addClickListener(e -> {
			System.out.println("Clicked Refresh");
			createClickRefreshListener();

		});
		verticalLayout.add(pageTitle, row1, row2, row3);
	}

	private void createClickRefreshListener() {
		refresh();

	}

	public TranLogFilter getValue() {
		TranLogFilter filter = new TranLogFilter();
		filter.setApproved(getApprovedCheckBox().getValue());
		filter.setRejected(getRejectedCheckBox().getValue());
		filter.setPending(getPendingCheckBox().getValue());
		filter.setVoided(getVoidedCheckBox().getValue());
		if (getLayersCombo().getValue() != null) {
			Short layer = Short.parseShort(getLayersCombo().getValue().toString());
			if (layer != null)
				filter.setLayers(new short[] { layer });
		}

		CardProduct cp = getCardProductCombo().getValue();
		if (cp != null)
			filter.setCardProduct(cp);
		String searchValue = searchField.getValue();
		if (isValidSearch()) {
			String prefix = searchValue.split(":", 2)[0].trim().toLowerCase();
			String[] values = ISOUtil.commaDecode(searchValue.split(":", 2)[1].trim());
			switch (prefix) {
			case ("itc"): {
				filter.setItcList(values);
				break;
			}
			case ("mid"):
			case ("comercio"): {
				filter.setMidList(values);
				break;
			}
			case ("id"): {
				if (values != null && values.length > 0)
					filter.setId(values[0]);
				break;
			}
			}
		}

		if (dateRangeCombo.getValue() != null) {
			DateRange dr = new DateRange();
			filter.setStart(dr.startDate.get(dateRangeCombo.getValue()));
			filter.setEnd(dr.endDate.get(dateRangeCombo.getValue()));
		} else {
			LocalDate stDate = startDatePicker.getValue();
			if (stDate != null)
				filter.setStart(Date.from(stDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			LocalDate endDate = endDatePicker.getValue();
			if (endDate != null)
				filter.setEnd(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		}
		return filter;
	}

	private boolean isValidSearch() {
		String value = searchField.getValue();
		if (value == null || "".equals(value)) {
			return false;
		}
		Pattern pattern = Pattern.compile(SEARCH_PATTERN);
		Matcher matcher = pattern.matcher(value);
		if (!matcher.matches()) {
			// setComponentError(new
			// UserError(app.getMessage("errorMessage.invalidSearchTranLog")));
			return false;
		} else {
			// setComponentError(null);
			return true;
		}
	}

	private Checkbox createCheckBox(String caption) {
		Checkbox cb = new Checkbox(caption);
		// cb.setStyleName(ValoTheme.CHECKBOX_SMALL);
		cb.addValueChangeListener(createValueChangeRefreshListener());
		return cb;
	}

	private ComboBox createLayersCombo() {
		ComboBox field = new ComboBox();
		field.setItems(helper.getCurrencies());
		field.setPlaceholder(app.getMessage("currency.all"));
		field.setItemLabelGenerator((ItemLabelGenerator) item -> app.getMessage("currency." + item.toString()));
		Short[] inputBoxed = ArrayUtils.toObject(layers);
		List<Short> inputAsList = Arrays.asList(inputBoxed);
		field.setItems(inputAsList);
		field.addValueChangeListener(createValueChangeRefreshListener());
		return field;
	}

	private ComboBox createDateRange() {
		ComboBox field = new ComboBox();
		field.setItems(DateRange.ranges);
		field.setValue(defaultDateRangeKey != null ? defaultDateRangeKey : DateRange.ranges[1]);
		field.setItemLabelGenerator((ItemLabelGenerator) item -> app.getMessage(item.toString()));
		field.addValueChangeListener(createValueChangeDateListener());
		return field;
	}

	private HasValue.ValueChangeListener createValueChangeDateListener() {
		return (HasValue.ValueChangeListener) event -> {
			if (event.getValue() != null) {
				endDatePicker.setValue(null);
				startDatePicker.setValue(null);
				refresh();
			}

		};
	}

	private ComboBox<CardProduct> createCardProductCombo() {
		ComboBox<CardProduct> field = new ComboBox();
		field.setWidth("400px");
		Set<CardProduct> productList=helper.getCardProducts();
		field.setItems(productList);
		if(productList != null && productList.size() > 0) {
			CardProduct c=productList.stream().findFirst().get();
			field.setValue(c);
		}
		field.setItemLabelGenerator((ItemLabelGenerator<CardProduct>) item -> {
			CardProduct b = (CardProduct) item;
			return b.getName();
		});
		field.addValueChangeListener(createValueChangeRefreshListener());
		return field;
	}

	private HasValue.ValueChangeListener createValueChangeRefreshListener() {
		return (HasValue.ValueChangeListener) event -> refresh();
	}

	public Checkbox getApprovedCheckBox() {
		return approvedCheckBox;
	}

	public void setApprovedCheckBox(Checkbox approvedCheckBox) {
		this.approvedCheckBox = approvedCheckBox;
	}

	public Checkbox getRejectedCheckBox() {
		return rejectedCheckBox;
	}

	public void setRejectedCheckBox(Checkbox rejectedCheckBox) {
		this.rejectedCheckBox = rejectedCheckBox;
	}

	public Checkbox getPendingCheckBox() {
		return pendingCheckBox;
	}

	public void setPendingCheckBox(Checkbox pendingCheckBox) {
		this.pendingCheckBox = pendingCheckBox;
	}

	public Checkbox getVoidedCheckBox() {
		return voidedCheckBox;
	}

	public void setVoidedCheckBox(Checkbox voidedCheckBox) {
		this.voidedCheckBox = voidedCheckBox;
	}

	public void setDefaultDateRangeKey(String defaultDateRangeKey) {
		this.defaultDateRangeKey = defaultDateRangeKey;
	}

	public ComboBox<Short> getLayersCombo() {
		return layersCombo;
	}

	public void setLayersCombo(ComboBox layersCombo) {
		this.layersCombo = layersCombo;
	}

	public ComboBox<CardProduct> getCardProductCombo() {
		return cardProductCombo;
	}

	public void setCardProductCombo(ComboBox cpCombo) {
		this.cardProductCombo = cpCombo;
	}

	protected abstract void refresh();

	public void resetFilter() {
		approvedCheckBox.setValue(false);
		rejectedCheckBox.setValue(false);
		pendingCheckBox.setValue(false);
		voidedCheckBox.setValue(false);
		layersCombo.setValue(null);
		cardProductCombo.setValue(null);
		searchField.setValue("");
		dateRangeCombo.setValue(null);
		startDatePicker.setValue(null);
		endDatePicker.setValue(null);
	}

	protected Button getRefreshButton() {
		return refreshButton;
	}
}
