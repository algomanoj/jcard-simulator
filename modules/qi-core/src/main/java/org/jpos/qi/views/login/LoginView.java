package org.jpos.qi.views.login;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.jpos.ee.BLException;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpos.qi.QI;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Route("login")
@RouteAlias("")
public class LoginView extends Composite<Component> implements BeforeEnterObserver, HasErrorParameter<NotFoundException> {
    LoginOverlay loginOverlay;

    @Override
    protected Component initContent() {
        if (UI.getCurrent().getSession().getAttribute(User.class) == null)
            removeRoutes();
        LoginI18n i18n = LoginI18n.createDefault();
        loginOverlay = new LoginOverlay(i18n);
        loginOverlay.setTitle("jPOS Login");
        loginOverlay.setDescription("Please enter your credentials");
        loginOverlay.setOpened(true);
        loginOverlay.setForgotPasswordButtonVisible(false);
        loginOverlay.addLoginListener(event -> {
            User user = getUserByNick(event.getUsername(), event.getPassword());
            if (user != null && user.hasPermission("*login")) {
                VaadinSession.getCurrent().setAttribute(QI.class, new QI(user));
                VaadinSession.getCurrent().setAttribute(User.class, user);
                UI.getCurrent().navigate("");
                loginOverlay.close();
            } else {
                loginOverlay.setError(true);
            }
        });
        return new VerticalLayout(loginOverlay);
    }

    private void removeRoutes () {
        List<RouteData> availableRoutes = RouteConfiguration.forSessionScope().getAvailableRoutes();
        for (RouteData route : availableRoutes) {
            RouteConfiguration.forSessionScope().removeRoute(route.getNavigationTarget());
        }
    }

    public User getUserByNick (String nick, String pass) {
        try {
            return DB.execWithTransaction((db) -> {
                UserManager umgr = new UserManager (db);
                User user;
                try {
                    user = umgr.getUserByNick(nick, pass);
                    if (user.getPasswordHash().length() == 40)
                        umgr.upgradePassword(user, pass);
                } catch (BLException e) {
                    return null;
                }
                return user;
            });
        } catch (Exception e) {
            QI.getQI().getLog().error(e);
            return null;
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (UI.getCurrent().getSession().getAttribute(User.class) != null) {
            beforeEnterEvent.rerouteTo("home");
            loginOverlay.close();
        }
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        getElement().setText("Could not navigate to '" + event.getLocation().getPath() + "'");
        //if not logged in (user == null) stay in login, else move to 404.
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
