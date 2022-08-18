package org.jpos.qi.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jpos.ee.Card;
import org.jpos.ee.CardProduct;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.TranLog;
//import org.jpos.ee.TranLogFilter;
//import org.jpos.ee.TranLogManager;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.PosDataCode;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.qi.QICrudFormFactory;
import org.jpos.qi.ViewConfig;
import org.jpos.qi.core.ee.TranLogFilter;
import org.jpos.qi.core.ee.TranLogManager;
import org.jpos.qi.util.DateRange;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

public class TranLogHelper extends QIHelper {

    public TranLogHelper() {
        this(null);
    }

    public TranLogHelper(ViewConfig viewConfig) {
        super(TranLog.class, viewConfig);
    }
    
    @Override
    public Object getEntityById(String param)  {
        try {
            return DB.exec(db -> {
                db.session().enableFetchProfile("eager");
                return (TranLog) db.session().get(clazz, new Long(param));
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }
    
    
    public Set<String> getCurrencies() {
    	Set<String> currencies = null;
    	List<TranLog> tranLogs = null;

        try {
        	tranLogs = DB.exec(db -> {
			    //db.session().enableFetchProfile("eager");
			    DBManager<TranLog> mgr = new DBManager(db, clazz);
			    return mgr.getAll();
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
        if(tranLogs!=null && tranLogs.size()>0) {
        	currencies = tranLogs.stream().filter(tl->tl.getCurrencyCode()!=null).map(tl-> tl.getCurrencyCode()).collect(Collectors.toSet());
        } else {
        	currencies = new HashSet<>();
        }
    	return currencies;
    }

    public Set<CardProduct> getCardProducts() {
    	Set<CardProduct> cardProducts = null;
        try {
        	cardProducts = DB.exec(db -> {
			    //db.session().enableFetchProfile("eager");
			    DBManager<TranLog> mgr = new DBManager(db, clazz);
			    System.out.println("mgr:"+mgr);
			    List<TranLog> tranLogs = mgr.getAll();
			    System.out.println("tranLogs:"+tranLogs.size());
			    if(tranLogs != null && tranLogs.size() > 0) {
				    return tranLogs.stream().filter(tl->tl.getCardProduct()!=null).map(tl-> tl.getCardProduct()).collect(Collectors.toSet());			    	
			    } else {
			    	return null;
			    }
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return cardProducts;

    }
    
    @Override
    public Stream getAll(int offset, int limit, Map<String, Boolean> orders) throws Exception {
        List<TranLog> list = DB.exec(db -> {
            TranLogManager mgr = new TranLogManager(db);
            return mgr.getAll(offset,limit,orders);
        });
        return list.stream();
    }

    public Stream getAll(int offset, int limit, Map<String, Boolean> orders, DateRange filter) throws Exception {
        return getAll (offset, limit, orders, filter, null);
    }


    public Stream getAll(int offset, int limit, Map<String, Boolean> orders, DateRange filter, Card card) throws Exception {
        List<TranLog> list =DB.exec(db -> {
            TranLogManager mgr;
            if (filter != null && card != null) {
                mgr = new TranLogManager(db, filter.getStart(), filter.getEnd(), card);
            } else if (filter != null) {
                mgr = new TranLogManager(db, filter.getStart(), filter.getEnd());
            } else if (card != null) {
                mgr = new TranLogManager(db, null, null, card);
            } else
                mgr = new TranLogManager(db);
            return mgr.getAll(offset,limit,orders);
        });
        return list.stream();
     }
    
    public ConfigurableFilterDataProvider getDataProvider () {
        DataProvider<TranLog, TranLogFilter> dataProvider =
          DataProvider.fromFilteringCallbacks(
            query -> {
                TranLogFilter filter = query.getFilter().orElse(null);
                try {
                    return DB.exec(db -> {
                        Map<String,Boolean> orders = new HashMap<>();
                        for (Object o : query.getSortOrders()) {
                           QuerySortOrder order = (QuerySortOrder) o;
                           orders.put(order.getSorted(), order.getDirection() == SortDirection.DESCENDING);
                        }
                        orders.put("id", false);
                        TranLogManager mgr = filter != null ? new TranLogManager(db, filter) : new TranLogManager(db);
                        return mgr.getAll(query.getOffset(), query.getLimit(), orders).stream();
                    });
                } catch (Exception e) {
                    getApp().getLog().error(e);
                    return null;
                }
            },
            query -> {
                TranLogFilter filter = query.getFilter().orElse(null);
                try {
                    return DB.exec(db -> {
                    	TranLogManager mgr = null;
                    	if(filter != null) {
                    		mgr = new TranLogManager(db, filter);
                    	} else {
                    		mgr = new TranLogManager(db);
                    	}
                    	//TranLogManager mgr = filter != null ? new TranLogManager(db, filter) : new TranLogManager(db);
                        return mgr.getItemCount();
                    });
                } catch (Exception e) {
                    getApp().getLog().error(e);
                    return 0;
                }
            }
            );
        return dataProvider.withConfigurableFilter();
    }

    //not used
    @Override
    public int getItemCount() throws Exception {
        return DB.exec(db -> {
            TranLogManager mgr = new TranLogManager(db);
            return mgr.getItemCount();
        });
    }
    protected String getFriendlyItc (String itc, String reasonCode) {
        String friendly;
        if (reasonCode != null && !"".equals(reasonCode))
            reasonCode = "." + reasonCode;
        try {
            friendly = getApp().getMessage("itc." + itc + reasonCode);
        } catch (MissingResourceException e) {
            try {
                friendly = getApp().getMessage("itc." + itc);
            } catch (MissingResourceException lastExc) {
                friendly = itc;
            }
        }
        return friendly;
    }

    public String getPdcAsString (byte[] pdc) {
        StringBuilder sb = new StringBuilder();
        if (pdc != null) {
            PosDataCode p = PosDataCode.valueOf(pdc);
            if (p.hasReadingMethod(PosDataCode.ReadingMethod.TRACK1_PRESENT) && p.hasReadingMethod(PosDataCode.ReadingMethod.TRACK2_PRESENT)) {
                sb.append("Track 1 and 2");
            } else if (p.hasReadingMethod(PosDataCode.ReadingMethod.TRACK1_PRESENT)) {
                sb.append("Track 1 only");
            } else if (p.hasReadingMethod(PosDataCode.ReadingMethod.TRACK2_PRESENT)) {
                sb.append("Track 2 only");
            } else {
                sb.append("Manual Entry");
            }
        }
        return sb.toString();
    }

    @Override
    public String getItemId(Object item) {
        return String.valueOf(((TranLog)item).getId());
    }

    public String getAuthorizingNetwork (TranLog tranLog) {
        String authorizingNetwork = null;
        ISOMsg ssData = getSsDataAsISOMsg(tranLog);
        if (ssData != null) {
            authorizingNetwork = ssData.getString("112.64");
            if (authorizingNetwork == null) {
                authorizingNetwork = ssData.getString("112.63") != null ?
                  ssData.getString("112.63").substring(11, 14) : null;
            }
        }
        return authorizingNetwork;
    }

    public ISOMsg getSsDataAsISOMsg (TranLog tranLog) {
        if (tranLog != null) {
            ISOMsg ssDataAsISOMsg = null;
            if (tranLog.getSsData() != null && tranLog.getSsData().startsWith("<isomsg")) {
                String ssData = "<isomsg>" + tranLog.getSsData() + "</isomsg>";
                ISOMsg m = new ISOMsg();
                try {
                    ISOPackager packager = new XMLPackager();
                    m.setPackager(packager);
                    m.unpack(ssData.getBytes());
                    ssDataAsISOMsg = m;
                } catch (ISOException ignored) {}
            }
            return ssDataAsISOMsg;
        }
        return null;
    }
    
    public List<TranLog> getTlList(TranLogFilter filter) {
    	try {
    		return DB.exec(db -> {
    			/*
    			TranLogManager mgr = new TranLogManager(db);
    			return mgr.getAll();
		        Map<String,Boolean> orders = new HashMap<>();
		        orders.put("id", false);
    			 */
		        TranLogManager mgr = filter != null ? new TranLogManager(db, filter) : new TranLogManager(db);
		        return mgr.getAll(0, -1, null);
    		});
        } catch (Exception e) {
            getApp().getLog().error(e);
            return new ArrayList<>();
        }
    }
    public List<TranLog> getTlList(DateRange dr) { 
    	List<TranLog> tranLogs = new ArrayList<>();
    	TranLogFilter tranLogFilter =  new TranLogFilter();
    	if(dr!=null) {
        	tranLogFilter.setStart(dr.getStart());
        	tranLogFilter.setEnd(dr.getEnd());
        	tranLogs = getTlList(tranLogFilter);
    	}
    	System.out.println("tranLogs fetched.........");
    	return tranLogs;
    }

    public QICrudFormFactory createCrudFormFactory () {
        QICrudFormFactory factory = new QICrudFormFactory(clazz, getViewConfig(), this);
        factory.setReadOnly(true);
        return factory;
    }

}
