package org.jpos.qi.views.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.gl.GLException;
import org.jpos.qi.QI;
import org.jpos.qi.services.SearchHelper;
import org.jpos.qi.util.CommonUtils;
import org.jpos.qi.util.NotificationUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

public class SearchView extends Composite<VerticalLayout> {
	public static final String TOKEN_PATTERN = "^$|[\\w\\s.\\-\']{6,64}$";
	public static final String NAME_PATTERN = "^$|[&0-9a-zA-Z áéíóúÁÉÍÓÚñÑ]+$";
	public static final String WORD_PATTERN = "^[\\w.\\-]*$";
	public static final String ACCOUNT_PATTERN = "^$|[a-zA-Z0-9][-._a-zA-Z0-9]{0,62}[a-zA-Z0-9]$";
	public static final String PAN_PATTERN = "^$|[\\d]{16}$";

	private QI app;
	private SearchHelper helper;
	private String name;
	private TextField cardTokenSearchField;
	private TextField cardHolderNameSearchField;
	private TextField cardHolderRealIdSearchField;
	private TextField accountSearchField;
	private Label errorLabel;
	private Grid cardHoldersGrid;
	private Notification errorNotification;

	public SearchView () {
		super();
		this.app = QI.getQI();
		this.name = "search";
		helper = new SearchHelper();
	}


	@Override
	public VerticalLayout initContent() {
		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(app.getMessage(name));
		viewTitle.addClassNames("mt-s", "text-l");
		vl.add(viewTitle, createSearchForm());
		return vl;
	}


	private HorizontalLayout createSearchForm() {
        Binder<String> binder = new Binder<>(String.class);
		FormLayout leftFormLayout = new FormLayout();
		FormLayout rightFormLayout = new FormLayout();
		cardTokenSearchField = new TextField(getApp().getMessage("search.byToken"));
//		cardTokenSearchField.setWidth("250px");
		cardHolderNameSearchField = new TextField(getApp().getMessage("search.byName"));
//		cardHolderNameSearchField.setWidth("250px");
		cardHolderRealIdSearchField = new TextField(getApp().getMessage("search.byRealId"));
//		cardHolderRealIdSearchField.setWidth("250px");
		accountSearchField = new TextField(getApp().getMessage("search.byAcct"));
//		accountSearchField.setWidth("250px");

		binder.forField(cardTokenSearchField).withValidator(
			new RegexpValidator(app.getMessage("errorMessage.invalidField", cardTokenSearchField.getLabel()),
			TOKEN_PATTERN)
		);

		cardTokenSearchField.setReadOnly(false);
		cardTokenSearchField.focus();
		cardHolderNameSearchField.setReadOnly(false);
		cardHolderRealIdSearchField.setReadOnly(false);
		accountSearchField.setReadOnly(false);

		errorLabel = new Label();
		Button searchBtn = new Button(getApp().getMessage("search"));
		searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchBtn.addClickListener( e -> {
			errorLabel.setText(null);
			errorLabel.setVisible(false);
			if (cardHoldersGrid != null)
				getContent().remove(cardHoldersGrid);
//			if (binder.isValid()) { // TODO: Check how done now in v23.
				search();
//			}

		});
		errorLabel.setVisible(false);
		HorizontalLayout hl = new HorizontalLayout();
		errorNotification= NotificationUtils.getInstance().getErrorNotification(NotificationVariant.LUMO_ERROR, Notification.Position.TOP_END);
		leftFormLayout.add(cardTokenSearchField, cardHolderNameSearchField, searchBtn, errorLabel,errorNotification);
		rightFormLayout.add(cardHolderRealIdSearchField, accountSearchField);
		hl.addAndExpand(leftFormLayout, rightFormLayout);
		return hl;
	}
	
	
	private void search() {
		boolean results = searchByCardToken() || searchByAccount() || searchByRealId() || searchByCardHolderName();
		if (!results) {
			//errorLabel.setVisible(true);
			//errorLabel.setText(getApp().getMessage("errorMessage.cardcardholderNotFound"));
			NotificationUtils.getInstance().showErrorNotification(getApp().getMessage("errorMessage.cardcardholderNotFound"), this.errorNotification);
		}
	}

	private boolean searchByCardToken() {
		if (cardTokenSearchField.getValue().isEmpty())
			return false;
		Card c = getHelper().getCardByToken(cardTokenSearchField.getValue());
		if (c == null)
			return false;
		CardHolder ch = c.getCardHolder();
		if (ch != null)
			navigateToCardHolder(ch);
		else
			UI.getCurrent().navigate("/result/card/" + c.getId());
		return true;
	}

	private boolean searchByAccount() {
		if (accountSearchField.getValue().isEmpty())
			return false;
		try {
			List<Card> cards = getHelper().getCardByAcctCode(accountSearchField.getValue());
			if (CommonUtils.isEmpty(cards))
				return false;
			showSearchResults(getCardHoldersOfCards(cards));
		} catch (GLException e) {
			getApp().getMessage(e.getMessage(), e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	private boolean searchByRealId() {
		if (cardHolderRealIdSearchField.getValue().isEmpty())
			return false;
		List<CardHolder> chs = getHelper().getCardHolderByRealId(cardHolderRealIdSearchField.getValue());
		if (CommonUtils.isEmpty(chs))
			return false;
		showSearchResults(chs);
		return true;
	}

	private boolean searchByCardHolderName() {
		if (cardHolderNameSearchField.getValue().isEmpty())
			return false;
		List<CardHolder> chs = getHelper().getCardHolderByName(cardHolderNameSearchField.getValue());
		System.out.println("---> chs.size: " + (chs != null ? chs.size() : "null"));
		if (CommonUtils.isEmpty(chs))
			return false;
		showSearchResults(chs);
		return true;
	}

	private void showSearchResults(List<CardHolder> chs) {
		if (chs != null) {
			if (chs.size() == 1) {
				navigateToCardHolder(chs.get(0));
			} else if (chs.size() > 1) {
//				CardHoldersResultGrid cardHolders = new CardHoldersResultGrid();
//				cardHoldersGrid = cardHolders.createCrud().getGrid();
				cardHoldersGrid.setItems(chs);
				getContent().add(cardHoldersGrid);
			}
		}
	}

	private void navigateToCardHolder(CardHolder ch) {
		UI.getCurrent().navigate("/cardholder-panel/" + ch.getId());
	}

	private List<CardHolder> getCardHoldersOfCards(List<Card> cards) {
		Set<CardHolder> chs = new HashSet<>();
		if (cards != null) {
			if (cards.size() == 1) {
				CardHolder ch = cards.get(0).getCardHolder();
				if (ch != null)
					chs.add(ch);
			} else {
				for (Card card : cards) {
					if (card.getCardHolder() != null)
						chs.add(card.getCardHolder());
				}
			}
		}
		return new ArrayList<>(chs);
	}

	public QI getApp() {
		return app;
	}

	public void setApp(QI app) {
		this.app = app;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TextField getCardTokenSearchField() {
		return cardTokenSearchField;
	}

	public void setCardTokenSearchField(TextField cardTokenSearchField) {
		this.cardTokenSearchField = cardTokenSearchField;
	}

	public TextField getCardHolderNameSearchField() {
		return cardHolderNameSearchField;
	}

	public void setCardHolderNameSearchField(TextField cardHolderNameSearchField) {
		this.cardHolderNameSearchField = cardHolderNameSearchField;
	}

	public TextField getCardHolderRealIdSearchField() {
		return cardHolderRealIdSearchField;
	}

	public void setCardHolderRealIdSearchField(TextField cardHolderRealIdSearchField) {
		this.cardHolderRealIdSearchField = cardHolderRealIdSearchField;
	}

	public TextField getAccountSearchField() {
		return accountSearchField;
	}

	public void setAccountSearchField(TextField accountSearchField) {
		this.accountSearchField = accountSearchField;
	}

	public SearchHelper getHelper() {
		return helper;
	}

	public void setHelper(SearchHelper helper) {
		this.helper = helper;
	}

	public Label getErrorLabel() {
		return errorLabel;
	}

	public void setErrorLabel(Label errorLabel) {
		this.errorLabel = errorLabel;
	}

}
