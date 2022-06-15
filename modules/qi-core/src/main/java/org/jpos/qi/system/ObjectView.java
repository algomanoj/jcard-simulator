/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
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

package org.jpos.qi.system;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jpos.iso.ISOUtil;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@PageTitle("Object")
public abstract class ObjectView extends VerticalLayout implements Runnable {
    private static final int TIMEOUT = 10000;
    private AtomicReference<UI> ui = new AtomicReference<>();
    private Label label = new Label();
    private Supplier<String> supplier;
    private long refresh;

    protected ObjectView (Supplier<String> supplier, long refresh) {
        this.supplier = supplier;
        this.refresh = refresh;
        if (refresh < 100L || refresh > TIMEOUT)
            this.refresh = 1000L; // force reasonable refresh period
        add (label);
    }

    @Override
    protected void onAttach(AttachEvent ev) {
        ui.set(ev.getUI());
        ui.get().setPollInterval(TIMEOUT); // early detect if user goes away
        new Thread(this).start();
    }

    @Override
    protected void onDetach(DetachEvent ev) {
        ui.set(null);
    }

    @Override
    public void run () {
        final AtomicLong lastTick = new AtomicLong(System.currentTimeMillis());
        for (UI  u = ui.get(); ui.get() != null && lastTick.get() > 0;) {
            u.access(() -> {
                label.setText (supplier.get());
                long l = u.getSession().getLastRequestTimestamp();
                if (System.currentTimeMillis() - l > TIMEOUT*2)
                    lastTick.set(0L);
                else
                    lastTick.set(l);
            });
            ISOUtil.sleep(refresh);
        }
    }
}
