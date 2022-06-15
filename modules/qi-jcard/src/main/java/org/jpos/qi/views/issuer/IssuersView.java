package org.jpos.qi.views.issuer;

import java.util.Arrays;

import org.jpos.ee.BLException;
import org.jpos.ee.Issuer;
import org.jpos.ee.State;
import org.jpos.ee.SysConfig;
import org.jpos.gl.Account;
import org.jpos.gl.Journal;
import org.jpos.qi.QIUtils;
import org.jpos.qi.services.IssuerHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.util.StringToShortConverter;
import org.jpos.qi.util.SysConfigComboBox;
import org.jpos.qi.util.SysConfigConverter;
import org.jpos.qi.views.QIEntityView;
import org.jpos.qi.views.minigl.AccountConverter;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;

public class IssuersView extends QIEntityView<Issuer> {

	private static final long serialVersionUID = -9160920591299009485L;

	private DatePicker startDatePicker = new DatePicker();
	private DatePicker endDatePicker = new DatePicker();
	private TextField assetsAccountField;
	private TextField earningsAccountField;
	private TextField lossesAccountField;
	private TextField institutionIdField;

	private ComboBox<State> statesComboBox;

	public IssuersView() {
		super(Issuer.class, "issuers");
	}

	@Override
	public GridCrud<Issuer> createCrud() {
		GridCrud<Issuer> crud = super.createCrud();

		String[] caption = { 
					getApp().getMessage("issuers.institutionId"),getApp().getMessage("issuers.tz"),
					getApp().getMessage("issuers.active"),getApp().getMessage("issuers.startDate"),
					getApp().getMessage("issuers.name"),getApp().getMessage("issuers.endDate"),
					getApp().getMessage("issuers.description"),getApp().getMessage("issuers.journal"),
					getApp().getMessage("issuers.contactName"),getApp().getMessage("issuers.assetsAccount"),
					getApp().getMessage("issuers.contactPosition"),getApp().getMessage("issuers.earningsAccount"),
					getApp().getMessage("issuers.contactEmail"),getApp().getMessage("issuers.lossesAccount"),
					getApp().getMessage("issuers.address1"),getApp().getMessage("issuers.address2"),
					getApp().getMessage("issuers.country"),getApp().getMessage("issuers.state"),
					getApp().getMessage("issuers.zip"),getApp().getMessage("issuers.phone"),
					getApp().getMessage("issuers.currency")
				};
		
		crud.getCrudFormFactory().setFieldCaptions(caption);
		
		setCrudFormField(crud);
		
		setCustomFields(crud);

		setConverter(crud);

		setListner(crud);

		validateDate(crud);

		crud.setAddOperation(issuer -> {
			return (Issuer) getHelper().saveEntity(issuer);
		});
		crud.setUpdateOperation(issuer -> {
			try {
				return getHelper().updateEntity(issuer);
			} catch (BLException e) {
				e.printStackTrace();
				return null;
			}
		});

		return crud;
	}

	private void setCustomFields(GridCrud<Issuer> crud) {
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, 
				"institutionId","tz", 
				"active", "startDate",
				"name","endDate",
				"description",   "journal", 
				"contactName","assetsAccount", 
				"contactPosition","earningsAccount",
				 "contactEmail","lossesAccount", 
				 "address1", "address2",
				 "country", "state",
				 "zip", "phone","localCurrency");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, 
				"institutionId","tz", 
				"active", "startDate",
				"name","endDate",
				"description",   "journal", 
				"contactName","assetsAccount", 
				"contactPosition","earningsAccount",
				 "contactEmail","lossesAccount", 
				 "address1", "address2",
				 "country", "state",
				"zip", "phone","localCurrency");

	}

	private void validateDate(GridCrud<Issuer> gridCrud) {
		try {
			gridCrud.getCrudFormFactory().setFieldProvider("startDate", () -> startDatePicker);
			gridCrud.getCrudFormFactory().setFieldProvider("endDate", () -> endDatePicker);
			startDatePicker.addValueChangeListener(e -> endDatePicker.setMin(e == null ? null : e.getValue()));
			endDatePicker.addValueChangeListener(e -> startDatePicker.setMax(e == null ? null : e.getValue()));
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	private void setListner(GridCrud<Issuer> crud) {
		crud.getCrudFormFactory().setFieldCreationListener("institutionId", field -> {
			institutionIdField = (TextField) field;
			institutionIdField.addValueChangeListener(event -> {
				try {
					preLoadAccountFieldsWithInstitutionId(event.getValue());
				} catch (Exception e) {
					getApp().getLog().error(e);
				}
			});
		});

		crud.getCrudFormFactory().setFieldCreationListener("earningsAccount", field -> {
			earningsAccountField = (TextField) field;
		});

		crud.getCrudFormFactory().setFieldCreationListener("assetsAccount", field -> {
			assetsAccountField = (TextField) field;
		});

		crud.getCrudFormFactory().setFieldCreationListener("lossesAccount", field -> {
			lossesAccountField = (TextField) field;
		});
	}

	private void setCrudFormField(GridCrud<Issuer> crud) {
		crud.getCrudFormFactory().setFieldProvider("journal", new ComboBoxProvider("journal", getHelper().getJournals(),
				null, (ItemLabelGenerator<Journal>) journal -> journal != null ? journal.getName() : ""));
		crud.getCrudFormFactory().setFieldType("startDate", DatePicker.class);
		crud.getCrudFormFactory().setFieldType("endDate", DatePicker.class);
		crud.getCrudFormFactory().setFieldType("assetsAccount", TextField.class);
		crud.getCrudFormFactory().setFieldType("earningsAccount", TextField.class);
		crud.getCrudFormFactory().setFieldType("lossesAccount", TextField.class);
		crud.getCrudFormFactory().setFieldType("institutionId", TextField.class);
		crud.getCrudFormFactory().setFieldType("description", TextField.class);
		
		crud.getCrudFormFactory().setConverter("localCurrency", new StringToShortConverter(null));
		
		crud.getCrudFormFactory().setFieldProvider("tz", () -> createTimeZoneCombo("tz"));
		
		crud.getCrudFormFactory().setFieldProvider("country", () -> createCountryCombo(""));
		crud.getCrudFormFactory().setConverter("country", new SysConfigConverter("country."));

		statesComboBox = createStatesCombo("mstate");
		crud.getCrudFormFactory().setFieldProvider("state", () -> statesComboBox);
	}

	private ComboBox<String> createTimeZoneCombo(String propertyId) {
		ComboBox<String> field = new ComboBox(QIUtils.getCaptionFromId("field." + propertyId));
		field.setItemLabelGenerator(
				(ItemLabelGenerator<String>) captionGenerator -> captionGenerator != null ? captionGenerator
						: "");
		field.setReadOnly(true);
		try {
			field.setItems(Arrays.asList("America/Montevideo","America/Sao_Paulo","America/Buenos_Aires","UTC"));
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return field;
	}

	private SysConfigComboBox createSysConfigCombo(String prefix, String propertyId) {
		SysConfigComboBox field;
		try {
			field = new SysConfigComboBox(QIUtils.getCaptionFromId("field." + propertyId), prefix);
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return field;
	}

	private ComboBox<SysConfig> createCountryCombo(String propertyId) {
		ComboBox<SysConfig> cb = createSysConfigCombo("country.", propertyId);
		if (cb != null) {
			cb.addValueChangeListener((HasValue.ValueChangeListener) event -> {
				SysConfig countryConfig = (SysConfig) event.getValue();
				if (countryConfig != null) {
					String countryCode = countryConfig.getId().substring("country.".length());
					try {
						statesComboBox.setItems(((IssuerHelper) getHelper()).getStates(countryCode));
					} catch (Exception e) {
						getApp().getLog().error(e.getMessage());
						getApp().displayNotification("Error" + e.getMessage());
					}
				}
			});
		}
		return cb;
	}

	private ComboBox<State> createStatesCombo(String propertyId) {
		ComboBox<State> field = new ComboBox(QIUtils.getCaptionFromId("field." + propertyId));
		field.setItemLabelGenerator(
				(ItemLabelGenerator<State>) captionGenerator -> captionGenerator != null ? captionGenerator.getName()
						: "");
		field.setReadOnly(true);
		try {
			field.setItems(((IssuerHelper) getHelper()).getStates(null));
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return field;
	}


	private void setConverter(GridCrud<Issuer> crud) {
		crud.getCrudFormFactory().setConverter("assetsAccount", new AccountConverter(true, true, false));
		crud.getCrudFormFactory().setConverter("earningsAccount", new AccountConverter(true, true, false));
		crud.getCrudFormFactory().setConverter("lossesAccount", new AccountConverter(true, true, false));
	}

	private void preLoadAccountFieldsWithInstitutionId(String institutionId) throws Exception {
		if (isFieldEditable(assetsAccountField)) {
			Account root = getHelper().getRootIssuersAssetsAccount();
			if (root != null)
				assetsAccountField.setValue(root.getCode() + "." + institutionId);
		}
		if (isFieldEditable(earningsAccountField)) {
			Account root = getHelper().getRootIssuersEarningsAccount();
			if (root != null)
				earningsAccountField.setValue(root.getCode() + "." + institutionId);
		}
		if (isFieldEditable(lossesAccountField)) {
			Account root = getHelper().getRootIssuersLossesAccount();
			if (root != null)
				lossesAccountField.setValue(root.getCode() + "." + institutionId);
		}
	}

	private boolean isFieldEditable (TextField field) {
		return field != null && !field.isReadOnly() && field.isEnabled();
	}

	@Override
	public QIHelper createHelper() {
		return new IssuerHelper(getViewConfig());
	}

	public IssuerHelper getHelper() {
		return (IssuerHelper) super.getHelper();
	}

	@Override
	public Component initContent() {
		Component component = super.initContent();
		return component;
	}

}
