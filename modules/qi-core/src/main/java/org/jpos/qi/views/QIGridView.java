package org.jpos.qi.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.XmlConfigurable;
import org.jpos.qi.QI;
import org.jpos.qi.QIUtils;
import org.jpos.qi.ViewConfig;
import org.jpos.qi.services.QIHelper;
import java.lang.reflect.ParameterizedType;

public abstract class QIGridView<T> extends Composite<Component> implements Configurable, XmlConfigurable {
    private QI app;
    private Class<T> clazz;
    private String name;
    private String title;
    private Grid<T> grid;
    private QIHelper helper;
    private ViewConfig viewConfig;
    private Configuration cfg;

    public QIGridView (String name) {
        super();
        app = QI.getQI();
        this.setViewConfig(app.getView(name));
        this.helper = createHelper();
        String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].toString();
        try {
            clazz = (Class<T>) Class.forName (className.substring(6));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(className);
        }
    }

    @Override
    public Component initContent() {
        VerticalLayout vl = new VerticalLayout();
        vl.setHeightFull();
        H2 viewTitle = new H2(app.getMessage(""));
        viewTitle.addClassNames("mt-s", "text-l");
        createGrid();
        vl.add(viewTitle, grid);
        return vl;
    }

    public Grid createGrid() {
        grid = new Grid(clazz, false);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        if (getViewConfig() != null) {
            String[] visibleColumns = getViewConfig().getVisibleColumns();
            for (String columnId : visibleColumns) {
                Renderer columnRenderer = buildCustomColumnRenderer(columnId);
                Grid.Column c;
                if (columnRenderer != null) {
                    c = grid.addColumn(columnRenderer);
                } else {
                    c = grid.addColumn(columnId);
                }
                c.setHeader(QIUtils.getCaptionFromId("column." + columnId));
            }
        }
        grid.setItems(getHelper().getDataProvider());
        return grid;
    }

    public Renderer buildCustomColumnRenderer (String columnId) {
        return null;
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

    public Grid<T> getGrid() {
        return grid;
    }

    public void setGrid(Grid<T> grid) {
        this.grid = grid;
    }

    public ViewConfig getViewConfig() {
        return viewConfig;
    }

    public void setViewConfig(ViewConfig viewConfig) {
        this.viewConfig = viewConfig;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
    }

    public void setConfiguration (Element element) {
        Attribute routeAttribute = element.getAttribute("route");
        name = routeAttribute != null ? routeAttribute.getValue() : name;
//        generalRoute = routeAttribute != null ? "/" + routeAttribute.getValue() : generalRoute;
        if (name != null && QI.getQI().getView(name)!= null)  {
//            this.setViewConfig(QI.getQI().getView(name));
            this.title = "<strong>" + app.getMessage(name) + "</strong>";
//            this.visibleColumns = getViewConfig().getVisibleColumns();
//            this.visibleFields = getViewConfig().getVisibleFields();
//            this.readOnlyFields = getViewConfig().getReadOnlyFields();
        }
    }
    
    public abstract QIHelper createHelper();
}
