package org.jpos.qi.views.card;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToDateConverter;
import com.vaadin.flow.function.ValueProvider;

import org.jpos.ee.CardHolder;
import org.jpos.ee.DB;
import org.jpos.ee.SysConfigManager;
import org.jpos.qi.QI;
import org.jpos.qi.QIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CardHolderInformationForm extends FormLayout {
    private CardHolder ch;
    private Binder<CardHolder> binder;
    private TextField firstName;
    private TextField middleName;
    private TextField lastName;
    private TextField lastName2;
    private TextField email;
    private TextField address1;
    private TextField address2;
    private TextField city;
    private TextField state;
    private TextField zip;
    private TextField country;
    private TextField birthDate;

    public CardHolderInformationForm () {
        this(null);
    }
    public CardHolderInformationForm (CardHolder ch) {
        super();
        String[] visibleFields = {"firstName","middleName","lastName","lastName2","email"};
        firstName = createField("firstName");
        middleName = createField("middleName");
        lastName = createField("lastName");
        lastName2 = createField("lastName2");
        email = createField("email");
        address1 = createField("address1");
        address2 = createField("address2");
        city = createField("city");
        state = createField("state");
        zip = createField("zip");
        country = createField("country");
        birthDate = createField("birthDate");
        add (firstName, middleName, lastName, lastName2, email, address1, address2, city, state, zip, country, birthDate);
        binder = new Binder<>(CardHolder.class);
        StringToDateConverter dateConverter = new StringToDateConverter() {
            @Override
            protected DateFormat getFormat(Locale locale) {
                return new SimpleDateFormat(QI.getQI().getMessage("dateformat"));
            }
        };
        binder.forField(birthDate).withNullRepresentation("").withConverter(dateConverter).bind("birthDate");
        binder.forField(state).bind(c -> c.getState() != null ? c.getState().getName() : "", null);
        binder.forField(country).bind(countryValueProvider(),null);
        binder.bindInstanceFields(this);
        binder.setReadOnly(true);
        setCardHolder(ch);
    }
    
    private ValueProvider countryValueProvider () {
        return (ValueProvider) (o) -> {
            String key = ((CardHolder)o).getCountry();
            return getSysConfigValue("country.", key);
        };
    }
    
    private String getSysConfigValue (String prefix, String key) {
        try {
            return DB.exec(db->{
                SysConfigManager mgr = new SysConfigManager(db, prefix);
                return mgr.get(key);
            });
        } catch (Exception e) {
            QI.getQI().getLog().error(e);
            return e.getMessage();
        }
    }

    public void setCardHolder (CardHolder ch) {
        if (ch != null) {
            this.ch = ch;
            binder.readBean(ch);
        }
    }

    private TextField createField (String propertyId) {
        TextField tf = new TextField(QIUtils.getCaptionFromId("field." + propertyId));
        tf.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        return tf;
    }

}
