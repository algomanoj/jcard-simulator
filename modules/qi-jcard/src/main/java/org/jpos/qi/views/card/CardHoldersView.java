package org.jpos.qi.views.card;

import org.jpos.ee.BLException;
import org.jpos.ee.CardHolder;
import org.jpos.ee.Issuer;
import org.jpos.ee.State;
import org.jpos.ee.SysConfig;
import org.jpos.gl.Journal;
import org.jpos.qi.QIUtils;
import org.jpos.qi.services.CardHolderHelper;
import org.jpos.qi.services.IssuerHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.util.StringToShortConverter;
import org.jpos.qi.util.SysConfigComboBox;
import org.jpos.qi.util.SysConfigConverter;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;

public class CardHoldersView extends QIEntityView<CardHolder> {

	private static final long serialVersionUID = 540332019695555602L;
	GridCrud<CardHolder> crud;
	private ComboBox<State> statesComboBox;

	public CardHoldersView() {
		super(CardHolder.class, "cardholders");
	}

    @Override
    public QIHelper createHelper() {
        return new CardHolderHelper(getViewConfig());
    }
    
    @Override
	public GridCrud<CardHolder> createCrud() {
		crud = super.createCrud();

		String[] caption = { 
					getApp().getMessage("cardholders.firstName"),getApp().getMessage("cardholders.middleName"),
					getApp().getMessage("cardholders.lastName"),getApp().getMessage("cardholders.lastName2"),
					getApp().getMessage("cardholders.email"),getApp().getMessage("cardholders.phone"),
					getApp().getMessage("cardholders.birthDate"),getApp().getMessage("cardholders.active"),
					getApp().getMessage("cardholders.address1"),getApp().getMessage("cardholders.country"),
					getApp().getMessage("cardholders.address2"),getApp().getMessage("cardholders.state"),
					getApp().getMessage("cardholders.city"),getApp().getMessage("cardholders.zip")
				};
		crud.getCrudFormFactory().setFieldCaptions(caption);
		
		setCrudFormField(crud);
		
		setCustomFields(crud);

		return crud;
	}
    
    private void setCustomFields(GridCrud<CardHolder> crud) {
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, 
				"firstName","middleName", 
				"lastName", "lastName2",
				"email","phone",
				"birthDate","active",
				"address1",   "country",
				"address2", "state",
				"city","zip");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, 
				"firstName","middleName", 
				"lastName", "lastName2",
				"email","phone",
				"birthDate","active",
				"address1",   "country",
				"address2", "state",
				"city","zip");

	}
    
    private void setCrudFormField(GridCrud<CardHolder> crud) {
		crud.getCrudFormFactory().setFieldType("birthDate", DatePicker.class);
		
		crud.getCrudFormFactory().setFieldProvider("country", () -> createCountryCombo(""));
		crud.getCrudFormFactory().setConverter("country", new SysConfigConverter("country."));

		statesComboBox = createStatesCombo("mstate");
		crud.getCrudFormFactory().setFieldProvider("state", () -> statesComboBox);

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
						statesComboBox.setItems(((CardHolderHelper) getHelper()).getStates(countryCode));
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
			field.setItems(((CardHolderHelper) getHelper()).getStates(null));
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return field;
	}

    public CardHolderHelper getHelper() {
        return (CardHolderHelper) super.getHelper();
    }

    @Override
    public Component initContent() {
        Component component = super.initContent();
        return component;
    }

}
