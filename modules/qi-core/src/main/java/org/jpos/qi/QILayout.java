package org.jpos.qi;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import org.jdom2.Element;
import org.jpos.ee.User;
import org.jpos.q2.Q2;
import org.jpos.qi.views.login.LoginView;
import java.util.*;
import java.util.List;

public class QILayout<T> extends AppLayout {
    private Tabs tabs;
    private Map<Tab, Class<? extends HasComponents>> tabToView = new HashMap<>();
    private Map<Class<? extends HasComponents>, Tab> viewToTab = new HashMap<>();
    private QI app;

    public QILayout () {
        app = QI.getQI();
        setPrimarySection(Section.NAVBAR);
        addToNavbar(true, createHeader());
        addToDrawer(createDrawerContent());
        createSidebar("system");
//        UI.getCurrent().addBeforeEnterListener((BeforeEnterListener) beforeEnterEvent -> { //TODO: Check this.
//            Class<? extends Component> target = (Class<? extends Component>)beforeEnterEvent.getNavigationTarget();
//            boolean isRegistered = RouteConfiguration.forSessionScope().isRouteRegistered(target);
//            System.out.println(target + " -------- is registered? " + isRegistered);
//            if (beforeEnterEvent.getNavigationTarget() != LoginView.class && !isRegistered) {
//                beforeEnterEvent.rerouteTo(LoginView.class);
//            }
//        });
    }


    private Component createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("text-secondary");
        toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        toggle.getElement().setAttribute("aria-label", "Menu toggle");
        Image logo = new Image("images/jpos-no-bg.png", "jPOS Logo");
        logo.setHeight("44px");
        H1 appName = new H1("TPP");
        appName.addClassNames("m-0", "text-l");
        MenuBar menuBar = createMenuBar();
        menuBar.addClassNames("ml-auto");
        Header header = new Header(toggle, logo, appName, menuBar);
        header.addClassNames("bg-base", "border-b", "border-contrast-10", "box-border", "flex", "h-xl", "items-center",
          "w-full");
        return header;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON, MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_SMALL);
        menuBar.setOpenOnHover(true);
        MenuItem item = menuBar.addItem("Welcome " + app.getUser().getNick());
        item.add(new Icon(VaadinIcon.ANGLE_DOWN));
        SubMenu subMenu = item.getSubMenu();
        subMenu.addItem(
          app.getMessage("profile"),
          menuItemClickEvent -> UI.getCurrent().navigate("users/" + app.getUser().getId())
        );
        subMenu.addItem("Logout", (ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
            UI.getCurrent().getSession().close();
            UI.getCurrent().getSession().setAttribute(User.class, null);
            UI.getCurrent().navigate(LoginView.class);
        });
        return menuBar;
    }

    private Component createDrawerContent() {
        com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(
          createSidebar("system"), createFooter()
        );
        section.addClassNames("flex", "flex-col", "items-stretch", "max-h-full", "min-h-full");
        return section;
    }

    private Nav createSidebar (String sidebarId) {
        Nav nav = new Nav();
        nav.addClassNames("border-b", "border-contrast-10", "flex-grow", "overflow-auto");
        nav.getElement().setAttribute("aria-labelledby", "views");

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("list-none", "m-0", "p-0");
        nav.add(list);

        String id = sidebarId;
        Element cfg = app.getXmlConfiguration();
        
        for (Element sb : cfg.getChildren("sidebar")) {
        	boolean isFirstSection=true;
            String eid = sb.getAttributeValue("id");
            if (Objects.equals(eid, id)) {
                for (Element e : sb.getChildren()) {
                    if ("section".equals(e.getName()) && hasPermissionInSection(sb.getChildren(), e)) {
                        String sectionName = e.getAttributeValue("name");
                        String sectionIcon = e.getAttributeValue("icon", "");
                        MenuItemInfo menuItemInfo = new MenuItemInfo(app.getMessage(sectionName), sectionIcon, null);
                        ListItem item = new ListItem(createSection(menuItemInfo));
                        if(!isFirstSection) {
                        	 list.add(new Hr());
                        }
                        isFirstSection=false;
                        list.add(item);
                    } else if ("option".equals (e.getName())) {
                        if (QI.getQI().hasAccessToRoute(e.getAttributeValue("action"))) {
                            String optionName = e.getAttributeValue("name");
                            String optionIcon = e.getAttributeValue("icon", ""); // la la-globe
                            String optionAction = e.getAttributeValue("action");
                            Optional<Class<? extends Component>> clazz = RouteConfiguration.forSessionScope().getRoute(optionAction);
                            if (clazz.isPresent()) {
                                MenuItemInfo menuItemInfo = new MenuItemInfo(app.getMessage(optionName), optionIcon, clazz.get());
                                ListItem item = new ListItem(createLink(menuItemInfo));
                                list.add(item);
                            }
                        }
                    }
                }
            }
        }
        return nav;
    }


    private Nav createNavigation() {
        Nav nav = new Nav();
        nav.addClassNames("border-b", "border-contrast-10", "flex-grow", "overflow-auto");
        nav.getElement().setAttribute("aria-labelledby", "views");

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("list-none", "m-0", "p-0");
        nav.add(list);

        for (RouterLink link : createLinks()) {
            ListItem item = new ListItem(link);
            list.add(item);
        }
        return nav;
    }

    private List<RouterLink> createLinks() {
        List<RouteData> availableRoutes = RouteConfiguration.forSessionScope().getAvailableRoutes();
        MenuItemInfo[] menuItems = new MenuItemInfo[availableRoutes.size()-1];
        int i = 0;
        for (RouteData rd : availableRoutes) {
            if (LoginView.class.equals(rd.getNavigationTarget()))
                break;
            menuItems[i] = new MenuItemInfo(rd.getNavigationTarget().getSimpleName(), "la la-globe", rd.getNavigationTarget());
            i++;
        }
        List<RouterLink> links = new ArrayList<>();
        for (MenuItemInfo menuItemInfo : menuItems) {
            links.add(createLink(menuItemInfo));

        }
        return links;
    }

    private static RouterLink createLink(MenuItemInfo menuItemInfo) {
        RouterLink link = new RouterLink();
        link.addClassNames("flex", "mx-s", "p-s", "relative", "text-secondary");
        link.setRoute(menuItemInfo.getView());
        Span icon = new Span();
        icon.addClassNames("me-s", "text-l");
        if (!menuItemInfo.getIconClass().isEmpty()) {
            icon.addClassNames("la la-" + menuItemInfo.getIconClass());
        }
        Span text = new Span(menuItemInfo.getText());
        text.addClassNames("font-medium", "text-s");
        link.add(icon, text);
        return link;
    }

    private static Span createSection(MenuItemInfo menuItemInfo) {
        Span root = new Span();
        root.addClassNames("flex", "mx-s", "p-s", "relative", "text-secondary");
        if (!menuItemInfo.getIconClass().isEmpty()) {
            Span icon = new Span();
            icon.addClassNames("me-s", "text-l");
            icon.addClassNames("la la-" + menuItemInfo.getIconClass());
            root.add(icon);
        }
        Span text = new Span(menuItemInfo.getText());
        text.addClassNames("font-bold", "text-s");
        root.add(text);
        return root;
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames("flex-col", "items-center", "my-s", "px-m", "py-xs");
        Anchor jposLink = new Anchor("https://jpos.org", "Powered by jPOS");
        jposLink.addClassNames("text-secondary", "block");
        layout.add(jposLink);
        String[] versionArgs = Q2.getAppVersionString().split(" ");
        if (versionArgs.length >= 6) {
            String appVersion = versionArgs[1];
            String buildTime = versionArgs[3] + versionArgs[4] + versionArgs [5];
            Span version = new Span(appVersion);
            version.addClassNames("text-secondary","text-2xs", "block");
            Span build = new Span(buildTime);
            build.addClassNames("text-secondary","text-2xs", "block");
            layout.add(version, build);
        }
        return layout;
    }

    private void tabsSelectionChanged(Tabs.SelectedChangeEvent event) {
        if (event.isFromClient()) {
            UI.getCurrent().navigate((Class<? extends Component>) tabToView.get(event.getSelectedTab()));
        }
    }

    private void addTab(Class<? extends HasComponents> clazz, String viewName) {
        Tab tab = new Tab(viewName);
        tabs.add(tab);
        tabToView.put(tab, clazz);
        viewToTab.put(clazz, tab);
    }

    private boolean hasPermissionInSection (List<Element> elements, Element section) {
        List<Element> subElements = elements.subList(elements.indexOf(section)+1, elements.size());
        int nextSectionIndex = subElements.size();
        for (Element element : subElements) {
            if ("section".equals(element.getName())) {
                nextSectionIndex = subElements.indexOf(element);
                break;
            }
        }
        subElements = subElements.subList(0, nextSectionIndex);
        for (Element element : subElements) {
            if (QI.getQI().hasAccessToRoute(element.getAttributeValue("action")))
                return true;
        }
        return false;
    }

    public static class MenuItemInfo {
        private String text;
        private String iconClass;
        private Class<? extends Component> view;

        public MenuItemInfo(String text, String iconClass, Class<? extends Component> view) {
            this.text = text;
            this.iconClass = iconClass;
            this.view = view;
        }

        public String getText() {
            return text;
        }

        public String getIconClass() {
            return iconClass;
        }

        public Class<? extends Component> getView() {
            return view;
        }

    }
}
