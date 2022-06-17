package org.jpos.qi.views.demo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

import org.jpos.core.Configuration;
import org.jpos.iso.*;
import org.jpos.iso.PosDataCode.POSEnvironment;
import org.jpos.q2.iso.QMUX;
import org.jpos.qi.QI;
import org.jpos.qi.QIUtils;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;
import java.math.BigDecimal;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;


public class TransactView extends Composite<VerticalLayout> {
	public class TranTypeData {
		private String mti;
		private String ttc;
		

		public TranTypeData() {

		}

		public TranTypeData(String mti, String ttc) {
			this.mti = mti;
			this.ttc = ttc;
		}

		public String getMti() {
			return mti;
		}

		public void setMti(String mti) {
			this.mti = mti;
		}

		public String getTtc() {
			return ttc;
		}

		public void setTtc(String ttc) {
			this.ttc = ttc;
		}
	};

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TOKEN_PATTERN = "^$|[\\w\\s.\\-\']{6,64}$";
	public static final String NAME_PATTERN = "^$|[&0-9a-zA-Z Ã¡Ã©Ã­Ã³ÃºÃ�Ã‰Ã�Ã“ÃšÃ±Ã‘]+$";
	public static final String WORD_PATTERN = "^[\\w.\\-]*$";
	public static final String ACCOUNT_PATTERN = "^$|[a-zA-Z0-9][-._a-zA-Z0-9]{0,62}[a-zA-Z0-9]$";
	public static final String CARDNUM_PATTERN = "^$|[\\d]{16,19}$";
	public static final String PHONE_PATTERN = "^$|\\d{1,15}$";
	private QI app;
	private TextField cardNumberField;
	private DatePicker expDateField;
	private TextField amountField;
	private TextField currencyField;
	private TextField cvvField;
	private TextField pinField;
	private TextField stanField;
	private TextField rrnField;
	private ComboBox<String> entryModeField;
	private ComboBox<String> tranTypeField;
	private TextField termIdField;
	private TextField midField;
	private TextField merchDetField;
	private Label tranTypeLabel;
	Button txnBtn;

	private Label statusLbl;
	private Label rspCodeLbl;
	private Label authCodeLbl;
	private Label balAmtLbl;
	private HashMap<String, TranTypeData> tranTypeData;
	private HashMap<String, byte[]> posEnvData;
	VerticalLayout vl;

	public TransactView() {
		super();
		posEnvData = new HashMap<>();
		for (POSEnvironment posEnv : POSEnvironment.values()) {
			if(posEnv != POSEnvironment.ATTENDED) {
				continue;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("04000000");
			try {
				sb.append(ISOUtil.zeropad(Integer.toHexString(posEnv.intValue()), 8));
			} catch (ISOException e) {
				sb.append("00000000");
			}
			sb.append("02000000");
			sb.append("01000000");
			posEnvData.put(posEnv.toString(), ISOUtil.hex2byte(sb.toString()));
		}

		tranTypeData = new HashMap<>();
		TranTypeData emd = new TranTypeData("100", "00");
		tranTypeData.put("Authorization", emd);

		tranTypeLabel = new Label();
		tranTypeLabel.setVisible(false);
		/*
		emd = new TranTypeData("100", "01");
		tranTypeData.put("Authorization (Cash Withdrawal)", emd);

		emd = new TranTypeData("100", "02");
		tranTypeData.put("Authorization Void (Goods and Services)", emd);

		emd = new TranTypeData("100", "09");
		tranTypeData.put("Authorization (Purchase with cashback)", emd);

		emd = new TranTypeData("100", "20");
		tranTypeData.put("Refund/Return (Authorization / Balance Inquiry)", emd);

		emd = new TranTypeData("100", "22");
		tranTypeData.put("Refund/Return Void", emd);

		emd = new TranTypeData("100", "30");
		tranTypeData.put("Balance Inquiry", emd);
		*/
		
		emd = new TranTypeData("200", "00");
		tranTypeData.put("POS Purchase", emd);

		/*
		emd = new TranTypeData("200", "01");
		tranTypeData.put("Cash Withdrawal", emd);

		emd = new TranTypeData("200", "02");
		tranTypeData.put("Void", emd);

		emd = new TranTypeData("200", "09");
		tranTypeData.put("POS Purchase With Cashback", emd);

		emd = new TranTypeData("200", "20");
		tranTypeData.put("Refund / Return", emd);

		emd = new TranTypeData("200", "21");
		tranTypeData.put("Payment / Deposit / Refresh", emd);

		emd = new TranTypeData("200", "40");
		tranTypeData.put("Account Transfer", emd);

		emd = new TranTypeData("220", "00");
		tranTypeData.put("Purchase Advice", emd);

		emd = new TranTypeData("220", "20");
		tranTypeData.put("Refund / Return Advice", emd);

		emd = new TranTypeData("304", "72");
		tranTypeData.put("Card Activation", emd);

		emd = new TranTypeData("304", "7S");
		tranTypeData.put("Refund / Return Advice", emd);
		*/
		
		/*
		 * emd = new TranTypeData("420", "XX"); tranTypeData.put("Reversal", emd);
		 */

		app = QI.getQI();

		
	}
	
	@Override
	public VerticalLayout initContent() {
		vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(app.getMessage("demo.title"));
		viewTitle.addClassNames("mt-s", "text-l");
		vl.add(viewTitle, createTransactForm());
		
		Hr hr = new Hr();
		hr.setWidth(100f, Unit.PERCENTAGE);
		vl.add(hr);
		
		Label respLabel = new Label("Response");
		respLabel.setWidth(100f, Unit.PERCENTAGE);
		vl.add(respLabel);
		
		vl.add(createResponseForm());
		
		return vl;
	}

	private VerticalLayout createResponseForm() {
		VerticalLayout rspLayout = new VerticalLayout();
		rspLayout.setWidth("100%");
		rspLayout.setSpacing(true);
		rspLayout.setMargin(true);

		statusLbl = new Label();
		statusLbl.setVisible(false);

		rspCodeLbl = new Label();
		rspCodeLbl.setVisible(false);

		authCodeLbl = new Label();
		authCodeLbl.setVisible(false);

		balAmtLbl = new Label();
		balAmtLbl.setVisible(false);

		rspLayout.add(statusLbl, rspCodeLbl, authCodeLbl, balAmtLbl);
		return rspLayout;
	}

	private HorizontalLayout createHeader(String title) {
		HorizontalLayout header = new HorizontalLayout();
		header.setWidth("100%");
		header.setSpacing(false);
		header.setMargin(true);
		Label lbl = new Label(title);
		lbl.setSizeUndefined();
		header.addComponentAsFirst(lbl);
		return header;
	}
	
	
	private HorizontalLayout createTransactForm() {
		FormLayout leftFormLayout = new FormLayout();
		FormLayout rightFormLayout = new FormLayout();


		cardNumberField = createTxtField(app.getMessage("demo.cardNumber"));
		expDateField = createDateField(app.getMessage("demo.expDate"));

		amountField = createTxtField(app.getMessage("demo.Amount"));
		amountField.setValue("1.0");

		currencyField = createTxtField(app.getMessage("demo.Currency"));
		currencyField.setValue("840");
		
		cvvField = createOptionalTxtField(app.getMessage("demo.CVV"));
		
		pinField = createOptionalTxtField(app.getMessage("demo.PIN"));

		stanField = createTxtField(app.getMessage("demo.stan"));
		Date date = new Date();
		stanField.setValue(ISODate.formatDate(date, "HHmmss"));

		rrnField = createTxtField(app.getMessage("demo.rrn"));
		rrnField.setValue(ISODate.formatDate(date, "yyMMddHHmmss"));

		entryModeField = createPosEnvField(app.getMessage("demo.entryMode"));
		tranTypeField = createEntryTypeField(app.getMessage("demo.tranType"));
		termIdField = createTxtField(app.getMessage("demo.termId"));
		termIdField.setValue("tid001");
		midField = createTxtField(app.getMessage("demo.merchantId"));
		midField.setValue("000000001");
		merchDetField = createTxtField(app.getMessage("demo.merchantDetail"));
		merchDetField.setValue("Test merchant");

		

		txnBtn = new Button(app.getMessage("Send Txn"), event -> {
			// statusLbl.setValue("Operation in progress...");
			// statusLbl.setVisible(true);
			if (fieldsValid()) {
				initiateTxn();
			} else {
				this.app.getLog().error("Error", "Invalid input.");
			}
		});
//		txnBtn.setClickShortcut(ShortcutAction.KeyCode.ENTER);
//		txnBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
//		txnBtn.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txnBtn.setDisableOnClick(true);
		txnBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		HorizontalLayout hl = new HorizontalLayout();
		Label line = new Label("Request");
		line.setWidth(100f, Unit.PERCENTAGE);
		vl.add(line);
		Hr hr = new Hr();
		line.setWidth(100f, Unit.PERCENTAGE);
		vl.add(hr);
		
		hl.add(leftFormLayout, rightFormLayout);
		leftFormLayout.add(cardNumberField, expDateField, cvvField, pinField, stanField, rrnField,midField);// amountField, currencyField);
		rightFormLayout.add( amountField, currencyField,entryModeField, merchDetField, termIdField, tranTypeField, 
				tranTypeLabel, txnBtn);
		
		
		return hl;
	}

	private boolean fieldsValid() {
		if (!fieldValid(cardNumberField)) {
			return false;
		}
		if (!fieldValid(stanField)) {
			return false;
		}
		if (!fieldValid(rrnField)) {
			return false;
		}
		if (!fieldValid(amountField)) {
			return false;
		}
		if (!fieldValid(currencyField)) {
			return false;
		}
		if (!fieldValid(midField)) {
			return false;
		}
		if (!fieldValid(merchDetField)) {
			return false;
		}
		if (!fieldValid(termIdField)) {
			return false;
		}
		return !expDateField.isEmpty();
	}

	private boolean fieldValid(TextField field) {
		if (field.isEmpty()) {
			field.setErrorMessage(app.getMessage("errorMessage.invalidField"));
			return false;
		} else {
			return true;
		}
	}


	private void initiateTxn() {
		try {
			TranTypeData emd = tranTypeData.get(tranTypeField.getValue());
			String mti = "2200";
			if (emd != null) {
				mti = "2"+emd.getMti();
			}
			ISOMsg m = new ISOMsg(mti);

			//m.set(0, mti); // mti
			m.set(2, cardNumberField.getValue());
			if (emd != null) {
				m.set(3, emd.getTtc() + "0000"); // Processing Code
			}
//			Double amt = Double.parseDouble(amountField.getValue());
	//		Integer iamt = (int) (amt * 100);
		//	m.set(4, currencyField.getValue() + "2" + ISOUtil.zeropad(iamt.toString(), 12)); // Currency
			
			BigDecimal amount = new BigDecimal(amountField.getValue());
            m.set(new ISOAmount(4, Integer.parseInt(currencyField.getValue()), amount));
			
			if(cvvField.getValue() == null || cvvField.getValue().isEmpty() == false) {
				m.set("49.2", cvvField.getValue());
			}
			if(pinField.getValue() == null || pinField.getValue().isEmpty() == false) {
				m.set(52, ISOUtil.hex2byte(pinField.getValue()));
			}
			
			Date date = new Date();
			m.set(7, ISODate.getDateTime(date)); // date
			m.set(11, stanField.getValue()); // stan
			m.set(12, ISODate.formatDate(date, "yyyyMMddHHmmss"));
			LocalDate expDate = expDateField.getValue();
			String exp = "9912";
			if (expDate != null) {
				exp = expDate.format(DateTimeFormatter.ofPattern("yyMM"));
			}
			m.set(14, exp); // expiry date
			// m.set(15,getDate(date, "yyyyMMdd"));
			// m.set(17, getDate(date,"MMdd"));
			byte[] entMode = posEnvData.get(entryModeField.getValue());
			if (entMode != null) {
				m.set(22, entMode);
			}
			m.set(32, "00000000001");
			m.set(37, rrnField.getValue());
			m.set(41, termIdField.getValue());// tid
			m.set(42, midField.getValue()); // mid
			m.set("43.2", merchDetField.getValue()); // merchant Name
			// m.set("43.4", "Montevideo");
			// m.set("43.5", "MV");
			// m.set("43.7", "UY");
			// m.set(46, "07D84020000005000000001D840200000050");
			m.set("113.2", "106");
			m.set("113.25", "MINIATM");
			MUX mux =  NameRegistrar.getIfExists ("mux.jcard");
			// if it's a 2220, send a 2100 first.
			boolean send = true;
			if ("2220".equals(mti)) {
				ISOMsg m1 = (ISOMsg) m.clone();
				m1.setMTI("2100");
				ISOMsg r1 = mux.request(m1, 5000);

				if (r1 != null && r1.getString(39).equals("0000")) {
					m.set(25, "0000");
					m.set(38, r1.getString(38));
					// success
				} else {
					send = false;
					if (r1 == null) {
						setStatus("Transaction Timed-out");
					} else {
						setResponse(r1);
					}
				}
			}
			if (send) {
				ISOMsg r = mux.request(m, 5000);
				if (r == null) {
					setStatus("Transaction Timed-out");
				} else {
					setResponse(r);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			setStatus("Encountered unknown exception");
		}
	}

	private TextField createTxtField(String message) {
		TextField textField = new TextField(QIUtils.getCaptionFromId(message));
		textField.setWidth("100%");
		textField.setRequiredIndicatorVisible(true);
		textField.addValueChangeListener(event -> {
			resetScreen();
		});
		return textField;
	}
	
	private TextField createOptionalTxtField(String message) {
		TextField textField = new TextField(QIUtils.getCaptionFromId(message));
		textField.setWidth("100%");
		textField.setRequiredIndicatorVisible(false);
		textField.addValueChangeListener(event -> {
			resetScreen();			
		});
		return textField;
	}

	private DatePicker createDateField(String message) {
		DatePicker field = new DatePicker(QIUtils.getCaptionFromId(message));
		field.setWidth("100%");
		field.setRequiredIndicatorVisible(true);
		field.addValueChangeListener(event -> {
			resetScreen();
			LocalDate value = event.getValue();
			value = value.withDayOfMonth(value.lengthOfMonth());
			field.setValue(value);
		});
		return field;
	}

	private ComboBox<String> createPosEnvField(String message) {
		ComboBox<String> field = new ComboBox<>();
		field.setItems(posEnvData.keySet().stream());
		field.setValue(posEnvData.keySet().iterator().next());
		field.setLabel(QIUtils.getCaptionFromId(message));
		field.setRequiredIndicatorVisible(true);
		field.addValueChangeListener(event -> {
			resetScreen();
		});
		field.setWidth("100%");
		return field;
	}

	private ComboBox<String> createEntryTypeField(String message) {
		ComboBox<String> field = new ComboBox<>(QIUtils.getCaptionFromId(message));
		field.setItems(tranTypeData.keySet().stream());
		field.setPlaceholder(tranTypeData.keySet().iterator().next());
		field.setWidth("100%");
		field.setRequiredIndicatorVisible(true);
		field.addValueChangeListener(event -> {
			resetScreen();
			TranTypeData emd = tranTypeData.get(event.getValue());
			if (emd != null) {
				tranTypeLabel.setVisible(true);
				tranTypeLabel.setText("MTI: " + emd.getMti() + " Processing Code: " + emd.getTtc());
			}
		});
		field.setValue(tranTypeData.keySet().iterator().next());
		return field;
	}

	private void setStatus(String message) {
		Date date = new Date();
		if(stanField != null) {
			stanField.setValue(ISODate.formatDate(date, "HHmmss"));
		}
		if(rrnField != null) {
			rrnField.setValue(ISODate.formatDate(date, "yyMMddHHmmss"));
		}
		
		statusLbl.setVisible(true);
		rspCodeLbl.setVisible(false);
		authCodeLbl.setVisible(false);
		balAmtLbl.setVisible(false);
		statusLbl.setText(message);
	}

	private void resetScreen() {
		if (statusLbl != null)
			statusLbl.setVisible(false);

		if (rspCodeLbl != null)
			rspCodeLbl.setVisible(false);

		if (authCodeLbl != null)
			authCodeLbl.setVisible(false);

		if (balAmtLbl != null)
			balAmtLbl.setVisible(false);

		if (txnBtn != null)
			txnBtn.setEnabled(true);
		
	}

	private void setResponse(ISOMsg rsp) {
		Date date = new Date();
		if(stanField != null) {
			stanField.setValue(ISODate.formatDate(date, "HHmmss"));
		}
		if(rrnField != null) {
			rrnField.setValue(ISODate.formatDate(date, "yyMMddHHmmss"));
		}
		
		// statusLbl.setStyleName(ValoTheme.LABEL_LARGE);
		// statusLbl.setValue("Transaction Response");
		statusLbl.setVisible(false);

		String field = rsp.getString(39);
		if (field == null) {
			field = "--";
		}
		String desc = rsp.getString(63);
		if (desc == null) {
			rspCodeLbl.setText("Result Code(39) -> " + field);
		} else {
			rspCodeLbl.setText("Result Code(39) -> " + field + " [" + desc + "]");
		}
		rspCodeLbl.setVisible(true);

		field = rsp.getString(38);
		if (field == null) {
			field = "--";
		}
		authCodeLbl.setText("Approval Code(38) -> " + field);
		authCodeLbl.setVisible(true);

		field = rsp.getString(54);
		if (field == null) {
			field = "--";
			balAmtLbl.setText("Balance(54) -> " + field);
		} else {
			Integer numAmts = field.length() / 21;
			for (int acnt = 0; acnt < numAmts; ++acnt) {
				String actField = field.substring(acnt * 21, (acnt + 1) * 21);
				String amtType = actField.substring(2, 4);
				if (!"02".equals(amtType)) {
					continue;
				}
				String currCode = actField.substring(4, 7);
				Integer minorUnit = Integer.parseInt(actField.substring(7, 8));
				String type = actField.substring(8, 9);
				if ("C".equals(type)) {
					type = "Credit";
				} else {
					type = "Debit";
				}
				Double amount = Double.parseDouble(actField.substring(9, 21));
				for (int cnt = 0; cnt < minorUnit; ++cnt) {
					amount = amount / 10;
				}
				balAmtLbl.setText("Balance(54) -> " + " Currency: " + currCode + ", Amount: " + amount + " " + type);
			}
		}
		balAmtLbl.setVisible(true);
	}

	public void setConfiguration(Configuration cfg) {
		// dummy configuration override function.
	}
}
