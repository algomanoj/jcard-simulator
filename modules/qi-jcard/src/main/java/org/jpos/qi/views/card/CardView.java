package org.jpos.qi.views.card;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jpos.ee.BLException;
import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.CardProduct;
import org.jpos.ee.Issuer;
import org.jpos.gl.FinalAccount;
import org.jpos.qi.QIUtils;
import org.jpos.qi.services.CardHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.util.NotificationUtils;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;

public class CardView extends QIEntityView<Card> {

	private static final long serialVersionUID = 540332019695555602L;
	private GridCrud<Card> crud;
	ConfigurableFilterDataProvider filteredDataProvider;
	DataProvider<Card, CardProduct> dataProvider;
	private ComboBox<CardProduct> cardProductComboBox;
	private CardProduct selectedCardProduct;
	private ComboBox<CardProduct> cardProdCrudCombo;
	private DatePicker endDatePicker = new DatePicker();
	private ComboBox<FinalAccount> finalAccountCombo;
	private ComboBox<CardHolder> cardHolderCombo;
	private TextField binField;
	private Notification errorNotification;
	
	
	public CardView() {
		super(Card.class, "cards");
	}

	@Override
	public GridCrud<Card> createCrud() {
		crud = super.createCrud();
		String[] captions = {  null, null, null,  null, null, getApp().getMessage("virtual", null),getApp().getMessage("expiryDate", null), 
				null };
		crud.getCrudFormFactory().setFieldCaptions(captions);

		dataProvider = getHelper().getDataProvider();
		filteredDataProvider = dataProvider.withConfigurableFilter();
		crud.getGrid().setDataProvider(filteredDataProvider);

		initiateGrid(crud.getGrid());

		setCustomField();

		crud.getAddButton().addClickListener(e -> {
			if (cardProductComboBox.getValue() == null) {
				cardProductComboBox.setReadOnly(false);
				crud.getCrudLayout().hideForm();
				NotificationUtils.getInstance().showErrorNotification(getApp().getMessage("cardproduct.notselected"), this.errorNotification);

			} else {
				cardProdCrudCombo.setReadOnly(true);
				List<CardProduct> cardProducts = new ArrayList<>();
				CardProduct cardProduct = cardProductComboBox.getValue();
				cardProducts.add(cardProduct);
				cardProdCrudCombo.setItems(cardProducts);
				cardProdCrudCombo.setValue(cardProduct);
				binField.setValue(cardProduct.getBin());
			}
		});

		crud.getFindAllButton().addClickListener(e -> {
			cardProductComboBox.setValue(null);
			cardProdCrudCombo.setValue(null);
		});
		//
		setCustomCrudOperation();
		return crud;
	}

	private void setCustomCrudOperation() {
		crud.setAddOperation(issuer -> {
			try {
				return (Card) getHelper().saveEntity(issuer);
			} catch (BLException e) {
				e.printStackTrace();
				return issuer;
			}

		});
		crud.setUpdateOperation(issuer -> {
			try {
				return getHelper().updateEntity(issuer);
			} catch (BLException e) {
				e.printStackTrace();
				return null;
			}
		});

	}

	private void setCustomField() {
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, 
				"cardProduct","bin",
				"cardHolder", "account", "active", "virtual", "endDate",  "state");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE,
				"cardProduct","bin",
				"cardHolder", "account", "active", "virtual", "endDate","state");
		crud.getCrudFormFactory().setFieldProvider("cardProduct", () -> createCrudCardProductCombo());
		crud.getCrudFormFactory().setFieldCreationListener("cardProduct", field -> {
			cardProdCrudCombo = (ComboBox<CardProduct>) field;
		});
		LocalDate now = LocalDate.now(ZoneId.systemDefault());
		endDatePicker.setMin(now);
		//endDatePicker.setMax(now.plusYears(10));
		crud.getCrudFormFactory().setFieldProvider("endDate", () -> endDatePicker);
        crud.getCrudFormFactory().setFieldProvider("cardHolder", () -> createCrudCardHolderCombo());
        
        crud.getCrudFormFactory().setFieldProvider("account", () -> createCrudAccountCombo());
        
		crud.getCrudFormFactory().setFieldCreationListener("cardHolder", field -> {
			cardHolderCombo = (ComboBox<CardHolder>) field;
			cardHolderCombo.addValueChangeListener(event -> {
				CardHolder cardHolder = event.getValue();
				if (finalAccountCombo != null && cardHolder != null) {
					List<FinalAccount> finalAccountList = new ArrayList<FinalAccount>();
					for (Map.Entry<String, FinalAccount> entry : cardHolder.getAccounts().entrySet()) {
						entry.getValue().setDescription(entry.getKey() + "|" + entry.getValue().getCode());
						finalAccountList.add(entry.getValue());
					}
					finalAccountCombo.setItemLabelGenerator(
							(ItemLabelGenerator<FinalAccount>) captionGenerator -> captionGenerator != null
									? captionGenerator.getDescription()
									: "");

					finalAccountCombo.setItems(finalAccountList);
				}
			});
		});
		
		crud.getCrudFormFactory().setFieldType("bin", TextField.class);
		crud.getCrudFormFactory().setFieldCreationListener("bin", field -> {
			binField = (TextField) field;
			binField.setReadOnly(true);
			
		});

	}

	private ComboBox<FinalAccount> createCrudAccountCombo() {
		finalAccountCombo= new ComboBox(QIUtils.getCaptionFromId("field." + "account"));
		finalAccountCombo.setItemLabelGenerator((ItemLabelGenerator<FinalAccount>) captionGenerator -> captionGenerator != null
				? captionGenerator.getDescription() : "");
		try {
			List<FinalAccount> finalAccountList = new ArrayList<FinalAccount>();
			finalAccountCombo.setItems(finalAccountList);
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return finalAccountCombo;
	}

	private ComboBox<CardHolder> createCrudCardHolderCombo() {
		cardHolderCombo = new ComboBox(QIUtils.getCaptionFromId("field." + "cardholder"));
		cardHolderCombo.setItemLabelGenerator((ItemLabelGenerator<CardHolder>) captionGenerator -> captionGenerator != null
				? captionGenerator.getFirstName() +" "+ (captionGenerator.getLastName() != null ? captionGenerator.getLastName() : "")
				: "");
		try {
			cardHolderCombo.setItems(((CardHelper) getHelper()).getAllCardHolders());
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return cardHolderCombo;
	}

	private ComboBox<CardProduct> createCrudCardProductCombo() {
		ComboBox<CardProduct> field = new ComboBox(QIUtils.getCaptionFromId("field." + "cardproduct"));
		field.setItemLabelGenerator((ItemLabelGenerator<CardProduct>) captionGenerator -> captionGenerator != null
				? captionGenerator.getName()
				: "");
		field.setReadOnly(true);
		try {
			List<CardProduct> cardProducts = new ArrayList<CardProduct>();
			field.setItems(cardProducts);
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return field;
	}

	private void initiateGrid(Grid<Card> grid) {
		grid.removeAllColumns();
		grid.addColumn(Card::getId).setHeader(getApp().getMessage("card.id")).setSortable(true).setKey("id");
		grid.addColumn(Card::getToken).setHeader(getApp().getMessage("card.token"))
			.setSortable(true)
			.setKey("token");
		grid.addColumn(Card::getLastFour).setHeader(getApp().getMessage("card.lastFour"))
			.setSortable(true)
			.setKey("lastFour");
		grid.addColumn(Card::getBin).setHeader(getApp().getMessage("card.bin")).setSortable(true).setKey("bin");
		grid.addColumn(Card::isActive).setHeader(getApp().getMessage("card.active"))
			.setSortable(true)
			.setKey("active");
		grid.addColumn(cardP -> cardP.getCardHolder() != null ? cardP.getCardHolder().getFirstName() : "")
			.setHeader(getApp().getMessage("card.cardHolder"))
			.setSortable(true)
			.setKey("cardHolder");
		grid.addColumn(Card::getEndDate).setHeader(getApp().getMessage("card.endDate")).setSortable(true)
				.setKey("endDate");
		grid.addColumn(Card::isVirtual).setHeader(getApp().getMessage("card.virtual")).setSortable(true)
				.setKey("virtual");
		grid.addColumn(Card::getState).setHeader(getApp().getMessage("card.state"))
			.setSortable(true)
			.setKey("state");
	}

	@Override
	public QIHelper createHelper() {
		return new CardHelper(getViewConfig());
	}

	public CardHelper getHelper() {
		return (CardHelper) super.getHelper();
	}

	@Override
	public Component initContent() {
		cardProductComboBox = createCardProductCombo();
		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(getApp().getMessage(getName()));
		viewTitle.addClassNames("mt-s", "text-l");
		crud = createCrud();
		errorNotification= NotificationUtils.getInstance().getErrorNotification(NotificationVariant.LUMO_PRIMARY, Notification.Position.BOTTOM_END);
		vl.add(viewTitle, cardProductComboBox, crud,errorNotification);
		if (getEntityId() != null)
			setBean((Card) getHelper().getEntityById(getEntityId()));
		if (getBean() != null)
			crud.getGrid().select(getBean());

		cardProductComboBox.addValueChangeListener(e -> {
			try {
				filteredDataProvider = dataProvider.withConfigurableFilter();
				crud.getGrid().setDataProvider(filteredDataProvider);
				this.selectedCardProduct = (CardProduct) e.getValue();
				filteredDataProvider.setFilter(this.selectedCardProduct);
				dataProvider.refreshAll();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		return vl;
	}

	private ComboBox<CardProduct> createCardProductCombo() {
		ComboBox field = new ComboBox(getApp().getMessage("cardproduct"));
		field.setItems(getHelper().getCardProducts());
		// field.setThemeName(Lumo.LIGHT);
		field.setItemLabelGenerator((ItemLabelGenerator) item -> {
			CardProduct b = (CardProduct) item;
			return b.getName();
		});

		return field;
	}
	
	private String getCardHolderName(CardHolder ch) {
		return (ch.getFirstName() == null ? ""
				: ch.getFirstName()) + " " + (ch.getLastName() == null ? "" : ch.getLastName()); 
	}

}
