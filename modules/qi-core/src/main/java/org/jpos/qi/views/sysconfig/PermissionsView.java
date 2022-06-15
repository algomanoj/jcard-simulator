package org.jpos.qi.views.sysconfig;

import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.SysConfigHelper;

public class PermissionsView extends SysConfigView {

    public PermissionsView () {
        super("permissions", "perm.");
    }

    @Override
    public QIHelper createHelper() {
        return new SysConfigHelper(getViewConfig(), "perm.");
    }
}
