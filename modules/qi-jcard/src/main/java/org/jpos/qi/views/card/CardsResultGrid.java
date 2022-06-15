package org.jpos.qi.views.card;

import static org.jpos.qi.QIUtils.getCaptionFromId;

import java.util.Set;

import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.CardProduct;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.grid.Grid;

public class CardsResultGrid  extends Grid<Card>{
	GridCrud<Card> crud;
    public CardsResultGrid (Set<Card> cards) {
    	super();
    	 setWidth("100%");
    	 setGridGetters () ;
         setSelectionMode(Grid.SelectionMode.SINGLE);
         setColumnReorderingAllowed(true);
         setItems(cards);
         
    }
    
    public void setGridGetters () {
        addColumn(Card::getId)
          .setHeader(getCaptionFromId("column.id"))
          .setId("id");
        
        addColumn(Card::getToken)
        .setHeader(getCaptionFromId("column.token"))
        .setId("token");
       
        addColumn(card ->
        card != null ? card.getLastFour() : "")
        .setHeader(getCaptionFromId("column.lastFour"))
        .setId("lastFour");
        
        addColumn(card -> {
        	CardProduct cp=card.getCardProduct();
        	return cp.getBin() != null ? cp.getBin() : "";
        })
        .setHeader(getCaptionFromId("column.bin"))
        .setId("bin");
        
        addColumn(card -> {
        	CardHolder ch=card.getCardHolder();
        	return ch.getFirstName() != null ? ch.getFirstName() : "";
        })
        .setHeader(getCaptionFromId("column.cardholder"))
        .setId("cardholder");
        
        addColumn(Card::getEndDate)
        .setHeader(getCaptionFromId("column.enddate"))
        .setId("enddate");
        
        addColumn(Card::isVirtual)
        .setHeader(getCaptionFromId("column.virtual"))
        .setId("virtual");
        addColumn(card ->{
        	return card.getState() != null ? card.getState().name() : "";
        })
        .setHeader(getCaptionFromId("column.state"))
        .setId("state");
        
    }

    

}
