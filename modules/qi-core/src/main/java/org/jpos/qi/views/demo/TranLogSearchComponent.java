package org.jpos.qi.views.demo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.jpos.ee.CardProduct;
//import org.jpos.ee.TranLogFilter;
import org.jpos.iso.ISOUtil;
//import org.jpos.qi.JCardQIHelper;
import org.jpos.qi.QI;
import org.jpos.qi.core.ee.TranLogFilter;
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
	private TextField panField;
	private TextField rrnField;
	private DatePicker datePicker;
	
	private Button refreshButton;
	private Button changeTxnStatusButton;

	private ComboBox<String> transectionTypesCombo = new ComboBox<>();

	private short[] layers;

	private H2 pageTitle;


	public TranLogSearchComponent(/* String defaultDateRangeKey, */short[] layers, H2 pageTitle, 
			TranLogHelper helper, VerticalLayout verticalLayout) {
		super();
		//this.defaultDateRangeKey = defaultDateRangeKey;
		this.layers = layers;
		app = QI.getQI();
		this.helper = helper;
		this.pageTitle = pageTitle;
		setWidth("100%");
		createFilters(verticalLayout);
		setMargin(false);
	}

	private void createFilters(VerticalLayout verticalLayout) {
		HorizontalLayout row1 = new HorizontalLayout();
		row1.setWidth("100%");
		row1.setMargin(false);

		panField = new TextField();
		panField.setPlaceholder(app.getMessage("PAN"));
		panField.getStyle().set("margin-right", "10px").set("width", "140px");
		rrnField = new TextField();
		rrnField.setPlaceholder(app.getMessage("RRN"));
		rrnField.getStyle().set("margin-right", "10px").set("width", "140px");
		datePicker = new DatePicker();
		datePicker.getStyle().set("margin-right", "10px").set("width", "130px");
		datePicker.setMax(LocalDate.now());
		datePicker.setValue(LocalDate.now().minusDays(1));
		
		datePicker.addValueChangeListener(e -> {
			datePicker.setMax(LocalDate.now());
		});

		refreshButton = new Button("Refresh");
		refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		FlexLayout refreshBtnLayout = new FlexLayout(refreshButton);
		refreshButton.addClickListener(e -> {
			System.out.println("Clicked Refresh");
			createClickRefreshListener();

		});

		FlexLayout row1Layout = new FlexLayout(panField, rrnField, datePicker, refreshBtnLayout);
		row1Layout.setAlignContent(ContentAlignment.START);
		row1Layout.setWidth("60%");
		
		transectionTypesCombo = new ComboBox();
		List<String> transectionTypeList = new ArrayList<String>();
		transectionTypeList.add("Reversal");
		transectionTypeList.add("Refund");
		transectionTypeList.add("Completion");
		transectionTypesCombo.setItems(transectionTypeList);
/*		transectionTypesCombo.addValueChangeListener(e -> {
			System.out.println("transectionTypesCombo.addValueChangeListener:"+e.getValue());
			changeTransectionStatus(e.getValue());
		}); */
		Label typeOfTxnLabel = new Label("Follow up Txn."); 
		typeOfTxnLabel.getStyle().set("font-weight", "500").set("padding", "9px 6px").set("color", "#5b6777");    
		changeTxnStatusButton = new Button("Send");
		changeTxnStatusButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		changeTxnStatusButton.getStyle().set("margin-left", "5px");

		changeTxnStatusButton.addClickListener(e -> {
			System.out.println("changeTxnStatusButton.addClickListener:"+transectionTypesCombo.getValue());
			changeTransectionStatus(transectionTypesCombo.getValue());
		});
		FlexLayout row2TextLayout = new FlexLayout(typeOfTxnLabel, transectionTypesCombo, changeTxnStatusButton);
		row2TextLayout.setAlignContent(ContentAlignment.END);
		row2TextLayout.setWidth("40%");
		
		row1.add(row1Layout, row2TextLayout);

		verticalLayout.add(pageTitle, row1);
	}
	abstract void changeTransectionStatus(String transectionStatus);
	private void createClickRefreshListener() {
		refresh();
	}

	public TranLogFilter getValue() {
		TranLogFilter filter = new TranLogFilter();
		//filter.setApproved(getApprovedCheckBox().getValue());
		//filter.setRejected(getRejectedCheckBox().getValue());
		//filter.setPending(getPendingCheckBox().getValue());
		//filter.setVoided(getVoidedCheckBox().getValue());
/*		if (getLayersCombo().getValue() != null) {
			Short layer = Short.parseShort(getLayersCombo().getValue().toString());
			if (layer != null)
				filter.setLayers(new short[] { layer });
		} */

		CardProduct cp = null;//getCardProductCombo().getValue();
		if (cp != null)
			filter.setCardProduct(cp);
		String panValue = panField.getValue();
		if (isValidSearch()) {
			String prefix = panValue.split(":", 2)[0].trim().toLowerCase();
			String[] values = ISOUtil.commaDecode(panValue.split(":", 2)[1].trim());
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
		return filter;
	}

	private boolean isValidSearch() {
		String value = panField.getValue();
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



	private HasValue.ValueChangeListener createValueChangeRefreshListener() {
		return (HasValue.ValueChangeListener) event -> refresh();
	}


	protected abstract void refresh();

	public void resetFilter() {
		panField.setValue("");
		rrnField.setValue("");
		datePicker.setValue(null);
	}

	protected Button getRefreshButton() {
		return refreshButton;
	}

	public TextField getPanField() {
		return panField;
	}

	public void setPanField(TextField panField) {
		this.panField = panField;
	}

	public TextField getRrnField() {
		return rrnField;
	}

	public void setRrnField(TextField rrnField) {
		this.rrnField = rrnField;
	}

	public DatePicker getDatePicker() {
		return datePicker;
	}

	public void setDatePicker(DatePicker datePicker) {
		this.datePicker = datePicker;
	}

	public ComboBox<String> getTransectionTypesCombo() {
		return transectionTypesCombo;
	}

	public void setTransectionTypesCombo(ComboBox<String> transectionTypesCombo) {
		this.transectionTypesCombo = transectionTypesCombo;
	}
	
	//private TextField rrnField;
	//private DatePicker datePicker;
	
	//private Button refreshButton;

	//private ComboBox<String> transectionTypesCombo = new ComboBox<>();
}
