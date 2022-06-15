package org.jpos.qi.views.card;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.data.converter.StringToDateConverter;
import org.jpos.ee.Card;
import org.jpos.qi.QI;
import org.jpos.qi.QIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CardInformationForm extends FormLayout {
    private Card card;
    private Binder<Card> binder;
    private TextField cardProduct;
    private TextField bin;
    private TextField lastFour;
    private TextField account;
    private TextField state;
    private TextField endDate;

    public CardInformationForm() {
        this(null);
    }

    public CardInformationForm(Card card) {
        super();
        cardProduct = createField("cardProduct");
        bin = createField("bin");
        lastFour = createField("lastFour");
        account = createField("account");
        state = createField("state");
        endDate = createField("endDate");
        add (cardProduct, bin, account, state, endDate);
        binder = new Binder<>(Card.class);
        binder.forField(cardProduct).bind(c -> c.getCardProduct() != null ?
          c.getCardProduct().getCode() + " - " + c.getCardProduct().getName() : null, null);
        binder.forField(account).bind(c -> c.getAccount() != null ?
            c.getAccount().getCode() + " - " + c.getAccount().getDescription() : null,
            null);
        binder.forField(state).bind(c -> c.getState().name(), null);
        StringToDateConverter dateConverter = new StringToDateConverter() {
            @Override
            protected DateFormat getFormat(Locale locale) {
                return new SimpleDateFormat(QI.getQI().getMessage("dateformat"));
            }
        };
        binder.forField(endDate).withConverter(dateConverter).bind("endDate");
        binder.bindInstanceFields(this);
        binder.setReadOnly(true);
        setCard(card);
    }

    public void setCard (Card card) {
        if (card != null) {
            this.card = card;
            binder.readBean(card);
        }
    }

    private TextField createField (String propertyId) {
        TextField tf = new TextField(QIUtils.getCaptionFromId("field." + propertyId));
        tf.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        return tf;
    }
}
