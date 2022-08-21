package org.jpos.qi.views.demo;

import java.util.List;

import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.CardHolderManager;
import org.jpos.ee.DB;
import org.jpos.qi.QI;
import org.jpos.qi.services.SearchManager;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class CardSearchView extends Composite<VerticalLayout> {
	private static final long serialVersionUID = 1L;

	private TextField tokenField;
	private TextField cardProductField;
	private TextField schemeField;
	private TextField cardHolderRealIdField;
	private Button searchButton;
	private HorizontalLayout responseHL;
	private QI app;
	private Label cardNumberResponse;
	public CardSearchView() {
		super();
		app = QI.getQI();	
	}
	
	@Override
	public VerticalLayout initContent() {
		VerticalLayout vl = new VerticalLayout();
		
		//Label tokenLabel = new Label("Token");
		tokenField = new TextField();
		tokenField.setPlaceholder("Token");
		//Label cardProductLabel = new Label("Card Product");
		cardProductField = new TextField();
		cardProductField.setPlaceholder("Card Product");
		//Label schemeLabel = new Label("Token");
		schemeField = new TextField();
		schemeField.setPlaceholder("Scheme");
		//Label cardHolderRealIdLabel = new Label("Token");
		cardHolderRealIdField = new TextField();
		cardHolderRealIdField.setPlaceholder("Real Id");
		searchButton = new Button("Search");
		searchButton.addClickListener(e-> {
			searchCard();
		});
		
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidthFull();
		hl1.add(tokenField,cardProductField,schemeField,cardHolderRealIdField,searchButton);

		responseHL = new HorizontalLayout();
		responseHL.setWidthFull();
		responseHL.getStyle().set("border-top", "1px solid gray");
		responseHL.setVisible(false);
		VerticalLayout responseLabelVL = new VerticalLayout(new Label("Response"));
		responseLabelVL.setWidthFull();
		VerticalLayout responseDataVL = new VerticalLayout();
		responseDataVL.setWidthFull();
		cardNumberResponse = new Label();
		responseDataVL.add(new Label("Card Number:"), cardNumberResponse);
		responseHL.add(responseLabelVL, responseDataVL);

		vl.add(hl1,responseHL);
		return vl;	
	}
	private void searchCard() {
		responseHL.setVisible(true);
		Card card = getRandomCard();
		cardNumberResponse.removeAll();
		String cardNumber = "";
		if(card!=null) {
			cardNumber = card.getBin()==null?"":card.getBin();
			cardNumber = cardNumber+" "+card.getLastFour()==null?"":card.getLastFour();
		}
		cardNumberResponse.add(cardNumber);
	}
	
	private Card getRandomCard() {
		try {
			return (Card) DB.exec((db) -> {
				SearchManager<Card> mgr = new SearchManager<Card>(db,Card.class);
				return mgr.getRandomCard(tokenField.getValue(), cardProductField.getValue(), schemeField.getValue(), cardHolderRealIdField.getValue());
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public TextField getTokenField() { return tokenField; }
	public TextField getCardProductField() { return cardProductField; }
	public TextField getSchemeField() { return schemeField; }
	public TextField getCardHolderRealIdField() { return cardHolderRealIdField; }
	public Button getSearchButton() { return searchButton; }
	
}
