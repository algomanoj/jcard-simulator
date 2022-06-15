package org.jpos.qi.util;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class NotificationUtils {
	
	 
	  private NotificationUtils()
	  {
	  }
	  private static class NotificationProvider
	  {
	    private static final NotificationUtils INSTANCE = new NotificationUtils();
	  }
	 
	  public static NotificationUtils getInstance()
	  {
	    return NotificationProvider.INSTANCE;
	  }
	
	public Notification getErrorNotification(NotificationVariant notificationVariant,Notification.Position position ) {
        Notification notification = new Notification();
	    notification.addThemeVariants(notificationVariant);
	    notification.setPosition(position);
	    notification.setDuration(4000);
     
	    Icon icon = VaadinIcon.WARNING.create();
	    Div info = new Div(new Text(""));

	    Button closeBtn = new Button(
	            VaadinIcon.CLOSE_CIRCLE_O.create(),
	            clickEvent -> notification.close());
	    closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

	    HorizontalLayout layout = new HorizontalLayout(
	            icon, info, closeBtn);
	    layout.setAlignItems(FlexComponent.Alignment.CENTER);

	    notification.add(layout);

	    return notification;
	}
	
	public void showErrorNotification(String msg,Notification errorNotification){
		HorizontalLayout l=(HorizontalLayout) errorNotification.getChildren().findFirst().get();
	    Div div=(Div) l.getComponentAt(1);
	    Text text=(Text) div.getComponentAt(0);
	    text.setText(msg);
	    errorNotification.open();
	}

}
