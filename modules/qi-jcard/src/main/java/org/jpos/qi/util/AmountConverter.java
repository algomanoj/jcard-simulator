package org.jpos.qi.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.flow.data.converter.StringToBigDecimalConverter;

public class AmountConverter extends StringToBigDecimalConverter {

    public AmountConverter(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected NumberFormat getFormat(Locale locale) {
        NumberFormat amountFormat = NumberFormat.getInstance();
        amountFormat.setGroupingUsed(true);
        amountFormat.setMinimumFractionDigits(2);
        amountFormat.setMaximumFractionDigits(7);
        if (amountFormat instanceof DecimalFormat) {
            ((DecimalFormat) amountFormat).setParseBigDecimal(true);
        }
        return amountFormat;
    }

}
