package org.jpos.qi;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

/**
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@Theme(value = "jpos")
@PWA(name = "jPOS", shortName = "QI", offlineResources = {"images/logo.png"})
@NpmPackage(value = "line-awesome", version = "1.3.0")
@Push
public class AppShell implements AppShellConfigurator {
    private static final int TIMEOUT = 10000;

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.getUi().ifPresent(ui -> {
            ui.setPollInterval(TIMEOUT);
            ui.getSession().getSession().setMaxInactiveInterval(TIMEOUT + 15);
        });
    }
}
