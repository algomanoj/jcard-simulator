package org.jpos.qi.views.merchant;

import org.jpos.ee.DB;
import org.jpos.ee.Merchant;
import org.jpos.ee.State;
import org.jpos.ee.SysConfig;
import org.jpos.ee.SysConfigManager;
import org.jpos.qi.QIUtils;
import org.jpos.qi.services.CardHolderHelper;
import org.jpos.qi.services.MerchantHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.util.SysConfigComboBox;
import org.jpos.qi.util.SysConfigConverter;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;

public class MerchantView extends QIEntityView<Merchant> {

	private static final long serialVersionUID = -9160920591299009485L;
	GridCrud<Merchant> crud ;
	private ComboBox<State> statesComboBox;
	
	public MerchantView() {
        super(Merchant.class, "merchants");
	}
	
	@Override
	public GridCrud<Merchant> createCrud() {
		crud = super.createCrud();
		String[] caption = {
					getApp().getMessage("merchant.merchantId"),getApp().getMessage("merchant.name"),
					getApp().getMessage("merchant.active"),getApp().getMessage("merchant.contact"),
					getApp().getMessage("merchant.address1"),getApp().getMessage("merchant.address2"),
					getApp().getMessage("merchant.country"),getApp().getMessage("merchant.state"),
					getApp().getMessage("merchant.city"),getApp().getMessage("merchant.province"),
					getApp().getMessage("merchant.zip"),getApp().getMessage("merchant.phone")
				};
		initiateGrid(crud.getGrid());
		crud.getCrudFormFactory().setFieldCaptions(caption);
		setCrudFormField(crud);
		setCustomFieldsOrder(crud);
		return crud;
	}
	
	private void initiateGrid(Grid<Merchant> grid) {
		grid.removeAllColumns();
		grid.addColumn(Merchant::getId).setHeader(getApp().getMessage("merchant.id")).setSortable(true).setKey("id");
		grid.addColumn(Merchant::getMerchantId).setHeader(getApp().getMessage("merchant.merchantId")).setSortable(true).setKey("merchantId");
		grid.addColumn(Merchant::getName).setHeader(getApp().getMessage("merchant.name")).setSortable(true)
				.setKey("name");
		grid.addColumn(Merchant::getCity).setHeader(getApp().getMessage("merchant.city")).setSortable(true)
		.setKey("city");
		grid.addColumn(m -> {
			if(m.getCountry() != null && !m.getCountry().isBlank()) {
				try {
					SysConfig sys = (SysConfig) DB.exec( (db) -> {
					    SysConfigManager mgr = new SysConfigManager(db, "country.");
					    return mgr.getObject(m.getCountry());
					});
					return sys.getValue();
				} catch (Exception e) {
					getApp().getLog().error(e.getMessage());
					return "";
				}
				
			}else {
				return "";
			}
		}).setHeader(getApp().getMessage("merchant.country")).setSortable(true).setKey("country");
		
	}

	private void setCustomFieldsOrder(GridCrud<Merchant> crud) {
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, 
				"merchantId","name", 
				"active", "contact",
				"address1","address2",
				"country",   "state", 
				"city","province", 
				"zip","phone");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, 
				"merchantId","name", 
				"active", "contact",
				"address1","address2",
				"country",   "state", 
				"city","province", 
				"zip","phone");
	}
	
	private void setCrudFormField(GridCrud<Merchant> crud) {
		
		crud.getCrudFormFactory().setFieldProvider("country", () -> createCountryCombo(""));
		crud.getCrudFormFactory().setConverter("country", new SysConfigConverter("country."));
		
		statesComboBox = createStatesCombo("state");
		crud.getCrudFormFactory().setFieldProvider("state", () -> statesComboBox);
	}

	private ComboBox<State> createStatesCombo(String propertyId) {
		ComboBox<State> field = new ComboBox(QIUtils.getCaptionFromId("field." + propertyId));
		field.setItemLabelGenerator(
				(ItemLabelGenerator<State>) captionGenerator -> captionGenerator != null ? captionGenerator.getName()
						: "");
		field.setReadOnly(true);
		try {
			field.setItems(((MerchantHelper) getHelper()).getStates(null));
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
						statesComboBox.setItems(((MerchantHelper) getHelper()).getStates(countryCode));
					} catch (Exception e) {
						getApp().getLog().error(e.getMessage());
						getApp().displayNotification("Error" + e.getMessage());
					}
				}
			});
		}
		return cb;
	}



    @Override
    public QIHelper createHelper() {
        return new MerchantHelper(getViewConfig());
    }

    public MerchantHelper getHelper() {
        return (MerchantHelper) super.getHelper();
    }

    @Override
    public Component initContent() {
        Component component = super.initContent();
        return component;
    }

}
