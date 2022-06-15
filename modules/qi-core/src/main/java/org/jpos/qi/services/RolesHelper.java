package org.jpos.qi.services;

import org.jpos.ee.*;
import org.jpos.qi.QICrudFormFactory;
import org.jpos.qi.ViewConfig;

import java.util.ArrayList;
import java.util.List;

public class RolesHelper extends QIHelper {

    public RolesHelper (ViewConfig viewConfig) {
        super(Role.class, viewConfig);
    }

    @Override
    public String getItemId(Object item) {
        return String.valueOf(((Role)item).getId());
    }

    public List<Permission> getPermissions () {
        List<Permission> allPermissions = new ArrayList<>();
        List<SysConfig> sysConfigs = null;
        try {
            sysConfigs = DB.exec( (db) -> {
                SysConfigManager mgr = new SysConfigManager(db, "perm.");
                return mgr.getAll();
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
        for (SysConfig sysConfig : sysConfigs) {
            Permission p = Permission.valueOf(sysConfig.getId().substring(5));
            allPermissions.add(p);
        }
        return allPermissions;
    }

//    public ViewConfig getViewConfig () {
//        ViewConfig viewConfig = new ViewConfig();
//        viewConfig.addColumn("id", null);
//        viewConfig.addColumn("name", null);
//        viewConfig.addField("name", null, QICrudFormFactory.WORD_PATTERN, 64, true, null);
//        viewConfig.addField("permissions", null, null, 0, true, null);
//        return viewConfig;
//    }

//    //TODO: Remove and QIHelper::getEntityName should have Config set.
//    @Override
//    public String getEntityName() {
//        return "user";
//    }
}
