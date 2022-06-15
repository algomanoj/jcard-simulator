package org.jpos.qi.services;

import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import org.jpos.core.Configuration;
import org.jpos.ee.*;
import org.jpos.qi.QI;
import org.jpos.qi.QICrudFormFactory;
import org.jpos.qi.ViewConfig;
import org.jpos.util.BeanDiff;
import org.vaadin.crudui.crud.AddOperationListener;
import org.vaadin.crudui.crud.DeleteOperationListener;
import org.vaadin.crudui.crud.UpdateOperationListener;
import java.util.*;
import java.util.stream.Stream;

public abstract class QIHelper {
    private User user;
    private QI app;
    private String entityName;
    private ViewConfig viewConfig;
    protected Class clazz;
    private Configuration cfg;
    private Object originalEntity;
    private Map<String, List<Validator>> validators = new HashMap<>();

    public QIHelper(Class clazz) {
        this(clazz, null);
    }

    public QIHelper(Class clazz, ViewConfig viewConfig) {
        this.clazz = clazz;
        this.viewConfig = viewConfig;
        cfg = viewConfig != null ? viewConfig.getConfiguration() : null;
        app = QI.getQI();
        user = app.getUser();
    }

    public QICrudFormFactory createCrudFormFactory () {
        return new QICrudFormFactory(clazz, getViewConfig(), this);
    }

    public DataProvider getDataProvider() {
        DataProvider dataProvider = DataProvider.fromCallbacks(
          (CallbackDataProvider.FetchCallback) query -> {
              int offset = query.getOffset();
              int limit = query.getLimit();
              Map<String,Boolean> orders = new LinkedHashMap<>();
              for (Object o : query.getSortOrders()) {
                  QuerySortOrder order = (QuerySortOrder) o;
                  orders.put(order.getSorted(),order.getDirection() == SortDirection.DESCENDING);
              }
              try {
                  return getAll(offset,limit,orders);
              } catch (Exception e) {
                  getApp().getLog().error(e);
                  return null;
              }
          },
          (CallbackDataProvider.CountCallback) query -> {
              try {
                  return getItemCount();
              } catch (Exception e) {
                  getApp().getLog().error(e);
                  return 0;
              }
          });
        return dataProvider.withConfigurableFilter();
    }

    public Stream getAll(int offset, int limit, Map<String, Boolean> orders) throws Exception {
        List items = DB.exec(db -> {
            DBManager mgr = new DBManager(db, clazz);
            return mgr.getAll(offset,limit,orders);
        });
        return items.stream();
    }

    public int getItemCount() throws Exception {
        return DB.exec(db -> {
            DBManager mgr = new DBManager(db, clazz);
            return mgr.getItemCount();
        });
    }

    public Object getEntityById (String id)  {
        //most id are longs, if id is of other type this method must be overridden
        try {
            return  DB.exec(db -> db.session().get(clazz, Long.parseLong(id)));
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }

    public Object saveEntity (Object o) {
        try {
            return DB.execWithTransaction(db -> {
                db.save(o);
                addRevisionCreated(db, getEntityName(), getItemId(o));
                return o;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            getApp().displayNotification(getApp().getMessage("errorMessage.unexpected"));
            return null;
        }
    }

    public Object removeEntity (Object entity) {
        try {
            return DB.execWithTransaction(db -> {
                db.session().delete(entity);
                addRevisionRemoved(db, getEntityName(), getItemId(entity));
                return entity;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            getApp().displayNotification(getApp().getMessage("errorMessage.unexpected"));
            return null;
        }
    }

    public Object updateEntity (Object entity) {
        try {
            return DB.execWithTransaction( (db) -> {
                Object oldObj = getOriginalEntity();
                db.session().merge(entity);
                addRevisionUpdated(db, getEntityName(), getItemId(entity), oldObj, entity, getUpdateProperties());
                return entity;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            getApp().displayNotification(getApp().getMessage("errorMessage.unexpected"));
            return null;
        }
    }

    public AddOperationListener createAddOperationListener () {
        return this::saveEntity;
    }

    public DeleteOperationListener createDeleteOperationListener () {
        return this::removeEntity;
    }

    public UpdateOperationListener createUpdateOperationListener () {
        return this::updateEntity;
    }

    public List<Validator> getValidators(String propertyId) {
        if (getViewConfig() == null)
            return Collections.emptyList();
        List<Validator> validators = new ArrayList<>();
        ViewConfig.FieldConfig config = getViewConfig().getFields().get(propertyId);
        if (config != null) {
            String regex = config.getRegex();
            int length = config.getLength();
            if (regex != null)
                validators.add(
                  new RegexpValidator(
                    getApp().getMessage("errorMessage.invalidField", getApp().getMessage("field." + propertyId)),
                    regex)
                );
            if (length > 0)
                validators.add(new StringLengthValidator(getApp().getMessage("errorMessage.invalidField", getApp().getMessage("field." + propertyId)),0,length));
            if ("email".equals(propertyId)) {
                validators.add(new EmailValidator(getApp().getMessage("errorMessage.invalidEmail")) {
                    @Override
                    protected boolean isValid(String value) {
                        return value == null || value.isEmpty() || super.isValid(value);
                    }
                });
            }
        }

        if (this.validators != null)
            validators.addAll(this.validators.getOrDefault(propertyId, Collections.emptyList()));

        return validators;
    }

    //Must be executed inside a DB.execWithTransaction
    public void addRevisionCreated (DB db, String entity, String id) {
        RevisionManager revMgr = new RevisionManager(db);
        User author = getUser();
        String info = getApp().getMessage("created", entity);
        revMgr.createRevision(author, entity.toLowerCase() + "." + id, info);
    }

    //Must be executed inside a DB.execWithTransaction
    public void addRevisionRemoved (DB db, String entity, String id) {
        RevisionManager revMgr = new RevisionManager(db);
        User author = getUser();
        String info = getApp().getMessage("removed", entity);
        revMgr.createRevision(author, entity.toLowerCase() + "." + id, info);
    }

    public boolean addRevisionUpdated (DB db, String entity, String id, Object oldItem, Object newItem,
                                       String[] itemProps)
    {
        return this.addRevisionUpdated(db, entity, id, oldItem, newItem, itemProps, null);
    }

    //Must be executed inside a DB.execWithTransaction
    public boolean addRevisionUpdated (DB db, String entity, String id, Object oldItem, Object newItem,
                                       String[] itemProps, String extraInfo)
    {
        StringBuilder revInfo = new StringBuilder();
        BeanDiff bd = new BeanDiff (oldItem, newItem, itemProps);
        revInfo.append(bd);
        if (revInfo.length() > 0) {
            User author = getUser();
            StringJoiner info = new StringJoiner(BeanDiff.LINESEP);
            if (extraInfo != null && !extraInfo.isEmpty())
                info.add(extraInfo);
            info.add(revInfo.length() < 1000 ? revInfo.toString() : revInfo.substring(0, 990) + "...");
            RevisionManager revMgr = new RevisionManager(db);
            revMgr.createRevision (author, entity.toLowerCase() + "." + id, info.toString());
            return true;
        } else {
            return false;
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public QI getApp() {
        return app;
    }

    public void setApp(QI app) {
        this.app = app;
    }

    public ViewConfig getViewConfig() {
        return this.viewConfig;
    }

    public String getEntityName() {
        if (entityName == null || entityName.isEmpty()) {
            String name = cfg != null ? cfg.get("entityName", null) : null;
            if (name == null || name.isEmpty()) {
                getApp().displayNotification("view.config.error");
            }
            this.entityName = name;
        }
        return entityName;
    }

    public String[] getUpdateProperties () {
        return getViewConfig().getVisibleFields();
    }

    public Object getOriginalEntity() {
        return originalEntity;
    }

    public void setOriginalEntity(Object originalEntity) {
        this.originalEntity = originalEntity;
    }

    public abstract String getItemId(Object item);
}
