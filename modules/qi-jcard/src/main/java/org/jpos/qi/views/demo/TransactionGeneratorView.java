package org.jpos.qi.views.demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;
import org.jpos.qi.QI;
import org.jpos.qi.QIUtils;
import org.jpos.util.NameRegistrar;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

enum RunningStatus {
	STARTED, STOPPED, RUNNING;
}

public class TransactionGeneratorView extends Composite<VerticalLayout> {
	private static final long serialVersionUID = 1L;
	private QI app;
	VerticalLayout vl;
	TextField txnStatus;
	Button startBtn;
	Button stopBtn;
	RunningStatus defaultStatus = RunningStatus.STOPPED;

	public TransactionGeneratorView() {
		super();
		app = QI.getQI();
	}

	@Override
	public VerticalLayout initContent() {
		vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(app.getMessage("demo.txn.generator.title"));
		viewTitle.addClassNames("mt-s", "text-l");

		vl.add(viewTitle, createStatusField(), createButton());

		return vl;
	}

	private Component createButton() {
		HorizontalLayout hl = new HorizontalLayout();

		startBtn = new Button();
		startBtn.setText(QIUtils.getCaptionFromId("field.txn.gen.start.button"));
		startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		startBtn.addClickListener(event -> {
			handleStartEvent();
		});
		startBtn.setEnabled(!isRunning());
		stopBtn = new Button();
		stopBtn.setText(QIUtils.getCaptionFromId("field.txn.gen.stop.button"));
		stopBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
		stopBtn.addClickListener(event -> {
			handleStopEvent();
		});
		stopBtn.getStyle().set("padding-left", "20px");
		stopBtn.setEnabled(isRunning());

		hl.add(startBtn, stopBtn);
		return hl;
	}

	private void handleStopEvent() {
		// call the Txn generator to start the service
		try {
			MUX mux = NameRegistrar.getIfExists("mux.sim");
			ISOMsg resp = mux.request(createRequest("200"), 5000);
			if (resp != null && resp.hasField(39) && "0000".equals(resp.getString(39))) {
				txnStatus.setValue(RunningStatus.STOPPED.name());
				stopBtn.setEnabled(false);
				startBtn.setEnabled(true);
			}
		} catch (ISOException e) {
			e.printStackTrace();
		}
	}

	private void handleStartEvent() {

		// call the Txn generator to start the service
		try {
			MUX mux = NameRegistrar.getIfExists("mux.sim");
			ISOMsg resp = mux.request(createRequest("100"), 5000);
			if (resp != null && resp.hasField(39) && "0000".equals(resp.getString(39))) {
				txnStatus.setValue(RunningStatus.RUNNING.name());
				stopBtn.setEnabled(true);
				startBtn.setEnabled(false);
			}
		} catch (ISOException e) {
			e.printStackTrace();
		}
	}

	private Component createStatusField() {
		txnStatus = new TextField();
		txnStatus.setWidth("100%");
		txnStatus.setValue(getStatus());
		txnStatus.setReadOnly(true);
		FormLayout leftFormLayout = new FormLayout();
		leftFormLayout.addFormItem(txnStatus, QIUtils.getCaptionFromId("field.txn.gen.status"));
		return leftFormLayout;
	}

	private ISOMsg createRequest(String code) {

		DateFormat df = new SimpleDateFormat("MMddHHmmss");
		String date = df.format(new Date());
		ISOMsg msg = new ISOMsg("2800");
		msg.set(7, date);
		msg.set(11, "00" + date);
		msg.set(70, code);
		return msg;
	}

	private String getStatus() {
		// call the Txn generator to check status
		try {
			MUX mux = NameRegistrar.getIfExists("mux.sim");
			ISOMsg resp = mux.request(createRequest("300"), 5000);
			if (resp != null && resp.hasField(39) && "0000".equals(resp.getString(39))) {
				defaultStatus = RunningStatus.RUNNING;
				return defaultStatus.name();

			}
		} catch (ISOException e) {
			e.printStackTrace();
		}
		return RunningStatus.STOPPED.name();
	}

	private boolean isRunning() {
		return defaultStatus==RunningStatus.RUNNING;
	}
}
