package org.jpos.qi.services;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import org.jpos.ee.DB;
import org.jpos.ee.SysConfig;
import org.jpos.ee.SysConfigManager;
import org.jpos.qi.QICrudFormFactory;
import org.jpos.qi.ViewConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SysConfigHelper extends QIHelper {
    private String prefix;

    public SysConfigHelper (ViewConfig viewConfig) {
        super(SysConfig.class, viewConfig);
    }

    public SysConfigHelper (ViewConfig viewConfig, String prefix) {
        super(SysConfig.class, viewConfig);
        this.prefix = prefix;
    }

    @Override
    public String getItemId(Object item) {
        return String.valueOf(((SysConfig)item).getId());
    }

    @Override
    public QICrudFormFactory createCrudFormFactory () {
        return new QICrudFormFactory(clazz, getViewConfig(), this) {
            public Binder.BindingBuilder formatField(String id, HasValue field) {
                Binder.BindingBuilder builder = super.formatField(id, field);
                if ("id".equals(id) && prefix != null && !prefix.isEmpty()) {
                    builder.withConverter(new Converter() {
                        @Override
                        public Result convertToModel(Object input, ValueContext valueContext) {
                            return Result.ok(addPrefix((String) input));
                        }
                        @Override
                        public Object convertToPresentation(Object value, ValueContext valueContext) {
                            return removePrefix((String) value);
                        }
                    });
                }
                return builder;
            }
        };
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String removePrefix (String value) {
        if (prefix != null && value != null && !value.isEmpty() && value.startsWith(prefix))
            return value.substring(prefix.length());
        return value;
    }

    public String addPrefix (String value) {
        if (prefix != null && value != null && !value.isEmpty() && !value.startsWith(prefix))
            return prefix + value;
        else
            return value;
    }

    public Stream getAll(int offset, int limit, Map<String, Boolean> orders) throws Exception {
        List items = DB.exec(db -> {
            SysConfigManager mgr = prefix != null ? new SysConfigManager(db, prefix) : new SysConfigManager(db);
            return mgr.getAll(offset,limit,orders);
        });
        return items.stream();
    }

    public int getItemCount() throws Exception {
        return DB.exec(db -> {
            SysConfigManager mgr = prefix != null ? new SysConfigManager(db, prefix) : new SysConfigManager(db);
            return mgr.getItemCount();
        });
    }

//    `@Override
//    public ViewConfig getViewConfig() {
//        return null;
//    }`

//    @Override
//    public ViewConfig getViewConfig() {
//        ViewConfig viewConfig = new ViewConfig();
//        viewConfig.addColumn("id", null);
//        viewConfig.addColumn("value", null);
//        viewConfig.addField("id", null, QICrudFormFactory.SYSCONFIG_ID_PATTERN, 64, true, null);
//        viewConfig.addField("value", null, QICrudFormFactory.TEXT_PATTERN, 0, true, null);
//        viewConfig.addReadOnly("id");
//        return viewConfig;
//    }
}
