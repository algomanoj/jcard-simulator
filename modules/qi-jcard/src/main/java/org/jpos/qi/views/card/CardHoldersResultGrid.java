package org.jpos.qi.views.card;

import org.jpos.ee.CardHolder;
import org.jpos.qi.services.CardHolderHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.impl.GridCrud;

public class CardHoldersResultGrid extends  QIEntityView<CardHolder>{
	GridCrud<CardHolder> crud;
    public CardHoldersResultGrid () {
    	super(CardHolder.class, "cardholders");
        //addItemClickListener(this::navigateToSpecificView);
    }
    @Override
	public GridCrud<CardHolder> createCrud() {
    	crud = super.createCrud();
    	return crud;
    	
    }
    
    

    @Override
    public QIHelper createHelper() {
    	  
        return new CardHolderHelper(getViewConfig());
    }

    public CardHolderHelper getHelper() {
        return (CardHolderHelper) super.getHelper();
    }

}
