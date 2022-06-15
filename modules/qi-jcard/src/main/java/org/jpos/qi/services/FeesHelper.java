package org.jpos.qi.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.jpos.ee.CardProduct;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.Fee;
import org.jpos.ee.FeeManager;
import org.jpos.qi.ViewConfig;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

public class FeesHelper extends QIHelper {

	public FeesHelper(ViewConfig viewConfig) {
        super(Fee.class, viewConfig);
    }
    
    @Override
    public String getItemId(Object item) {
        return String.valueOf(((Fee)item).getId());
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
    
   // @Override
    public Stream getAll() throws Exception {
        List<Fee> items = DB.exec(db -> {
            DBManager<Fee> mgr = new DBManager(db, Fee.class);
            List<Fee> fees = mgr.getAll();
            /*for (Fee fee : fees) {
            	if(fee.getCardProduct()!=null)
            		fee.getCardProduct().getName();
			}*/
            return fees;
        });
        return items.stream();
    }
    
    
    public Stream getAll(int offset, int limit, Map<String, Boolean> orders, CardProduct cp) throws Exception {
        return ((List<Fee>)DB.exec(db -> {
            FeeManager mgr = new FeeManager(db/*,cp*/);
            db.session().enableFetchProfile("eager");
            return mgr.getAll(offset,limit,orders);
        })).stream();
    }

  
    @Override
    public int getItemCount() throws Exception {
        return (int) DB.exec(db -> {
            FeeManager mgr = new FeeManager(db);
            return mgr.getItemCount();
        });
    }
    
    public int getItemCount(CardProduct cp) throws Exception {
        return (int) DB.exec(db -> {
            FeeManager mgr = new FeeManager(db/*,cp*/);
            return mgr.getItemCount();
        });
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
            	  return getAll(offset,limit,orders,cp);
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
        return dataProvider;
    }
    

}