package org.jpos.qi.services;

import static org.jpos.ee.Constants.TRAN_APPROVED;
import static org.jpos.ee.Constants.TRAN_PARTIALLY_APPROVED;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.jpos.ee.Card;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.TranLog;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKeyStore;
import org.jpos.security.SecureKeyStore.SecureKeyStoreException;
import org.jpos.security.jceadapter.SSM;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

public class SearchManager<T> extends DBManager<T>{
	
	Random random = new Random();

	public SearchManager(DB db, Class<T> clazz) {
		super(db, clazz);
	}
    public Card getRandomCard (String token, String cardProductName, String scheme, String realId) throws Exception {
        Card card = null;

        Criteria critCard = db.session().createCriteria(Card.class);
		if (isNotEmpty(token)) {
			critCard.add(Restrictions.eq("token", token));
		}

		if(isNotEmpty(scheme) || isNotEmpty(cardProductName)) {
			Criteria critCP = critCard.createCriteria("cardProduct");
			if(isNotEmpty(cardProductName))
				critCP.add(Restrictions.eq("name", cardProductName.toLowerCase()).ignoreCase());
			if(isNotEmpty(scheme))
				critCP.add(Restrictions.eq("scheme", scheme.toLowerCase()).ignoreCase());
		}
        
        if(isNotEmpty(realId)) {
        	Criteria critCH = critCard.createCriteria("cardHolder");
        	critCH.add(Restrictions.eq("realId", realId));
        }
        critCard.setProjection(Projections.rowCount());
        List<Long> results = critCard.list();
        int totalRecord = ((Long)results.get(0)).intValue();        
        
        critCard.setProjection(null);
        critCard.setFirstResult(random.nextInt(totalRecord));
        critCard.setMaxResults(1);
        card = (Card)critCard.uniqueResult();
        
		// descrypt and get the pan
		Map secureMap = getCardSecureMap(card);
		card.setPan((String) secureMap.get("P"));

        
		/*
		 * 
		 * String queryStr = "from org.jpos.ee.Card as card "; if(isNotEmpty(token) ||
		 * isNotEmpty(cardProductName) || isNotEmpty(scheme) || isNotEmpty(realId) ) {
		 * queryStr = queryStr + " where "; if(isNotEmpty(token) ) { queryStr = queryStr
		 * + " card.token =:token "; } if(isNotEmpty(cardProductName) ) { queryStr =
		 * queryStr + " card.cardProduct.name =:cardProductName "; }
		 * if(isNotEmpty(scheme) ) { queryStr = queryStr +
		 * " card.cardProduct.scheme =:scheme "; } if(isNotEmpty(realId) ) { queryStr =
		 * queryStr + " card.cardHolder.realId =:realId "; } } Query query =
		 * db.session().createQuery (queryStr); if (isNotEmpty(token)) {
		 * query.setParameter("token", token); } if (isNotEmpty(cardProductName)) {
		 * query.setParameter("cardProductName", cardProductName); } if
		 * (isNotEmpty(scheme)) { query.setParameter("scheme", scheme); } if
		 * (isNotEmpty(realId)) { query.setParameter("realId", realId); }
		 * 
		 * card = (Card)query.uniqueResult();
		 */
        return card;
    }

	boolean isNotEmpty(String str) {
		return str != null && !str.isBlank();
	}
	
	private SSM getSSM() throws NameRegistrar.NotFoundException {
		return (SSM) NameRegistrar.get("ssm");
	}

	protected SecureDESKey getBDK(String bdkName) throws SMException, SecureKeyStoreException {
		try {
			SecureKeyStore ks = NameRegistrar.get("ks");
			return ks.getKey(bdkName);
		} catch (NotFoundException e) {
			throw new SMException(e.getMessage());
		}
	}

	protected Map getCardSecureMap(Card card) throws Exception {
		Map secureMap = card.getSecureMap();
		if (secureMap == null) {
			secureMap = getSSM().customDecryptMap(getBDK(card.getKid()), card.getSecureData());
			card.setSecureMap(secureMap);
		}
		return secureMap;
	}
	
}
