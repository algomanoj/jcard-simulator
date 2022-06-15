package org.jpos.qi.views.card;

import java.util.Optional;

import org.jpos.core.Configuration;
import org.jpos.ee.Card;
import org.jpos.qi.QI;
import org.jpos.qi.services.CardHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.SearchHelper;
import org.jpos.qi.views.QIEntityView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;

public class CardResultView extends QIEntityView<Card> {
	SearchHelper helper;
	Card card;
	QI app;
	VerticalLayout vLayout;

	public CardResultView() {
		super(Card.class, "result/card");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		Optional<String> id = event.getRouteParameters().get("id");
		long cardId = id.isPresent() && id != null ? Long.parseLong(id.get()) : null;
		card = helper.getCardById(cardId);
		if (card != null) {
			if(vLayout != null) {
				vLayout = new VerticalLayout();
			}
			vLayout.add(createHeader("Card: " + card.getToken() + " - ..." + card.getLastFour()));
			// TabSheet tabSheet = createTabSheet();
			// addComponent(tabSheet);
			// setExpandRatio(tabSheet, 1f);
		}
	}

	@Override
	public Component initContent() {
		vLayout = new VerticalLayout();
		return vLayout;
	}


	protected HorizontalLayout createHeader(String title) {
		HorizontalLayout header = new HorizontalLayout();
		header.setWidth("100%");
		header.setSpacing(false);
		// header.setMargin(new MarginInfo(false, true, false, true));
		H2 lbl = new H2(title);
		lbl.setSizeUndefined();
		header.add(lbl);
		// Add Back Button
//        if (((QINavigator) QI.getQI().getNavigator()).hasHistory()) {
//            Button back = new Button(app.getMessage("back"));
//            back.setStyleName(ValoTheme.BUTTON_LINK);
//            back.setIcon(VaadinIcons.ARROW_LEFT);
//            back.addClickListener(clickEvent -> ((QINavigator)app.getNavigator()).navigateBack());
//            header.addComponent(back);
//            header.setComponentAlignment(back, Alignment.BOTTOM_RIGHT);
//            header.setExpandRatio(back, 1f);
//        }
		return header;
	}

//    private CardTabSheet createTabSheet() {
//        return new CardTabSheet(card);
//    }

	@Override
	public void setConfiguration(Configuration cfg) {
	}

	@Override
	public QIHelper createHelper() {
		return new CardHelper(getViewConfig());
	}

	public CardHelper getHelper() {
		return (CardHelper) super.getHelper();
	}
}
