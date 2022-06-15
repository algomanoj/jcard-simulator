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

package org.jpos.qi;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.*;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.Visitor;
import org.jpos.ee.VisitorManager;
import org.jpos.q2.Q2;
import org.jpos.q2.qbean.QXmlConfig;
import org.jpos.util.Log;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

public class QI {
    private Locale locale;
    private static final String CONFIG_NAME = "QI";
    private static final long CONFIG_TIMEOUT = 5000L;
    private List<Element> availableLocales;
    private HashMap<Locale, SortedMap<String,Object>> messagesMap;
    private List<Element> messageFiles;
    private ValueContext valueContext;
    private Log log;
    private Q2 q2;
    private HashMap<String,ViewConfig> views;
    private User user;
    Map<String,String> perms = new HashMap<>();
    Map<String,String> routes = new HashMap<>();

    public QI(User user) {
        VaadinSession.getCurrent().setAttribute(QI.class, this);
        locale = Locale.getDefault();
        log = Log.getLog(Q2.LOGGER_NAME, "QI");
        views = new HashMap<>();
        q2 = Q2.getQ2();
        messagesMap = new HashMap<>();
        valueContext = new ValueContext(locale);
        this.user = user;
        init(getXmlConfiguration());
    }
    public User getUser() {
        return user;
    }

    public static QI getQI() {
        return VaadinSession.getCurrent().getAttribute(QI.class);
    }

    private Visitor getVisitor(DB db) {
        VaadinRequest request = VaadinService.getCurrentRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];

        VisitorManager vmgr = new VisitorManager(db, cookies);
        Visitor v = vmgr.getVisitor(true);
        if (v != null) {
            vmgr.set (v, "IP", request.getRemoteAddr());
            vmgr.set (v,"HOST", request.getRemoteHost());
        }
        VaadinService.getCurrentResponse().addCookie(vmgr.getCookie());
        return v;
    }

    protected Element getXmlConfiguration() {
        Element cfg = QXmlConfig.getConfiguration (CONFIG_NAME, CONFIG_TIMEOUT);
        if (cfg == null) {
//            Notification.show(getMessage(ErrorMessage.SYSERR_CONFIG_NOT_FOUND), Notification.Type.ERROR_MESSAGE);
            return null;
        }
        return cfg;
    }

    public String getMessage (String id, Object... obj) {
        if (messagesMap.containsKey(locale)) {
            SortedMap map = messagesMap.get(locale);
            MessageFormat mf = new MessageFormat((String) map.getOrDefault(id, id));
            mf.setLocale(locale);
            return mf.format(obj);
        }
        return id;
    }
    public String getMessage (ErrorMessage em) {
        if (messagesMap.containsKey(locale)) {
            SortedMap map = messagesMap.get(locale);
            return (String) map.getOrDefault(em.getPropertyName(), em.getDefaultMessage());
        }
        return em.getPropertyName();
    }

    public String getMessage (ErrorMessage em, Object... obj) {
        if (messagesMap.containsKey(locale)) {
            SortedMap map = messagesMap.get(locale);
            String format = (String) map.getOrDefault(em.getPropertyName(), em.getDefaultMessage());
            MessageFormat mf = new MessageFormat(format, locale);
            return mf.format(obj);
        }
        return em.getPropertyName();
    }

    private void init (Element cfg) {
        String title = cfg.getChildText("title");
        String theme = cfg.getChildText("theme");
        String logger = cfg.getAttributeValue("logger");
        messageFiles = cfg.getChildren("messages");
        if (logger != null) {
            String realm = cfg.getAttributeValue("realm");
            log = Log.getLog(logger, realm != null ? realm : "QI");
        }
//        if (title != null)
//            getPage().setTitle(title);
//        if (theme != null) {
//            setTheme(theme);
//        }
        //Get all the available locales
        //The first one will be the default.
        availableLocales = cfg.getChildren("locale");
        if (availableLocales.size() > 0) {
            String localeName = availableLocales.get(0).getValue();
            if (localeName != null) {
                Locale l = Locale.forLanguageTag(localeName);
                if (l.hashCode() == 0) {
                    Notification.show(
                      getMessage(ErrorMessage.SYSERR_INVALID_LOCALE, localeName)
                  );
                } else {
                    locale = l;
                }
            }
        }
        parseMessages();
        initRoutes();
    }

    private void parseMessages() {
        Properties master = new Properties();
        for (Element element: availableLocales) {
            String localeCode = element.getValue();
            Locale l = Locale.forLanguageTag(localeCode);
            Iterator<Element> iterator = messageFiles.iterator();
            if (iterator.hasNext()) {
                String masterName = iterator.next().getValue();
                try {
                    master.load(getClass().getResourceAsStream("/" + masterName.concat("_" + localeCode + ".properties")));
                    while (iterator.hasNext()) {
                        Properties additionalProp = new Properties();
                        String additionalName = iterator.next().getValue();
                        additionalProp.load(getClass().getResourceAsStream("/" + additionalName.concat("_" + localeCode + ".properties")));
                        master.putAll(additionalProp);
                    }
                } catch (NullPointerException | IOException n) {
                    //Log but continue
                    //Show notification only if main locale is faulty
                    if (locale.toString().equals(localeCode))
                        displayNotification("Invalid locale '" + localeCode +"' : check configuration");
                    log.error(ErrorMessage.SYSERR_INVALID_LOCALE,localeCode);
                }
                TreeMap<String, Object> treeMap = new TreeMap<>((Map) master);
                messagesMap.put(l, treeMap);
            }
        }
    }

    private void initRoutes() {
        for (Element e : getXmlConfiguration().getChildren("view")) {
            String route = e.getAttributeValue("route");
            String clazz = e.getAttributeValue("class");
            String perm = e.getAttributeValue("perm", "-");
            try {
                Class c = Class.forName(clazz);
                RouteConfiguration.forSessionScope().setRoute(route + "/:id?", c, QILayout.class);
                perms.put(route, perm);
                e.getChildren("property").stream()
                  .filter(p -> "entityName".equals(p.getAttributeValue("name")))
                  .forEach(p ->
                    routes.putIfAbsent(p.getAttributeValue("value"), route)
                  );
            } catch (ClassNotFoundException ex) {
                getLog().error(ex);
            }
            addView(route, e);
        }
    }

    void addView(String route, Element e) {
        ViewConfig vc = new ViewConfig();
        try {
            vc.setXmlElement(e);
            vc.setConfiguration(getQ2().getFactory().getConfiguration(e));
            this.views.put(route, vc);
        } catch (ConfigurationException | DataConversionException exc) {
            getLog().warn(exc);
        }
    }

    public boolean hasAccessToRoute (String route) {
        String required = perms.get(route);
        required = "*".equals(required) ? null : required;
        return required == null || QI.getQI().getUser().hasPermission(required);
    }

    public ViewConfig getView(String route) {
        return views.get(route);
    }

    public Log getLog() {
        return log;
    }

    public void displayNotification (String message) {
        Notification.show(message);
    }

    public Q2 getQ2() {
        return q2;
    }

    public void setQ2(Q2 q2) {
        this.q2 = q2;
    }
}
