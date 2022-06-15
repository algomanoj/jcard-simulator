package org.jpos.qi;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.*;
import org.apache.commons.lang3.StringUtils;
import org.jpos.ee.Cloneable;
import org.jpos.qi.services.QIHelper;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.form.CrudFormConfiguration;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.data.converter.StringToByteConverter;
import org.vaadin.data.converter.StringToCharacterConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.jpos.qi.QIUtils.getCaptionFromId;

public class QICrudFormFactory<T> extends DefaultCrudFormFactory<T> {
    public static String AMOUNT_PATTERN = "[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?";
    public static String CURRENCY_PATTERN = "^\\d{1,5}";
    public static String TEXT_PATTERN = "^[\\w\\s.,\\-\\']*$";
    public static String TEXT_EXTENDED_PATTERN = "^[\\w\\s.,\\-\'():]*$";
    public static String WORD_PATTERN = "^[\\w.\\-]*$";
    public static String ACCT_CODE_PATTERN = "^[\\w.]*$";
    public static String SYSCONFIG_ID_PATTERN = "^[\\w\\s.\\-\\/\\?\\=\\:]{0,255}$";

    private QI app;
    private ViewConfig viewConfig;
    private Map<String, List<Validator>> validators;
    private QIHelper helper;
    private boolean readOnly;

    public QICrudFormFactory (Class<T> domainType, ViewConfig viewConfig, QIHelper helper) {
        super(domainType);
        this.viewConfig = viewConfig;
        this.helper = helper;
        setVisibleProperties(viewConfig.getVisibleFields());
        setDisabledProperties(CrudOperation.UPDATE, viewConfig.getReadOnlyFields());
    }

    public QICrudFormFactory (Class<T> domainType, ViewConfig viewConfig, Map<String, List<Validator>> validators,
                              QIHelper helper) {
        this(domainType, viewConfig, helper);
        this.validators = validators;
    }

    protected void bindField(HasValue field, String property, Class<?> propertyType, CrudFormConfiguration configuration) {
        Binder.BindingBuilder bindingBuilder = formatField(property, field);
        if (TextField.class.isAssignableFrom(field.getClass()) || PasswordField.class.isAssignableFrom(field.getClass()) || TextArea.class.isAssignableFrom(field.getClass())) {
            bindingBuilder = bindingBuilder.withNullRepresentation("");
        }
        if (configuration.getConverters().containsKey(property)) {
            bindingBuilder = bindingBuilder.withConverter(configuration.getConverters().get(property));
        } else if (!Double.class.isAssignableFrom(propertyType) && !Double.TYPE.isAssignableFrom(propertyType)) {
            if (!Long.class.isAssignableFrom(propertyType) && !Long.TYPE.isAssignableFrom(propertyType)) {
                if (BigDecimal.class.isAssignableFrom(propertyType)) {
                    bindingBuilder = bindingBuilder.withConverter(new StringToBigDecimalConverter((BigDecimal)null, "Must be a number"));
                } else if (BigInteger.class.isAssignableFrom(propertyType)) {
                    bindingBuilder = bindingBuilder.withConverter(new StringToBigIntegerConverter((BigInteger)null, "Must be a number"));
                } else if (!Integer.class.isAssignableFrom(propertyType) && !Integer.TYPE.isAssignableFrom(propertyType)) {
                    if (!Byte.class.isAssignableFrom(propertyType) && !Byte.TYPE.isAssignableFrom(propertyType)) {
                        if (!Character.class.isAssignableFrom(propertyType) && !Character.TYPE.isAssignableFrom(propertyType)) {
                            if (!Float.class.isAssignableFrom(propertyType) && !Float.TYPE.isAssignableFrom(propertyType)) {
                                if (Date.class.isAssignableFrom(propertyType)) {
                                    bindingBuilder = bindingBuilder.withConverter(new LocalDateToDateConverter());
                                }
                            } else {
                                bindingBuilder = bindingBuilder.withConverter(new StringToFloatConverter((Float)null, "Must be a number"));
                            }
                        } else {
                            bindingBuilder = bindingBuilder.withConverter(new StringToCharacterConverter());
                        }
                    } else {
                        bindingBuilder = bindingBuilder.withConverter(new StringToByteConverter((Byte)null, "Must be a number"));
                    }
                } else {
                    bindingBuilder = bindingBuilder.withConverter(new StringToIntegerConverter((Integer)null, "Must be a number"));
                }
            } else {
                bindingBuilder = bindingBuilder.withConverter(new StringToLongConverter((Long)null, "Must be a number"));
            }
        } else {
            bindingBuilder = bindingBuilder.withConverter(new StringToDoubleConverter((Double)null, "Must be a number"));
        }
        bindingBuilder.bind(property);
    }

    public Binder.BindingBuilder formatField(String id, HasValue field) {
        Binder.BindingBuilder builder = getBinder().forField(field);
        if (viewConfig == null)
            return builder;
        List<Validator> v = helper.getValidators(id);
        for (Validator val : v)
            builder.withValidator(val);
        if (isRequired(id))
            builder.asRequired(getApp().getMessage("errorMessage.req", StringUtils.capitalize(getCaptionFromId("field."+id))));
        ViewConfig.FieldConfig config = viewConfig.getFields().get(id);
        String width = config != null ? config.getWidth() : null;
        if (field instanceof HasSize)
            ((HasSize)field).setWidth(width);
        return builder;
    }

    @Override
    protected Button buildOperationButton(CrudOperation operation, T domainObject, ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (clickListener == null) {
            return null;
        } else if (isReadOnly()) {
            setCancelButtonCaption(getApp().getMessage("close"));
            return null;
        } else {
            String caption = this.buttonCaptions.get(operation);
            Icon icon = this.buttonIcons.get(operation);
            Button button = icon != null ? new Button(caption, icon) : new Button(caption);
            if (this.buttonStyleNames.containsKey(operation)) {
                ((Set)this.buttonStyleNames.get(operation))
                  .stream()
                  .filter(Objects::nonNull)
                  .forEach((styleName) -> button.addClassName((String) styleName));
            }
            if (this.buttonThemes.containsKey(operation))
                button.getElement().setAttribute("theme", this.buttonThemes.get(operation));

            button.addClickListener((event) -> {
                if (CrudOperation.UPDATE.equals(operation)) {
                    try {
                        helper.setOriginalEntity(((Cloneable)domainObject).clone());
                    } catch (CloneNotSupportedException e) {
                        getApp().getLog().error(e);
                        this.showError(operation, e);
                    }
                }
                if (this.binder.writeBeanIfValid(domainObject)) {
                    try {
                        clickListener.onComponentEvent(event);
                    } catch (Exception var6) {
                        this.showError(operation, var6);
                    }
                } else {
                    Notification.show(this.validationErrorMessage);
                }
            });
            return button;
        }
    }

    @Override
    protected List<HasValueAndElement> buildFields(CrudOperation operation, T domainObject, boolean readOnly) {
        return super.buildFields(operation, domainObject, isReadOnly());
    }

    public boolean isRequired(String propertyId) {
        return viewConfig != null && viewConfig.getFields().containsKey(propertyId) &&
          viewConfig.getFields().get(propertyId).isRequired();
    }

    public Binder<T> getBinder() {
        return this.binder;
    }

    public QI getApp() {
        if (app == null)
            app = QI.getQI();
        return app;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
