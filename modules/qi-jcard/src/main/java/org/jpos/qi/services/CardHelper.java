package org.jpos.qi.services;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.Restrictions;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.jpos.ee.BLException;
import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.CardManager;
import org.jpos.ee.CardProduct;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.iso.ISOUtil;
import org.jpos.qi.ViewConfig;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKeyStore;
import org.jpos.security.jceadapter.SSM;
import org.jpos.util.NameRegistrar;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

public class CardHelper extends QIHelper {

	private static Random random = new SecureRandom();
	 
	public CardHelper(ViewConfig viewConfig) {
		super(Card.class, viewConfig);
	}

	@Override
	public String getItemId(Object item) {
		return String.valueOf(((Card) item).getId());
	}

	public List<CardProduct> getCardProducts() {
		try {
			return DB.exec(db -> {
				return new DBManager(db, CardProduct.class).getAll();
			});
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<CardProduct>();
		}
	}

	public Stream getAllCardByCardProduct(int offset, int limit, Map<String, Boolean> orders, CardProduct cardProduct)
			throws Exception {
		return ((List<Card>) DB.exec(db -> {
			db.session().enableFetchProfile("qi_card_product_holders_account_view");
			CriteriaBuilder criteriaBuilder = db.session().getCriteriaBuilder();
			CriteriaQuery<Card> query = criteriaBuilder.createQuery(Card.class);
			Root<Card> root = query.from(Card.class);
			List<Order> orderList = new ArrayList<>();
			for (Map.Entry<String, Boolean> entry : orders.entrySet()) {
				OrderImpl order = new OrderImpl(root.get(entry.getKey()), entry.getValue());
				orderList.add(order);
			}
			query.select(root);
			query.orderBy(orderList);

			if (cardProduct != null) {
				Predicate notSelf = criteriaBuilder.equal(root.get("cardProduct"), cardProduct);
				query.where(notSelf);
			}
			List<Card> list = db.session().createQuery(query).setMaxResults(limit).setFirstResult(offset).getResultList();
			return list;
		})).stream();

	}

	public int getCardCountsByCardProduct(CardProduct cardProduct) throws Exception {
		return DB.exec(db -> {
			CriteriaBuilder criteriaBuilder = db.session().getCriteriaBuilder();
			CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
			Root<Card> root = query.from(Card.class);
			query.select(criteriaBuilder.count(root));

			if (cardProduct != null) {
				Predicate notSelf = criteriaBuilder.equal(root.get("cardProduct"), cardProduct);
				query.where(notSelf);
			}
			return db.session().createQuery(query).getSingleResult().intValue();
		});
	}

	public DataProvider getDataProvider() {
		DataProvider dataProvider = DataProvider.fromFilteringCallbacks(query -> {
			int offset = query.getOffset();
			int limit = query.getLimit();
			Iterator it = query.getSortOrders().iterator();
			Map<String, Boolean> orders = new LinkedHashMap<>();
			while (it.hasNext()) {
				QuerySortOrder order = (QuerySortOrder) it.next();
				orders.put(order.getSorted(), order.getDirection() == SortDirection.DESCENDING);
			}
			CardProduct cardProduct = (CardProduct) query.getFilter().orElse(null);
			try {
				return getAllCardByCardProduct(offset, limit, orders, cardProduct);
			} catch (Exception e) {
				getApp().getLog().error(e);
				return null;
			}
		}, query -> {
			CardProduct cardProduct = (CardProduct) query.getFilter().orElse(null);
			try {
				return getCardCountsByCardProduct(cardProduct);
			} catch (Exception e) {
				getApp().getLog().error(e);
				return 0;
			}
		});
		return dataProvider;
	}
	
    public Card saveEntity(Card card) throws BLException {
        try {

            SecureKeyStore ks = (SecureKeyStore) NameRegistrar.get ("ks");
            SSM ssm = NameRegistrar.get("ssm");    
            SecureDESKey currentBDK = ks.getKey ("bdk.001");
            return DB.execWithTransaction(db -> { 
                	CardManager cardManager = new CardManager(db);
                	int minRange = 000000000;
            		int maxRange = 999999999;
                	String bin = card.getCardProduct().getBin();
                	String accNo = ISOUtil.zeropad(
            				Integer.toString((Math.abs(random.nextInt(maxRange - minRange) + 1) + minRange % 1000000000)), 9);
            		String pan = bin + accNo;
            		card.setStartDate(new Date());
            		//ServiceCode TBD 
            		//prev it is set from card->card Product -> brand -> service code 
            		// currently scheme we hav replace for Brand so if its length more than 3, we get track1 length greater than 37 and InvalidCardException
            		//as of now substring scheme value from sysconfig to 3 char only.
            		//String scheme=card.getCardProduct().getScheme();
            		//String serviceCode=scheme != null ? scheme.substring(0, 2) :"";
            		String serviceCode="101";
            		Card newCard = cardManager.createCard(card.getCardHolder(), pan, card.getStartDate(), card.getEndDate(), serviceCode, ssm, currentBDK, "bdk.001");
                 	newCard.setCardProduct(card.getCardProduct());
                 	newCard.setCardHolder(card.getCardHolder());
                 	newCard.setState(card.getState());
                 	newCard.setVirtual(card.isVirtual());
                 	newCard.setAccount(card.getAccount());
                 	//newCard.setEmbossingName(card.getEmbossingName());
                 	//newCard.setCompanyName(card.getCompanyName());
                    db.save(newCard);
                    return newCard;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            getApp().displayNotification(getApp().getMessage("errorMessage.unexpected"));
            return null;
        }
    }
    
    
    public List<CardHolder> getAllCardHolders() {
        try {
            return (List<CardHolder>) DB.exec(db -> {
            	db.session().enableFetchProfile("qi_cardholders_accounts_view");
            	return db.session()
                .createCriteria(CardHolder.class)
                .add(Restrictions.gt("id", 0l))
                .setMaxResults(10000)
                .setReadOnly(false)
                .setCacheable(false)
                .addOrder(org.hibernate.criterion.Order.asc("id"))
                .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
                .list();
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }
	

	public Card updateEntity(Card card) throws BLException {
		try {
			return DB.execWithTransaction((db) -> {
				db.session().merge(card);
				return card;
			});
		} catch (Exception e) {
			throw new BLException(e.getMessage());
		}
	}

}
