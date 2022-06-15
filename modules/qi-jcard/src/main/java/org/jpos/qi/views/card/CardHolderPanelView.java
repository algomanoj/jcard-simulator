package org.jpos.qi.views.card;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.jpos.ee.CardHolder;
import org.jpos.qi.QI;
import org.jpos.qi.services.CardHolderHelper;

import java.util.Optional;

public class CardHolderPanelView extends Composite<VerticalLayout> implements BeforeEnterObserver, AfterNavigationObserver {
    private QI app;
    private String name;
    private String entityId;
    private CardHolder cardHolder;
    private CardHolderHelper helper;
    private H2 viewTitle;
    private TextField emailField;
    private TextField phoneField;
//    private CardHolderPanelSummary cardHolderPanelSummary;

    public CardHolderPanelView() {
        super();
        this.app = QI.getQI();
        this.name = "cardholder-panel";
        helper = new CardHolderHelper(null);
    }

    @Override
    public VerticalLayout initContent() {
        VerticalLayout vl = new VerticalLayout();
        vl.setHeightFull();
        viewTitle = new H2(app.getMessage(name));
        viewTitle.addClassNames("mt-s", "text-l");
//        cardHolderPanelSummary = new CardHolderPanelSummary();
        vl.add(viewTitle, createCardHolderSummary());
        return vl;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> id = event.getRouteParameters().get("id");
        entityId = id.isPresent() && id != null ? id.get() : null;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        if (entityId != null)
            setCardHolder((CardHolder) getHelper().getEntityById(entityId));
    }

    public CardHolder getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(CardHolder cardHolder) {
        this.cardHolder = cardHolder;
        if (cardHolder != null) {
            viewTitle.setText(app.getMessage(name) + ": " + cardHolder.getFirstName() + " " + (cardHolder.getLastName() != null ? cardHolder.getLastName()  : ""));
            emailField.setValue(cardHolder.getEmail() !=null ? cardHolder.getEmail() : "--");
            phoneField.setValue(cardHolder.getPhone() !=null ? cardHolder.getPhone() :"--");
            VerticalLayout tabsContainer = new VerticalLayout();
            tabsContainer.setSizeFull();
            CardTabSheet tabSheet = new CardTabSheet(cardHolder.getPrimaryCard(), tabsContainer,cardHolder.getCards(),cardHolder);
            getContent().add(tabSheet, tabsContainer); 
        }

    }

    private VerticalLayout createCardHolderSummary () {
    	//added Form Layout to show Label on left side like Email abc@xyz.com
        FormLayout formLayout = new FormLayout();
        emailField = new TextField();
        phoneField = new TextField();
        emailField.setReadOnly(true);
        phoneField.setReadOnly(true);
        formLayout.addFormItem(emailField, app.getMessage("label.email"));
        formLayout.addFormItem(phoneField, app.getMessage("label.phone"));
        return new VerticalLayout(formLayout);
    }

    public CardHolderHelper getHelper() {
        return helper;
    }

    public void setHelper(CardHolderHelper helper) {
        this.helper = helper;
    }
}
