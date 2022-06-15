package org.jpos.qi.util;

import java.util.Collection;
import java.util.List;

import org.jpos.ee.DB;
import org.jpos.ee.SysConfig;
import org.jpos.ee.SysConfigManager;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.DataProvider;

public class SysConfigComboBox extends ComboBox {
    public SysConfigComboBox (String caption, DataProvider dataProvider) {
        super(caption);
        setDataProvider(dataProvider);
        setItemLabelGenerator((ItemLabelGenerator<SysConfig>) captionGenerator -> captionGenerator != null ? captionGenerator.getValue()
				: "");
        //setEmptySelectionAllowed(false);
    }	


    public SysConfigComboBox (String caption, Collection<SysConfig> items) {
        super(caption);
        setItems(items);
        setItemLabelGenerator((ItemLabelGenerator<SysConfig>) captionGenerator -> captionGenerator != null ? captionGenerator.getValue()
						: "");
    }

    public SysConfigComboBox (String caption, String prefix) throws Exception {
        this(caption, prefix, false);
    }

    public SysConfigComboBox (String caption, String prefix, boolean includeId) throws Exception {
        super(caption);
        List items = DB.exec(db -> {
            SysConfigManager mgr = new SysConfigManager(db, prefix);
            return mgr.getAllByValue();
        });
        setItems(items);
        setItemLabelGenerator((ItemLabelGenerator<SysConfig>) item -> {
            if (includeId)
                return stripPrefixFromId(prefix, item.getId()) + " - " + item.getValue();
            else
                return item.getValue();
        });
       // setEmptySelectionAllowed(false);
    }

    private String stripPrefixFromId (String prefix, String id) {
        return prefix != null ? id.substring(prefix.length()) : id;
    }
}
