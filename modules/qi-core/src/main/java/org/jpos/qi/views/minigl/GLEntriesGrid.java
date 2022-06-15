package org.jpos.qi.views.minigl;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.renderer.LitRenderer;
import org.jpos.gl.GLEntry;
import org.jpos.gl.GLTransaction;
import static org.jpos.qi.QIUtils.getCaptionFromId;

public class GLEntriesGrid extends Grid<GLEntry> {
    public GLEntriesGrid (GLTransaction txn) {
        super();
        setGridGetters();
        setItems(txn.getEntries());
        addThemeVariants(GridVariant.LUMO_COMPACT);
        addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        setAllRowsVisible(true);
    }

    public void setGridGetters () {
        addColumn(LitRenderer
          .<GLEntry>of("<label>${item.accountCode}</label>&nbsp;<small>${item.accountDesc}</small")
          .withProperty("accountCode",
            glEntry -> glEntry.getAccount() != null ? glEntry.getAccount().getCode() : "")
          .withProperty("accountDesc",
            glEntry -> glEntry.getAccount() != null ? glEntry.getAccount().getDescription() : "")
        ).setHeader(getCaptionFromId("column.account")).setAutoWidth(true).setId("account");
        addColumn(GLEntry::getLayer).setHeader(getCaptionFromId("column.layer")).setId("layer");
        addColumn(GLEntry::getDetail).setHeader(getCaptionFromId("column.detail")).setId("detail");
        addColumn((entry) -> entry.isDebit() ? entry.getAmount() : null)
          .setHeader(getCaptionFromId("column.debit"))
          .setTextAlign(ColumnTextAlign.END)
          .setId("debit");
        addColumn((entry) -> entry.isCredit() ? entry.getAmount() : null)
          .setHeader(getCaptionFromId("column.credit"))
          .setTextAlign(ColumnTextAlign.END)
          .setId("credit");
    }
}
