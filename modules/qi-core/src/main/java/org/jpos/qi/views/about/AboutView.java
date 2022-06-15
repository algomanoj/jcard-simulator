package org.jpos.qi.views.about;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;

import org.jpos.q2.Q2;

@PageTitle("About")
public class AboutView extends VerticalLayout {
    public AboutView() {
        Image img = new Image("images/jpos.png", "jPOS Logo");
        img.setWidth("200px");
        add(img);

        add(new Pre(Q2.getVersionString()));

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }
}
