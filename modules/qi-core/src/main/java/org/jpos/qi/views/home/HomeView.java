package org.jpos.qi.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.jpos.ee.User;

public class HomeView extends Composite<Component> {

    @Override
    public Component initContent() {
        User user = UI.getCurrent().getSession().getAttribute(User.class);
        String title = "";
        if (user != null)
            title = user.getNickAndId();
        VerticalLayout vl = new VerticalLayout(new H1("Welcome " + title));
        vl.setSizeFull();
        vl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        vl.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        vl.getStyle().set("text-align", "center");
        return vl;
    }

}
