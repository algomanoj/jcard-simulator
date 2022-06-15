package org.jpos.qi.views.users;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.textfield.EmailField;
import org.jpos.ee.Role;
import org.jpos.ee.User;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.UsersHelper;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.CheckBoxGroupProvider;

public class UsersView extends QIEntityView {

    public UsersView () {
        super(User.class,"users");
    }

    @Override
    public GridCrud createCrud () {
        GridCrud crud = super.createCrud();
        crud.getCrudFormFactory().setFieldType("email", EmailField.class);
        crud.getCrudFormFactory().setFieldProvider("roles", new CheckBoxGroupProvider<>(
          "roles", getHelper().getRoles(), (ItemLabelGenerator<Role>) role -> role != null ? role.getName() : "")
        );
        return crud;
    }

    @Override
    public QIHelper createHelper() {
        return new UsersHelper(getViewConfig());
    }

    public UsersHelper getHelper() {
        return (UsersHelper) super.getHelper();
    }
}
