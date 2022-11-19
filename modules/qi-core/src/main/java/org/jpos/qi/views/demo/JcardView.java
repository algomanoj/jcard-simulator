package org.jpos.qi.views.demo;

import org.jpos.qi.QI;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class JcardView extends Composite<VerticalLayout> {
	private QI app;
	public JcardView() {
		super();
		app = QI.getQI();	
	}
	
	@Override
	public VerticalLayout initContent() {
		VerticalLayout vl = new VerticalLayout();
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidthFull();
		
		H2 viewTitle = new H2(app.getMessage("jCARD"));
		viewTitle.addClassNames("mt-s", "text-l");
		vl.add(viewTitle);
		
		return vl;	
	}

}
