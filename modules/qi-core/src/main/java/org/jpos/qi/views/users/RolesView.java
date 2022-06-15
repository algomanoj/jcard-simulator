package org.jpos.qi.views.users;

import com.vaadin.flow.component.ItemLabelGenerator;
import org.jpos.ee.Permission;
import org.jpos.ee.Role;
import org.jpos.ee.SysConfig;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.RolesHelper;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.CheckBoxGroupProvider;

public class RolesView extends QIEntityView {

    public RolesView() {
        super(Role.class,"roles");
    }

    @Override
    public QIHelper createHelper() {
        return new RolesHelper(getViewConfig());
    }

    @Override
    public GridCrud createCrud () {
        GridCrud crud = super.createCrud();
        crud.getCrudFormFactory().setFieldProvider("permissions", new CheckBoxGroupProvider<>(
          "permissions", getHelper().getPermissions(),
          (ItemLabelGenerator<Permission>) permission -> permission != null ? permission.getName() : "")
        );
        return crud;
    }

    public RolesHelper getHelper() {
        return (RolesHelper) super.getHelper();
    }
}
