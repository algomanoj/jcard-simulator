package org.jpos.qi.views.vp;

import java.util.HashSet;

import org.jpos.ee.BLException;
import org.jpos.ee.CardProduct;
import org.jpos.ee.VelocityProfile;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.VelocityProfileHelper;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.theme.lumo.Lumo;

public class VelocityProfileView extends QIEntityView<VelocityProfile> {

	private GridCrud<VelocityProfile> gridCrud;
	//private ComboBox cardProductCombo;
	//ConfigurableFilterDataProvider filteredDataProvider;
	//DataProvider<VelocityProfile, CardProduct> dataProvider;
	//CardProduct cp = null;
	//private ComboBox<CardProduct> cardProductDropDown;

	public VelocityProfileView() {
		super(VelocityProfile.class, "velocity_profile");
	}

	@Override
	public GridCrud createCrud() {
		gridCrud = super.createCrud();
/*		
		dataProvider = getHelper().getDataProvider();
		filteredDataProvider = dataProvider.withConfigurableFilter();
		gridCrud.getGrid().setDataProvider(filteredDataProvider);

		cardProductDropDown = new ComboBox("cardProduct",
				getHelper() == null ? new HashSet<>() : getHelper().getCardProducts());
		cardProductDropDown
				.setItemLabelGenerator((ItemLabelGenerator<CardProduct>) cardP -> cardP != null ? cardP.getName() : "");
		gridCrud.getCrudFormFactory().setFieldProvider("cardProduct", () -> cardProductDropDown);


		gridCrud.getFindAllButton().addClickListener(e -> {
			cardProductCombo.setValue(null);
		});
*/
		gridCrud.setAddOperation(vp -> {
			try {
				return getHelper().saveEntity(vp);
			} catch (BLException e1) {
				e1.printStackTrace();
				return null;
			}
		});

		gridCrud.setUpdateOperation(vp -> {
			try {
				return getHelper().updateEntity(vp);
			} catch (BLException e) {
				e.printStackTrace();
				return null;
			}
		});
		return gridCrud;
	}

	@Override
	public QIHelper createHelper() {
		return new VelocityProfileHelper(getViewConfig());
	}

	public VelocityProfileHelper getHelper() {
		return (VelocityProfileHelper) super.getHelper();
	}

	@Override
	public Component initContent() {
		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(getApp().getMessage(getName()));
		viewTitle.addClassNames("mt-s", "text-l");
		gridCrud = createCrud();
		vl.add(viewTitle,  gridCrud);
		if (getEntityId() != null)
			setBean((VelocityProfile) getHelper().getEntityById(getEntityId()));
		if (getBean() != null)
			gridCrud.getGrid().select(getBean());
		
		return vl;
	}

	/*
	private ComboBox createCardProductCombo() {
		ComboBox field = new ComboBox(getApp().getMessage("cardproduct"));
		field.setItems(getHelper().getCardProducts());
		// field.setThemeName(Lumo.LIGHT);
		field.setItemLabelGenerator((ItemLabelGenerator) item -> {
			CardProduct b = (CardProduct) item;
			return b.getName();
		});
		// field.addValueChangeListener(createValueChangeRefreshListener());
		return field;
	}*/
}
