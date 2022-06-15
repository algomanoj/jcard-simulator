package org.jpos.qi.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.*;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.XmlConfigurable;
import org.jpos.qi.*;
import org.jpos.qi.services.QIHelper;
import org.vaadin.crudui.crud.LazyCrudListener;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import java.util.Optional;

public abstract class QIEntityView<T> extends Composite<Component>
  implements BeforeEnterObserver, AfterNavigationObserver, Configurable, XmlConfigurable {
    private QI app;
    private Class<T> clazz;
    private String name;
    private String title;
    private QIHelper helper;
    private String entityId;
    private T bean;
    private Configuration cfg;
    private ViewConfig viewConfig;
    private GridCrud<T> crud;

    public QIEntityView (Class clazz, String name) {
        super();
        app = QI.getQI();
        this.setViewConfig(app.getView(name));
        this.helper = createHelper();
        this.clazz = clazz;
    }

    @Override
    public Component initContent() {
        VerticalLayout vl = new VerticalLayout();
        vl.setHeightFull();
        H2 viewTitle = new H2(app.getMessage(name));
        viewTitle.addClassNames("mt-s", "text-l");
        crud = createCrud();
        vl.add(viewTitle, crud);
        if (entityId != null)
            bean = (T) getHelper().getEntityById(entityId);
        if (bean != null)
            crud.getGrid().select(bean);
        return vl;
    }

    public GridCrud createCrud () {
        GridCrud<T> crud;
        if (getViewConfig().isHorizontalSplitLayout()) {
            HorizontalSplitCrudLayout crudLayout = new HorizontalSplitCrudLayout();
            crudLayout.addToolbarComponent(new HorizontalLayout());
            crud = new GridCrud(clazz, crudLayout, helper.createCrudFormFactory());
        } else {
            crud = new GridCrud(clazz, helper.createCrudFormFactory());
        }
        boolean canEdit = hasWritePerm();
        crud.setAddOperationVisible(canEdit);
        crud.setUpdateOperationVisible(false);
        crud.setDeleteOperationVisible(canEdit);
        crud.setClickRowToUpdate(canEdit);
        if (getViewConfig() != null)
            crud.getGrid().setColumns(getViewConfig().getVisibleColumns());
        for (Grid.Column c : crud.getGrid().getColumns())
            c.setHeader(QIUtils.getCaptionFromId(c.getKey()));
        crud.getGrid().addSelectionListener((SelectionListener<Grid<T>, T>) selectionEvent -> {
            UI ui = UI.getCurrent();
            ui.getPage().fetchCurrentURL(currentUrl -> {
                Optional optional = selectionEvent.getFirstSelectedItem();
                if (optional.isPresent()) {
                    ui.getPage().getHistory().pushState(null, getGeneralRoute() + "/" + getHelper().getItemId(optional.get()));
                } else {
                    ui.getPage().getHistory().pushState(null, getGeneralRoute());
                }
            });
        });
        crud.setCrudListener(new LazyCrudListener<T>() {
            @Override
            public DataProvider<T, ?> getDataProvider() {
                return helper.getDataProvider();
            }

            @Override
            public T add(T entity) {
                return (T) helper.saveEntity(entity);
            }

            @Override
            public T update(T entity) {
                return (T) helper.updateEntity(entity);
            }

            @Override
            public void delete(T entity) {
                helper.removeEntity(entity);
            }
        });
        crud.setShowNotifications(true);
        crud.getGrid().addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        return crud;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> id = event.getRouteParameters().get("id");
        entityId = id.isPresent() && id != null ? id.get() : null;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        if (entityId != null) {
            bean = (T) getHelper().getEntityById(entityId);
            if (bean != null) {
                getGrid().select(bean);
            } else {
                // TO-DO: Display "<entity> not found".
            }
        }

    }

    public abstract QIHelper createHelper();

    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
    }

    public void setConfiguration (Element element) {
        Attribute routeAttribute = element.getAttribute("route");
        name = routeAttribute != null ? routeAttribute.getValue() : name;
//        generalRoute = routeAttribute != null ? "/" + routeAttribute.getValue() : generalRoute;
        if (name != null && app.getView(name)!= null)  {
//            this.setViewConfig(QI.getQI().getView(name));
            this.title = "<strong>" + app.getMessage(name) + "</strong>";
//            this.visibleColumns = getViewConfig().getVisibleColumns();
//            this.visibleFields = getViewConfig().getVisibleFields();
//            this.readOnlyFields = getViewConfig().getReadOnlyFields();
        }
    }

    public QICrudFormFactory getCrudFormFactory () {
        return (QICrudFormFactory) getCrud().getCrudFormFactory();
    }

    private boolean isGeneralRoute (String currentPath) {
        return getGeneralRoute().equals(currentPath);
    }

    public String getGeneralRoute() {
        return "/" + getName();
    }

    public QIHelper getHelper() {
        return helper;
    }

    public void setHelper(QIHelper helper) {
        this.helper = helper;
    }

    public QI getApp() {
        return app;
    }

    public void setApp(QI app) {
        this.app = app;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ViewConfig getViewConfig() {
        return viewConfig;
    }

    public void setViewConfig(ViewConfig viewConfig) {
        this.viewConfig = viewConfig;
        setConfiguration(getViewConfig().getXmlElement());
        setConfiguration(getViewConfig().getConfiguration());
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    public GridCrud<T> getCrud() {
        return crud;
    }

    public void setCrud(GridCrud<T> crud) {
        this.crud = crud;
    }

    public Grid<T> getGrid () {
        return getCrud().getGrid();
    }

    // Check if User has write permission defined as write-perm in 00_qi for the view.
    // If write-perm is not defined or empty default to true.
    public boolean hasWritePerm () {
        String writePerm = getViewConfig() != null ? getViewConfig().getWritePerm() : "";
        if (writePerm != null && !writePerm.isEmpty())
            return getApp().getUser().hasPermission(writePerm);
        else
            return true;
    }

    public boolean isHorizontalSplitLayout ()  {
        return getViewConfig() != null && getViewConfig().isHorizontalSplitLayout();
    }
}
