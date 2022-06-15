package org.jpos.qi.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jpos.ee.BLException;
import org.jpos.ee.CardProduct;
import org.jpos.ee.CardProductManager;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.VelocityManager;
import org.jpos.ee.VelocityProfile;
import org.jpos.qi.ViewConfig;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

public class VelocityProfileHelper extends QIHelper {

    public VelocityProfileHelper(ViewConfig viewConfig) {
        super(VelocityProfile.class, viewConfig);
    }
    
    
    public DataProvider getDataProvider() {
        DataProvider dataProvider = DataProvider.fromFilteringCallbacks(
          query -> {
              int offset = query.getOffset();
              int limit = query.getLimit();
              Iterator it = query.getSortOrders().iterator();
              Map<String,Boolean> orders = new LinkedHashMap<>();
              while (it.hasNext()) {
                  QuerySortOrder order = (QuerySortOrder) it.next();
                  orders.put(order.getSorted(),order.getDirection() == SortDirection.DESCENDING);
              }
             CardProduct cp = (CardProduct) query.getFilter().orElse(null);
              try {
            	  if(cp != null) 
            		  return getAll(offset,limit,orders,cp);
            	  return getAll(offset,limit,orders);
              } catch (Exception e) {
                  getApp().getLog().error(e);
                  return null;
              }
          },
          query -> {
        	  CardProduct cp = (CardProduct) query.getFilter().orElse(null);
              try {
                  return getItemCount(cp);
              } catch (Exception e) {
                  getApp().getLog().error(e);
                  return 0;
              }
          });
        return  dataProvider;
    }
    
    public Stream getAll(int offset, int limit, Map<String, Boolean> orders, CardProduct cp) throws Exception {
        return ((List<VelocityProfile>)DB.exec(db -> {
			VelocityManager mgr = new VelocityManager(db/* ,cp */); 
            db.session().enableFetchProfile("eager");
            return mgr.getAll(offset,limit,orders);
        })).stream();
    }
    
    public List<VelocityProfile> getfilteredVelocities(int offset, int limit, Map<String, Boolean> orders, CardProduct cp) throws Exception {
        return ((List<VelocityProfile>)DB.exec(db -> {
			VelocityManager mgr = new VelocityManager(db/* ,cp */); 
            db.session().enableFetchProfile("eager");
            return mgr.getAll(offset,limit,orders);
        }));
    }
    public int getItemCount(CardProduct cp) throws Exception {
        return (int) DB.exec(db -> {
			VelocityManager mgr = new VelocityManager(db/* ,cp */);
            return mgr.getItemCount();
        });
    }
    
    @Override
    public String getItemId(Object item) {
        return String.valueOf(((VelocityProfile)item).getId());
    }

    public List<CardProduct> getCardProducts() {
        try {
        	return  DB.exec(db -> {
			    return new DBManager(db, CardProduct.class).getAll();
			});
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<CardProduct>();
		}
    }
    
    public VelocityProfile saveEntity(VelocityProfile newVP) throws BLException {
        try {
            return (VelocityProfile) DB.execWithTransaction(db -> {
                if (newVP != null) {
                    //CardProductManager cpManager = new CardProductManager(db);
                    //CardProduct cp = cpManager.getItemByParam("id",newVP.getCardProduct().getId(),true);
                    //cp.addVelocityProfile(newVP);
                    //db.save(cp);
                	db.save(newVP);
                    addRevisionCreated(db, getEntityName(), getItemId(newVP));
                    return newVP;
                }
                return newVP;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            getApp().displayNotification(getApp().getMessage("errorMessage.unexpected"));
            return newVP;
        }
    }


    public VelocityProfile updateEntity(VelocityProfile newVP) throws BLException {
            try {
                return (VelocityProfile) DB.execWithTransaction( (db) -> {
                    VelocityProfile oldVP = (VelocityProfile) ((VelocityProfile) getOriginalEntity()).clone();
                    db.session().merge(newVP);
                    addRevisionUpdated(db, getEntityName(),
                            String.valueOf(newVP.getId()),
                            oldVP,
                            newVP,
                            new String[]{"id","name","active","approvalsOnly","scopeCard","scopeAccount","validOnPurchase",
                                    "validOnWithdrawal","validOnTransfer","validOnCredit","scopeDaily","scopeMonthly",
                            "currencyCode","numberOfDays","usageLimit","amountLimit"/*,"cardproduct"*/});
                    return newVP;
                });
            } catch (Exception e) {
                throw new BLException(e.getMessage());
            }
    }

}
