package org.jpos.qi.views.revision;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.RouteParameters;
import org.jpos.ee.Revision;
import org.jpos.ee.User;
import org.jpos.qi.QI;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.RevisionHistoryHelper;
import org.jpos.qi.views.QIGridView;
import org.jpos.qi.views.users.UsersView;

import java.util.ArrayList;
import java.util.Collections;

public class RevisionHistoryView extends QIGridView<Revision> {
    private QI app;

    public RevisionHistoryView() {
        super("revision_history");
    }

    @Override
    public Grid createGrid() {
        Grid<Revision> grid = super.createGrid();
        GridSortOrder<Revision> order = new GridSortOrder<>(
          grid.getColumnByKey("date"), SortDirection.ASCENDING
        );
        grid.sort(new ArrayList<>(Collections.singleton(order)));
        return grid;
    }

    private static LitRenderer<Revision> createUserRenderer() {
        return LitRenderer.<Revision>of(
            "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
              + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
              + "    <a @click=${onClick}><span>${item.nick}</span></a>"
              + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
              + "      ${item.name}" + "    </span>"
              + "  </vaadin-vertical-layout>"
              + "</vaadin-horizontal-layout>")
          .withFunction("onClick", revision -> {
              User u = revision.getAuthor();
              if (u != null) {
                  UI.getCurrent().navigate(UsersView.class, new RouteParameters("id", u.getId().toString()));
              }
          })
          .withProperty("nick", revision -> revision.getAuthor() != null ? revision.getAuthor().getNick() : "")
          .withProperty("name", revision -> revision.getAuthor() != null ? revision.getAuthor().getName() : "");
    }

    private static ComponentRenderer createInfoRenderer() {
        return new ComponentRenderer<Html, Revision>(
          revision -> new Html("<span>" + revision.getInfo() + "</span>")
        );
    }
    
    public QIHelper createHelper() {
        return new RevisionHistoryHelper(getViewConfig());
    }

    @Override
    public Renderer buildCustomColumnRenderer (String columnId) {
        if ("author".equals(columnId)) {
            return createUserRenderer();
        } else if ("info".equals(columnId)) {
            return createInfoRenderer();
        }
        return null;
    }

    public QI getApp() {
        return app;
    }

    public void setApp(QI app) {
        this.app = app;
    }
}
