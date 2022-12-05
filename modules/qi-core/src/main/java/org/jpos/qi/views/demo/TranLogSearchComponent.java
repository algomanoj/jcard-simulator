package org.jpos.qi.views.demo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jpos.ee.TranLog;
import org.jpos.qi.QI;
import org.jpos.qi.core.ee.TranLogFilter;
import org.jpos.qi.services.TranLogHelper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.ContentAlignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public abstract class TranLogSearchComponent extends VerticalLayout {
	private QI app;
	private TranLogHelper helper;
	private TextField panField;
	private TextField rrnField;
	private DatePicker datePicker;
	
	private Button refreshButton;
	private Button changeTxnStatusButton;

	private ComboBox<String> transectionTypesCombo = new ComboBox<>();

	private H2 pageTitle;
	private Label respStatus;


	public TranLogSearchComponent(short[] layers, H2 pageTitle, 
			TranLogHelper helper, VerticalLayout verticalLayout) {
		super();
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
		panField.getStyle().set("margin-right", "10px").set("width", "190px");
		rrnField = new TextField();
		rrnField.setPlaceholder(app.getMessage("RRN"));
		rrnField.getStyle().set("margin-right", "10px").set("width", "190px");
		datePicker = new DatePicker();
		datePicker.getStyle().set("margin-right", "10px").set("width", "190px");
		datePicker.setMax(LocalDate.now());
		datePicker.setValue(LocalDate.now());
		
		datePicker.addValueChangeListener(e -> {
			datePicker.setMax(LocalDate.now());
		});

		refreshButton = new Button("Refresh");
		refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		FlexLayout refreshBtnLayout = new FlexLayout(refreshButton);
		refreshButton.addClickListener(e -> {
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
		Label typeOfTxnLabel = new Label("Select Txn."); 
		typeOfTxnLabel.getStyle().set("font-weight", "500").set("padding", "9px 6px").set("color", "#5b6777");    
		changeTxnStatusButton = new Button("Send");
		changeTxnStatusButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		changeTxnStatusButton.getStyle().set("margin-left", "5px");

		changeTxnStatusButton.addClickListener(e -> {
			changeTransectionStatus(transectionTypesCombo.getValue());
		});
		FlexLayout row2TextLayout = new FlexLayout(typeOfTxnLabel, transectionTypesCombo, changeTxnStatusButton);
		row2TextLayout.setAlignContent(ContentAlignment.END);
		row2TextLayout.setWidth("40%");
		
		row1.add(row1Layout, row2TextLayout);
		
		HorizontalLayout row2 = new HorizontalLayout();
		row2.setWidth("100%");
		row2.setMargin(false);
		respStatus = new Label();
		respStatus.setVisible(false);
		respStatus.addClassNames("pr-m");
		row2.addClassNames("pr-s");
		row2.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		row2.add(respStatus);
		verticalLayout.add(pageTitle, row1, row2);
	}
	
	public void showTranlogRespStatus(TranLog tranlog, String respCode, String error) {
		respStatus.removeAll();
		Paragraph p = new Paragraph("ID : "+tranlog.getId() + "      Resp Code : "+ respCode + (error != null ? "      Description : "+ error : ""));
		p.addClassNames("font-light", "text-s");
		p.getStyle().set("white-space", "pre-wrap");
		respStatus.add(p);
		respStatus.setVisible(true);
	}
	
	abstract void changeTransectionStatus(String transectionStatus);
	private void createClickRefreshListener() {
		refresh();
	}

	public TranLogFilter getValue() {
		TranLogFilter filter = new TranLogFilter();
		return filter;
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
	
}
