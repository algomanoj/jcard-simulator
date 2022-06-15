package org.jpos.qi.services;

import org.jpos.ee.FinancialInstitution;
import org.jpos.qi.ViewConfig;

public class FinancialInstitutionHelper extends QIHelper {

    public FinancialInstitutionHelper(ViewConfig viewConfig) {
        super(FinancialInstitution.class, viewConfig);
    }
    
    @Override
    public String getItemId(Object item) {
        return String.valueOf(((FinancialInstitution)item).getId());
    }

}
