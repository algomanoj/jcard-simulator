package org.jpos.qi.util;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.AbstractStringToNumberConverter;
import com.vaadin.flow.data.converter.Converter;

public class StringToShortConverter  extends AbstractStringToNumberConverter<Short> {

    public StringToShortConverter (String errorMessage) {
    	super(null, errorMessage);
    }

	@Override
	public Result<Short> convertToModel(String value, ValueContext context) {
		Result<Number> n = convertToNumber(value, context);
        return n.flatMap(number -> {
            if (number == null) {
                return Result.ok(null);
            } else {
                Short intValue = number.shortValue();
                return Result.ok(intValue);
            }
        });
	}
}