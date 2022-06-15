package org.jpos.qi.views.test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import org.jpos.qi.QILayout;
import org.jpos.qi.views.BlankLayout;

import java.util.Optional;

@PageTitle("Test")
@Route(value = "test/:value?", layout = QILayout.class)
public class TestView extends Composite<Component> implements BeforeEnterObserver {
    private H1 title;

    @Override
    public Component initContent() {
        VerticalLayout vl = new VerticalLayout();
        vl.add(new Paragraph("Test View on blank page"));
        vl.setSizeFull();
        vl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        vl.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        title = new H1("Test");
        vl.add(title);
        vl.getStyle().set("text-align", "center");
        return vl;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> value = event.getRouteParameters()
          .get("value");
        title.setText(value.orElse("(no value)"));
    }

}
