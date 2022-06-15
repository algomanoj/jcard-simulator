package org.jpos.qi.views.card;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.qi.QI;import org.jpos.qi.util.CommonUtils;
import org.jpos.qi.util.DateRange;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CardTabSheet extends Tabs {
    private Card card;
    private QI app;
    private Map<Tab, Component> tabsToPages;
    private VerticalLayout tabsContainer;
    private Set<Card> cards;
    private CardHolder cardHolder;

    public CardTabSheet (Card c, VerticalLayout tabsContainer,Set<Card> cards, CardHolder cardHolder) {
        super();
        setWidthFull();
        this.card = c;
        this.tabsContainer = tabsContainer;
        app = QI.getQI();
        this.cards=cards;
        tabsToPages = new HashMap<>();
        this.cardHolder=cardHolder;
        addTabs();
    }

    private void addTabs() {
        Tab cardInformation = new Tab(app.getMessage("card.form"));
        Tab cardHolderInformation = new Tab(app.getMessage("cardHolder.form"));
        Tab cardTranLogs = new Tab(app.getMessage("card.transactions"));
        Tab cardAdjustments = new Tab(app.getMessage("card.adjustments"));
        add(cardInformation, cardHolderInformation, cardTranLogs, cardAdjustments);
        tabsToPages.put(cardInformation, CommonUtils.isEmpty(cards) ? getDefaultLabel(app.getMessage("cards.not.found")) :new CardsResultGrid(cards));
        tabsToPages.put(cardHolderInformation, new CardHolderInformationForm(cardHolder));
        
        RealAccountTranLogGrid realAccountTranLogGrid=new RealAccountTranLogGrid(card, new DateRange(DateRange.ALL_TIME));
        tabsToPages.put(cardTranLogs, realAccountTranLogGrid.getItemCount() == 0 ? getDefaultLabel(app.getMessage("cards.transactions.not.found")) :realAccountTranLogGrid.renderGrid());
        tabsToPages.put(cardAdjustments, getDefaultLabel(app.getMessage("adjustment.empty")));

        addSelectedChangeListener(event -> {
            Tab selected = event.getSelectedTab();
            tabsContainer.removeAll();
            tabsContainer.add(tabsToPages.get(selected));
        });
        tabsContainer.add(tabsToPages.get(cardInformation));
    }

	private Component getDefaultLabel(String msg) {
		Label cardLabel= new Label();
        cardLabel.addClassName("searchCardHolderLabel");
        cardLabel.setText(msg);
        return cardLabel;
	}
}
