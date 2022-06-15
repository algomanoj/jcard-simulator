package org.jpos.qi.views.card;
import org.jpos.ee.Card;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

public class CardConverter implements Converter<String, Card> {
    @Override
    public Result<Card> convertToModel(String value, ValueContext context) {
        return Result.ok(null);
    }

    @Override
    public String convertToPresentation(Card value, ValueContext context) {
        return (value == null) ? null : value.getBin() + "... " + value.getLastFour();
    }
}