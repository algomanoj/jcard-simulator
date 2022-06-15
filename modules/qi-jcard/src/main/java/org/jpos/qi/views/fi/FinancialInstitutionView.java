package org.jpos.qi.views.fi;

import org.jpos.ee.FinancialInstitution;
import org.jpos.qi.services.FinancialInstitutionHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.impl.GridCrud;

public class FinancialInstitutionView extends QIEntityView<FinancialInstitution> {

    public FinancialInstitutionView() {
        super(FinancialInstitution.class,"financial_institutions");
    }

    @Override
    public GridCrud createCrud () {
        GridCrud crud = super.createCrud();
        return crud;
    }

    @Override
    public QIHelper createHelper() {
        return new FinancialInstitutionHelper(getViewConfig());
    }

    public FinancialInstitutionHelper getHelper() {
        return (FinancialInstitutionHelper) super.getHelper();
    }
}
