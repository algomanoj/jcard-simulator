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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
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
	private VerticalLayout responseVL;
	private HorizontalLayout responseDataHL;
	private QI app;
	private Label cardNumberResponse;
	public CardSearchView() {
		super();
		app = QI.getQI();	
	}
	
	@Override
	public VerticalLayout initContent() {
		VerticalLayout vl = new VerticalLayout();
		H2 viewTitle = new H2(app.getMessage("fetchCard"));
		viewTitle.addClassNames("mt-s", "text-l");
		
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
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.addClickListener(e-> {
			searchCard();
		});
		
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidthFull();
		hl1.add(tokenField,cardProductField,schemeField,cardHolderRealIdField,searchButton);

		responseVL = new VerticalLayout();
		responseVL.setWidthFull();
		responseVL.getStyle().set("border-top", "2px solid #d8d8d8");
		responseVL.setVisible(false);
		
		Label responseLabel = new Label("Response");
		responseLabel.getStyle().set("font-weight", "500");
		HorizontalLayout responseLabelHL = new HorizontalLayout(responseLabel);
		responseLabelHL.setWidthFull();
		VerticalLayout responseLabelVL = new VerticalLayout();
		responseLabelVL.setWidthFull();
		responseLabelVL.add(responseLabelHL); 
		
		responseDataHL = new HorizontalLayout();
		responseDataHL.setWidthFull();
		//cardNumberResponse = new Label();
		//responseDataVL.add(new Label("Card Number:"), cardNumberResponse);
		responseVL.add(responseLabelHL, responseDataHL);

		vl.add(viewTitle, hl1,responseVL);
		return vl;	
	}
	private void searchCard() {
		responseDataHL.removeAll();
		responseVL.setVisible(true);
		Card card = getRandomCard();
		if(card == null) {
			responseDataHL.add("No Card found for selected criteria");
		} else {
			responseDataHL.add(new Label("Card Number:"), new Label(card.getPan()), new Label("Expiry Date:"), new Label(card.getEndDate().toString()));
		}
		
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
