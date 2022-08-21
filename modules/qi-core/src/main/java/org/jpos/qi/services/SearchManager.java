package org.jpos.qi.services;

import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.jpos.ee.Card;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;

public class SearchManager<T> extends DBManager<T>{

	public SearchManager(DB db, Class<T> clazz) {
		super(db, clazz);
	}
    public Card getRandomCard (String token, String cardProductName, String scheme, String realId) throws HibernateException {
        Card card = null;

    	String queryStr = "from org.jpos.ee.Card as card ";
    	if(isNotEmpty(token) || isNotEmpty(cardProductName) || isNotEmpty(scheme) || isNotEmpty(realId) ) {
    		queryStr = queryStr + " where ";
    		if(isNotEmpty(token) ) {
    			queryStr = queryStr + " card.token =:token ";
    		}
    		if(isNotEmpty(cardProductName) ) {
    			queryStr = queryStr + " card.cardProduct.name =:cardProductName ";
    		}
    		if(isNotEmpty(scheme) ) {
    			queryStr = queryStr + " card.cardProduct.scheme =:scheme ";
    		}
    		if(isNotEmpty(realId) ) {
    			queryStr = queryStr + " card.cardHolder.realId =:realId ";
    		}
    	}
        Query query  = db.session().createQuery (queryStr);
    	if(isNotEmpty(token) || isNotEmpty(cardProductName) || isNotEmpty(scheme) || isNotEmpty(realId) ) {
    		if(isNotEmpty(token) ) {
                query.setParameter ("token", token);
    		}
    		if(isNotEmpty(cardProductName) ) {
                query.setParameter ("cardProductName", cardProductName);
    		}
    		if(isNotEmpty(scheme) ) {
                query.setParameter ("scheme", scheme);
    		}
    		if(isNotEmpty(realId) ) {
                query.setParameter ("realId", realId);
    		}
    	}
        Iterator iter = query.list().iterator();
        if (iter.hasNext())
            card = (Card) iter.next();
    
        return card;
    }

    boolean isNotEmpty(String str) {
    	return str!=null && str.trim().length()>0;
    }
}
