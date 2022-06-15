package org.jpos.qi.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.query.criteria.internal.OrderImpl;
import org.jpos.ee.BLException;
import org.jpos.ee.CardProduct;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.Fee;
import org.jpos.ee.FeeManager;
import org.jpos.ee.Issuer;
import org.jpos.ee.IssuerManager;
import org.jpos.ee.VelocityProfile;
import org.jpos.gl.Account;
import org.jpos.gl.CompositeAccount;
import org.jpos.gl.FinalAccount;
import org.jpos.gl.GLException;
import org.jpos.gl.GLSession;
import org.jpos.qi.QI;
import org.jpos.qi.ViewConfig;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

/**
 * Created by jr on 9/29/15.
 */
public class CardProductHelper extends QIHelper {

    private List<Issuer> issuers;

    public CardProductHelper(ViewConfig viewConfig) {
        super(CardProduct.class, viewConfig);
        issuers = new ArrayList<>();
    }
    
    @Override
    public String getItemId(Object item) {
        return String.valueOf(((CardProduct)item).getId());
    }
    
    public Stream getAllCardProductsByScheme(int offset, int limit, Map<String, Boolean> orders, String scheme)
      throws Exception {
		return DB.exec(db -> {
			CriteriaBuilder criteriaBuilder = db.session().getCriteriaBuilder();
			CriteriaQuery<CardProduct> query = criteriaBuilder.createQuery(CardProduct.class);
			Root<CardProduct> root = query.from(CardProduct.class);
			List<Order> orderList = new ArrayList<>();
			for (Map.Entry<String,Boolean> entry : orders.entrySet()) {
	            OrderImpl order = new OrderImpl(root.get(entry.getKey()),entry.getValue());
	            orderList.add(order);
	        }
	        query.select(root);
	        query.orderBy(orderList);

			if (scheme != null) {
				Predicate notSelf = criteriaBuilder.equal(root.get("scheme"), scheme);
				query.where(notSelf);
			}
			List<CardProduct> list = db.session().createQuery(query).setMaxResults(limit).setFirstResult(offset)
					.getResultList();
			return list;
		}).stream();

	}
	
	public int getCardProductsCountsByScheme(String scheme) throws Exception {
		return DB.exec(db -> {
			CriteriaBuilder criteriaBuilder = db.session().getCriteriaBuilder();
			CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
			Root<CardProduct> root = query.from(CardProduct.class);
			query.select(criteriaBuilder.count(root));
	        
			if (scheme != null) {
				Predicate notSelf = criteriaBuilder.equal(root.get("scheme"), scheme);
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
			String scheme = (String) query.getFilter().orElse(null);
			try {
				return getAllCardProductsByScheme(offset, limit, orders, scheme);
			} catch (Exception e) {
				getApp().getLog().error(e);
				return null;
			}
		}, query -> {
			String scheme = (String) query.getFilter().orElse(null);
			try {
				return getCardProductsCountsByScheme(scheme);
			} catch (Exception e) {
				getApp().getLog().error(e);
				return 0;
			}
		});
		return dataProvider;
	}
    
    public List<Issuer> getIssuers() {
        try {
            return DB.exec((db) -> {
                IssuerManager mgr = new IssuerManager(db);
                return mgr.getAll();
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }
    
    public List<Fee> getFees() {
        try {
            return DB.exec((db) -> {
                FeeManager mgr = new FeeManager(db);
                return mgr.getAll();
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }
    
    public List<VelocityProfile> getVelocityProfiles() {
        try {
            return DB.exec((db) -> {
            	DBManager mgr = new DBManager<VelocityProfile>(db, VelocityProfile.class);
                return mgr.getAll();
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }
    
    public CardProduct saveEntity(CardProduct newCardProduct) {
        try {
            return DB.execWithTransaction(db -> {
                if (newCardProduct !=null) {
                    
                    Account issuedAcct = null;
                    Account feeAcct = null;
                    Account lossesAcct = null;
                    if (newCardProduct.getIssuedAccount() != null)
                        issuedAcct = db.session().get(Account.class, newCardProduct.getIssuedAccount().getId());
                    if (issuedAcct == null) {           // Account entered does not exist
                        //New account.
                        newCardProduct.setIssuedAccount(createNewAssetsAccount(newCardProduct, db));
                    }
                    if (newCardProduct.getFeeAccount() != null)
                        feeAcct = db.session().get(Account.class, newCardProduct.getFeeAccount().getId());
                    if (feeAcct == null) { //Account entered does not exist
                        //New account.
                        newCardProduct.setFeeAccount(createNewEarningsAccount(newCardProduct, db));
                    }
                    if (newCardProduct.getLossesAccount() != null)
                        lossesAcct = db.session().get(Account.class, newCardProduct.getLossesAccount().getId());
                    if (lossesAcct == null) { //Account entered does not exist
                        //New account.
                        newCardProduct.setLossesAccount(createNewLossesAccount(newCardProduct, db));
                    }
                    db.save(newCardProduct);
                    addRevisionCreated(db, getEntityName(), getItemId(newCardProduct));
                    return newCardProduct;
                }
                return newCardProduct;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            getApp().displayNotification(getApp().getMessage("errorMessage.unexpected"));
            return newCardProduct;
        }
    }

    public CardProduct updateEntity(CardProduct cardProduct) throws BLException {
        try {
            return DB.execWithTransaction( (db) -> {
                CardProduct oldProduct = (CardProduct) ((CardProduct) getOriginalEntity()).clone();
                if (cardProduct != null) {
                    Account issuedAcct;
                    Account feeAcct;
                    Account lossesAcct;
                    if (oldProduct.getIssuedAccount() == null && cardProduct != null) {
                        issuedAcct = db.session().get(Account.class, cardProduct.getIssuedAccount().getId());
                        if (issuedAcct == null)     // Account entered does not exist
                            //New account
                            cardProduct.setIssuedAccount(createNewAssetsAccount(cardProduct, db));
                    }
                    if (oldProduct.getFeeAccount() == null && cardProduct.getFeeAccount() != null) {
                        feeAcct = db.session().get(Account.class, cardProduct.getFeeAccount().getId());
                        if (feeAcct == null)        // Account entered does not exist
                            //New account
                            cardProduct.setFeeAccount(createNewEarningsAccount(cardProduct, db));
                    }
                    if (oldProduct.getLossesAccount() == null && cardProduct.getLossesAccount() != null) {
                        lossesAcct = db.session().get(Account.class, cardProduct.getLossesAccount().getId());
                        if (lossesAcct == null)     // Account entered does not exist
                            //New account
                            cardProduct.setLossesAccount(createNewLossesAccount(cardProduct, db));
                    }
                    db.session().merge(cardProduct);
                    addRevisionUpdated(db, getEntityName(),
                            String.valueOf(cardProduct.getId()),
                            oldProduct,
                            cardProduct,
                            new String[]{"id","name", "code", "active","pos","atm","moto","ecommerce" ,"tips","anonymous","startdate"
                                    ,"enddate","issuedAccount","feeAccount", "lossesAccount", "issuer", "externalAccount", "bin",
                                    "binextended","extended","binvirtual","binextendedvirtual","cardnumberlength",
                                    "smart","randomcardnumber"});
                }
                return cardProduct;
            });
        } catch (Exception e) {
            throw new BLException(e.getMessage());
        }
    }
    
    private FinalAccount createNewAssetsAccount (CardProduct newCardProduct, DB db) throws Exception {
        Account root = getIssuersAssetsAccount(newCardProduct.getIssuer());
        return createNewAccount(root, newCardProduct.getIssuedAccount(), newCardProduct.getName(), db);
    }

    private FinalAccount createNewEarningsAccount (CardProduct newCardProduct, DB db) throws Exception {
        Account root = getIssuersEarningsAccount(newCardProduct.getIssuer());
        return createNewAccount(root, newCardProduct.getFeeAccount(), newCardProduct.getName(), db);
    }

    private FinalAccount createNewLossesAccount (CardProduct newCardProduct, DB db) throws Exception {
        Account root = getIssuersLossesAccount(newCardProduct.getIssuer());
        if (root == null)   // some legacy issuers may not have a losses root account
            return null;
        return createNewAccount(root, newCardProduct.getLossesAccount(), newCardProduct.getName(), db);
    }

    private FinalAccount createNewAccount (Account parent, Account newAcct, String description, DB db)
            throws GLException
    {
        newAcct.setCreated(new Date());
        newAcct.setType(parent.getType());
        newAcct.setDescription(description);
        String userNick = QI.getQI().getUser() != null ? QI.getQI().getUser().getNick() : null;
        GLSession gls = new GLSession(db, userNick);
        gls.session().refresh(parent);
        gls.addAccount((CompositeAccount) parent, newAcct);
        return (FinalAccount) newAcct;
    }

    public Account getIssuersAssetsAccount (Issuer issuer) throws Exception {
        return issuer != null ? getRefreshedAccount(issuer.getAssetsAccount()) : null;
    }

    public Account getIssuersEarningsAccount (Issuer issuer) throws Exception {
        return issuer != null ? getRefreshedAccount(issuer.getEarningsAccount()) : null;
    }

    public Account getIssuersLossesAccount (Issuer issuer) throws Exception {
        return issuer != null ? getRefreshedAccount(issuer.getLossesAccount()) : null;
    }

    private Account getRefreshedAccount (Account acct) throws Exception {
        return DB.exec(db-> (acct != null) ? db.session().get(Account.class, acct.getId()) : null);
    }



}
