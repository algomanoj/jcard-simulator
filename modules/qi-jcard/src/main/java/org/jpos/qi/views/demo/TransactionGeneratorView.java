package org.jpos.qi.views.demo;

import org.jpos.qi.QI;
import org.jpos.qi.QIUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

enum RunningStatus{
	STARTED,STOPPED,RUNNING;
}

public class TransactionGeneratorView extends Composite<VerticalLayout> {
	private static final long serialVersionUID = 1L;
	private QI app;
	VerticalLayout vl;
	TextField txnStatus;
	Button startBtn;
	Button stopBtn;
	RunningStatus defaultStatus=RunningStatus.STOPPED;
	
	public TransactionGeneratorView(){
		super();
		app = QI.getQI();
	}
	
	@Override
	public VerticalLayout initContent() {
		vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(app.getMessage("demo.txn.generator.title"));
		viewTitle.addClassNames("mt-s", "text-l");
		
		
		vl.add(viewTitle, createStatusField(),createButton());
		
		
		return vl;
	}

	private Component createButton() {
		HorizontalLayout hl = new HorizontalLayout();
		
		startBtn= new Button();
		startBtn.setText(QIUtils.getCaptionFromId("field.txn.gen.start.button"));
		startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		startBtn.addClickListener(event -> {
			handleStartEvent();
		});
		
		stopBtn= new Button();
		stopBtn.setText(QIUtils.getCaptionFromId("field.txn.gen.stop.button"));
		stopBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
		stopBtn.addClickListener(event ->{
			handleStopEvent();
		});
		stopBtn.getStyle().set("padding-left", "20px");
		stopBtn.setEnabled(false);
		
		hl.add(startBtn,stopBtn);
		return hl;
	}

	private void handleStopEvent() {
		txnStatus.setValue(RunningStatus.STOPPED.name());
		stopBtn.setEnabled(false);
		startBtn.setEnabled(true);
	}

	private void handleStartEvent() {
		txnStatus.setValue(RunningStatus.RUNNING.name());
		stopBtn.setEnabled(true);
		startBtn.setEnabled(false);
		
	}

	private Component createStatusField() {
		txnStatus = new TextField();
		txnStatus.setWidth("100%");
		txnStatus.setValue(defaultStatus.name());
		FormLayout leftFormLayout = new FormLayout();
		leftFormLayout.addFormItem(txnStatus,QIUtils.getCaptionFromId("field.txn.gen.status"));
		return leftFormLayout;
	}

}
