package org.jpos.qi.views.fee;

import java.util.HashSet;

import org.jpos.ee.CardProduct;
import org.jpos.ee.Fee;
import org.jpos.ee.Issuer;
import org.jpos.qi.services.FeesHelper;
import org.jpos.qi.services.QIHelper;
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

public class FeesView extends QIEntityView<Fee> {

	private GridCrud<Fee> gridCrud;
	private ComboBox<CardProduct> cardProductCombo;
	private TextField searchField;
	ConfigurableFilterDataProvider filteredDataProvider;
	DataProvider<Fee, CardProduct> dataProvider;
	CardProduct cp = null;
	private ComboBox<CardProduct> cardProductDropDown;

	public FeesView() {
		super(Fee.class, "fees");
	}
/*
	@Override
	public GridCrud createCrud() {
		this.gridCrud = super.createCrud();
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
		return gridCrud;
	}
*/
	@Override
	public QIHelper createHelper() {
		return new FeesHelper(getViewConfig());
	}

	public FeesHelper getHelper() {
		return (FeesHelper) super.getHelper();
	}
/*
	@Override
	public Component initContent() {
		cardProductCombo = createCardProductCombo();
		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(getApp().getMessage(getName()));
		viewTitle.addClassNames("mt-s", "text-l");
		gridCrud = createCrud();
		vl.add(viewTitle, cardProductCombo, gridCrud);
		if (getEntityId() != null)
			setBean((Fee) getHelper().getEntityById(getEntityId()));
		if (getBean() != null)
			gridCrud.getGrid().select(getBean());
		cardProductCombo.addValueChangeListener(e -> {
			try {
				filteredDataProvider = dataProvider.withConfigurableFilter();
				gridCrud.getGrid().setDataProvider(filteredDataProvider);
				this.cp = (CardProduct) e.getValue();
				filteredDataProvider.setFilter(this.cp);
				dataProvider.refreshAll();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		return vl;
	}*/
/*
	private ComboBox createCardProductCombo() {
		ComboBox field = new ComboBox(getApp().getMessage("cardproduct"));
		field.setItems(getHelper().getCardProducts());
		// field.setThemeName(Lumo.LIGHT);
		field.setItemLabelGenerator((ItemLabelGenerator) item -> {
			CardProduct b = (CardProduct) item;
			return b.getName();
		});

		return field;
	}
*/
}
