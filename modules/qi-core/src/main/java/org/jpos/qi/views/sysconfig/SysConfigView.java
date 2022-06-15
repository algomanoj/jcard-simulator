/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
 *
 * This program is free software: you can redist`ribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.qi.views.sysconfig;

import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import org.jpos.ee.SysConfig;
import org.jpos.qi.QIUtils;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.SysConfigHelper;
import org.jpos.qi.views.QIEntityView;
import org.vaadin.crudui.crud.impl.GridCrud;
import java.util.*;

@PageTitle("SysConfig")
public class SysConfigView extends QIEntityView {

    private String prefix;

    public SysConfigView (String name, String prefix) {
        super(SysConfig.class, name);
        this.prefix = prefix;
    }

    public SysConfigView() {
        super(SysConfig.class,"sysconfig");
    }

    @Override
    public GridCrud createCrud () {
        GridCrud crud = super.createCrud();
        crud.getGrid().removeAllColumns();
        crud.getGrid().addColumn(
          new TextRenderer<SysConfig>(revision-> ((SysConfigHelper)getHelper()).removePrefix(revision.getId()))
          )
          .setHeader(QIUtils.getCaptionFromId("column.id"))
          .setSortProperty("id");
        Set<String> visibleColumns = new HashSet<>();
        visibleColumns.addAll(Arrays.asList(getViewConfig().getVisibleColumns()));
        visibleColumns.remove("id");
        crud.getGrid().addColumns(visibleColumns.toArray(new String[0]));
        return crud;
    }

    @Override
    public QIHelper createHelper() {
        return new SysConfigHelper(getViewConfig(), prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}