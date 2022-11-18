package org.jpos.qi.views.demo;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jetty.io.ssl.SslConnection.DecryptedEndPoint;
import org.jpos.core.Configuration;
import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.TranLog;
import org.jpos.iso.ISOAmount;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
//import org.jpos.ee.TranLogFilter;
import org.jpos.qi.QI;
import org.jpos.qi.ViewConfig;
import org.jpos.qi.ViewConfig.Position;
import org.jpos.qi.core.ee.TranLogFilter;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.SearchHelper;
import org.jpos.qi.services.TranLogHelper;
import org.jpos.qi.util.AmountConverter;
import org.jpos.qi.util.DateRange;
import org.jpos.qi.views.QIEntityView;
import org.jpos.qi.views.card.CardConverter;
import org.jpos.qi.views.demo.TransactView.TranTypeData;
import org.jpos.qi.views.minigl.GLEntriesGrid;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKeyStore;
import org.jpos.security.SecureKeyStore.SecureKeyStoreException;
import org.jpos.security.jceadapter.SSM;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToDateConverter;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

public class TranLogView extends QIEntityView<TranLog> {
	private TextField currencyCodeFilterField = new TextField();
	GridCrud<TranLog> crud;
	private TranLogSearchComponent tranLogSearchComponent;
	ConfigurableFilterDataProvider filteredDataProvider;
	private short[] layers;
	private DateRange defaultDateRange = new DateRange(DateRange.TODAY);
	SearchHelper searchHelper;
	
	public TranLogView() {
		super(TranLog.class, "tranlog");
	}

	@Override
	public GridCrud<TranLog> createCrud() {
		crud = super.createCrud();
		crud.setAddOperationVisible(false);
		crud.setUpdateOperationVisible(false);
		crud.setDeleteOperationVisible(false);
		crud.setClickRowToUpdate(false);

		initiateGridColumns(crud.getGrid());
		filteredDataProvider = getHelper().getDataProvider();
		crud.getGrid().setItems(filteredDataProvider);
		loadLayersFromConfig();
		Object sessionDateRange = VaadinSession.getCurrent().getAttribute("tranlog-daterange");
		defaultDateRange = sessionDateRange instanceof DateRange ? ((DateRange) sessionDateRange) : defaultDateRange;
		crud.getGrid().addItemClickListener(e -> {
			TranLog item = (TranLog) getHelper().getEntityById(e.getItem().getId().toString());
			FormLayout hLayout = createTranLogDetailsComponent(item);
			String caption= getApp().getMessage("tranlog.header")+": "+item.getId() + (item.getItc() != null ? " - "+ item.getItc() : "");
			crud.getCrudLayout().showDialog(caption, hLayout);
		});

		return crud;
	}

	private FormLayout createTranLogDetailsComponent(TranLog item) {
		FormLayout formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("500px", 1));
		FormLayout leftLayout = new FormLayout();
		leftLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("500px", 1));
		leftLayout.setWidth("100%");
		FormLayout rightLayout = new FormLayout();
		rightLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("500px", 1));
		rightLayout.setWidth("100%");
		HorizontalLayout hl = new HorizontalLayout(leftLayout, rightLayout);
		hl.setWidth("100%");
		hl.setMargin(false);
		formLayout.add(hl);
		Map<String, String> sectionsTitle = new HashMap<>() {
			{
				put("id", getApp().getMessage("requestData"));
				put("irc", getApp().getMessage("responseData"));
				put("amount", getApp().getMessage("amountData"));
				put("cardHolderId", getApp().getMessage("additionalData"));
			}
		};
		Binder<TranLog> binder = new Binder<>(TranLog.class);
		for (String id : getViewConfig().getVisibleFields()) {
			ViewConfig.FieldConfig fieldConfig = getViewConfig().getFields().get(id);
			Component c = buildAndBindCustomComponent(id, binder, item);
			if (c != null) {
				if (fieldConfig.getPosition().equals(Position.RIGHT)) {
					if (sectionsTitle.containsKey(id)) {
						Label heading = new Label(sectionsTitle.get(id));
						heading.addClassName("bold-label");
						heading.setWidthFull();
						rightLayout.add(heading);
					}
					rightLayout.addFormItem(c, QI.getQI().getMessage("tranlog." + id)).addClassName("formItemPos");
					rightLayout.addClassName("formItemLabel");
					
					// rightLayout.setColspan(c, fieldConfig.getExpandRatio());
				} else if (fieldConfig.getPosition().equals(Position.BOTTOM)) {
					if (sectionsTitle.containsKey(id)) {
						Label heading = new Label(sectionsTitle.get(id));
						heading.addClassName("bold-label");
						heading.setWidthFull();
						formLayout.add(heading);
					}
					formLayout.addFormItem(c, QI.getQI().getMessage("tranlog." + id)).addClassName("formItemPos");
					formLayout.addClassName("formItemLabel");
				} else { //Add in left layout by default
					if (sectionsTitle.containsKey(id)) {
						Label heading = new Label(sectionsTitle.get(id));
						heading.addClassName("bold-label");
						heading.setWidthFull();
						leftLayout.add(heading);
					}
					leftLayout.addFormItem(c, QI.getQI().getMessage("tranlog." + id)).addClassName("formItemPos");
					leftLayout.addClassName("formItemLabel");
					// leftLayout.setColspan(c, fieldConfig.getExpandRatio());
				}
			}
		}
		binder.readBean(item);
		return formLayout;
	}

	protected Component buildAndBindCustomComponent(String propertyId, Binder binder, TranLog item) {
		switch (propertyId) {
			case ("id"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getId().toString();
				}, null);
				return field;
			}

			case ("date"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				StringToDateConverter dateConverter = new StringToDateConverter() {
					@Override
					protected DateFormat getFormat(Locale locale) {
						return new SimpleDateFormat(QI.getQI().getMessage("dateformat"));
					}
				};
				builder.withNullRepresentation("").withConverter(dateConverter).bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getDate();
				}, null);
				return field;
			}
			case ("itc"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					if (tl.getItc() != null) {
						String itc = tl.getItc();
//	                        return itc + " " + ((TranLogHelper)getHelper()).getFriendlyItc(itc, tl.getReasonCode());
						return itc;
					} else {
						return "";
					}
				}, null);
				return field;
			}

			case ("stan"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					if (tl.getStan() != null) {
						return tl.getStan();
					} else {
						return "";
					}
				}, null);
				return field;
			}

			case ("acquirer"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getAcquirer() != null ? tl.getAcquirer() : "";
				}, null);
				return field;
			}

			case ("mid"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getMid() != null ? tl.getMid() : "";
				}, null);
				return field;
			}

			case ("tid"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getTid() != null ? tl.getTid() : "";
				}, null);
				return field;
			}
		
			case ("irc"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return String.format("%s (%s)", tl.getIrc(), tl.getRc());
				}, null);
				return field;
			}

			case ("approvalNumber"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getApprovalNumber() != null ? tl.getApprovalNumber() : "";
				}, null);
				return field;
			}

			case ("displayMessage"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getDisplayMessage() != null ? tl.getDisplayMessage() : "";
				}, null);
				return field;
			}

			case ("rrn"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getRrn() != null ? tl.getRrn() : "";
				}, null);
				return field;
			}

			case ("responseCode"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getResponseCode() != null ? tl.getResponseCode() : "";
				}, null);
				return field;
			}

			case ("rc"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getRc() != null ? tl.getRc() : "";
				}, null);
				return field;
			}

			case ("cardHolderId"): {
				String url = getViewConfig().getFields().get(propertyId).getLink();
				Anchor field = new Anchor();
				CardHolder cardHolder = item.getCardHolder();
				if (cardHolder != null && cardHolder.getId() != null) {
					field.setHref(getGeneralRoute() + "/" + url + "/" + item.getCardHolder().getId().toString());

				}
				return field;
			}
			case ("accountCode"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getAccount() != null ? tl.getAccount().getCode() : "";
				}, null);
				return field;
			}
			case ("caName"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getCardAcceptor() != null ? tl.getCardAcceptor().getName() : "";
				}, null);
				return field;
			}

		
			case ("ss"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getSs() != null ? tl.getSs() : "";
				}, null);
				return field;
			}
			case ("transmissionDate"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				StringToDateConverter dateConverter = new StringToDateConverter() {
					@Override
					protected DateFormat getFormat(Locale locale) {
						return new SimpleDateFormat(QI.getQI().getMessage("dateformat"));
					}
				};
				builder.withNullRepresentation("").withConverter(dateConverter).bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getTransmissionDate();
				}, null);
				return field;
			}
			case ("localTransactionDate"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				StringToDateConverter dateConverter = new StringToDateConverter() {
					@Override
					protected DateFormat getFormat(Locale locale) {
						return new SimpleDateFormat(QI.getQI().getMessage("dateformat"));
					}
				};
				builder.withNullRepresentation("").withConverter(dateConverter).bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getLocalTransactionDate();
				}, null);
				return field;
			}
			case ("amount"): {
				TextField field = new TextField();
				field.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
				Binder.BindingBuilder builder = binder.forField(field);
				builder.withNullRepresentation("").withConverter(new AmountConverter(getApp().getMessage("errorMessage.NaN"))).bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getAmount();
				}, null);
				return field;
			}
			case ("currencyCode"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getCurrencyCode() != null ? tl.getCurrencyCode() : "";
				}, null);
				return field;
			}
			
			case ("card"):
			case ("card2"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.withNullRepresentation("").withConverter(new CardConverter())
					.bind(bean-> ((TranLog)bean).getCard(), null);
				return field;
			}
			
		}
		return null;
	}

	private void initiateGridColumns(Grid<TranLog> grid) {
		grid.removeAllColumns();
		grid.setSelectionMode(Grid.SelectionMode.MULTI);
		grid.addColumn(TranLog::getId).setKey("id").setSortable(true).setHeader("Id");
		grid.addColumn(TranLog::getDate).setKey("date").setHeader("Date");
		grid.addColumn(TranLog::getItc).setKey("itc").setHeader("ITC");
		grid.addColumn(tranlog -> tranlog.getCard() != null
		? tranlog.getCard().getBin() + "..." + tranlog.getCard().getLastFour()
		: "").setKey("card").setHeader("Card");

		grid.addColumn(TranLog::getMid).setKey("mid").setHeader("MID");
		grid.addColumn(TranLog::getIrc).setKey("irc").setHeader("IRC");

		grid.addColumn(TranLog::getApprovalNumber).setKey("approvalNumber").setHeader("Approval No");
		grid.addColumn(TranLog::getDisplayMessage).setKey("displayMessage").setHeader("Message");
		grid.addColumn(TranLog::getAmount).setKey("amount").setHeader("Amount");
		grid.addColumn(TranLog::getCurrencyCode).setKey("currencyCode").setHeader("Currency Code");
		grid.addColumn(TranLog::getRrn).setKey("rrn").setHeader("RRN");
	}

	@Override
	public Component initContent() {
		currencyCodeFilterField.setPlaceholder("Filter by currency code...");
		currencyCodeFilterField.setClearButtonVisible(true);
		currencyCodeFilterField.setValueChangeMode(ValueChangeMode.EAGER);
		Component component = super.initContent();

		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(getApp().getMessage(getName()));
		viewTitle.addClassNames("mt-s", "text-l");
		crud = createCrud();
		crud.getContent().getStyle().set("margin-top", "-60px");
		crud.getFindAllButton().addClickListener(e -> {
			tranLogSearchComponent.resetFilter();
		});
		tranLogSearchComponent = new TranLogSearchComponent(layers, viewTitle, getHelper(), vl) {
			@Override
			protected void refresh() {
				TranLogFilter tranLogFilter = getValue();
				tranLogFilter.setApproved(true);
				refreshGrid(tranLogFilter);
			}
			@Override
			void changeTransectionStatus(String transectionStatus) {
				changeTranStatus(transectionStatus);
			}
		};
		vl.add(/* viewTitle, */ tranLogSearchComponent, crud);
		if (getEntityId() != null)
			setBean((TranLog) getHelper().getEntityById(getEntityId()));
		if (getBean() != null)
			crud.getGrid().select(getBean());
		return vl;
	}
	private void changeTranStatus(String transectionStatus) {
		for (TranLog tranLog : crud.getGrid().getSelectedItems()) {
			initiateTxn(tranLog, transectionStatus);
		}
	}
	
	private void initiateTxn(TranLog tranLog, String transectionStatus) {
		try {
			String mti = null;
			if("refund".equalsIgnoreCase(transectionStatus)) {
				mti = "2100";
			} if("reversal".equalsIgnoreCase(transectionStatus)) {
				mti = "2420";
			} if("completion".equalsIgnoreCase(transectionStatus)) {
				mti = "2220";
			}
			ISOMsg m = new ISOMsg(mti);

			Map smap = getCardSecureMap(tranLog.getCard());
			m.set(2, (String)smap.get("P"));

			String itc = tranLog.getItc();
			m.set(3,  itc.substring(itc.indexOf(".")) + "0000"); // Processing Code
			
			BigDecimal amount = tranLog.getAmount();
            m.set(new ISOAmount(4, Integer.parseInt(tranLog.getCurrencyCode()), amount));
			
			Date date = tranLog.getTransmissionDate();
			m.set(7, ISODate.getDateTime(date)); // date
			m.set(11, tranLog.getStan()); // stan
			m.set(12, ISODate.formatDate(date, "yyyyMMddHHmmss"));
			LocalDate expDate = tranLog.getCard().getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();// expDateField.getValue();
			String exp = "9912";
			if (expDate != null) {
				exp = expDate.format(DateTimeFormatter.ofPattern("yyMM"));
			}
			m.set(14, exp); // expiry date
			m.set(22, tranLog.getPdc());
			m.set(32, "00000000001");
			m.set(37, tranLog.getRrn());
			m.set(41, tranLog.getTid() );
			m.set(42, tranLog.getMid());
			if(tranLog.getCardAcceptor()!=null) {
				ISOMsg f43 = new ISOMsg(43);
				f43.set(2, tranLog.getCardAcceptor().getName());
				f43.set(4, tranLog.getCardAcceptor().getCity());
				f43.set(5, tranLog.getCardAcceptor().getRegion());
				f43.set(6, tranLog.getCardAcceptor().getPostalCode());
				f43.set(7, tranLog.getCardAcceptor().getCountry());
				m.set(f43);
				
			}
			m.set("113.2", "106");
			m.set("113.25", "MINIATM");
			MUX mux =  NameRegistrar.getIfExists ("mux.jcard");

			ISOMsg resp = mux.request(m, 5000);
			if(resp!=null) {
				String authCode = resp.getString(38);
				String respCode = resp.getString(39);
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	
	public void refreshGrid(TranLogFilter filter) {
		ConfigurableFilterDataProvider wrapper = (ConfigurableFilterDataProvider) crud.getGrid().getDataProvider();
		if(tranLogSearchComponent.getDatePicker()!=null && tranLogSearchComponent.getDatePicker().getValue()!=null) {
			LocalDate lclDate = tranLogSearchComponent.getDatePicker().getValue();
			Date date = Date.from(lclDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DAY_OF_YEAR, 1);
			cal.add(Calendar.SECOND, -1);
			filter.setStart(date);
			filter.setEnd(cal.getTime());
			System.out.println("Filter Start:"+date+" End:"+cal.getTime());
		}
		if(tranLogSearchComponent.getPanField()!=null && tranLogSearchComponent.getPanField().getValue()!=null 
				&& !"".equals(tranLogSearchComponent.getPanField().getValue().trim())) {
			Card card=null;
			try {
				if(searchHelper==null) 
					searchHelper = new SearchHelper();
				card = searchHelper.getCardByNumber(tranLogSearchComponent.getPanField().getValue().trim());
			} catch (SMException | NotFoundException e) {
				e.printStackTrace();
			}
			if(card==null) {
				filter.setId("-1");
			} else {
				filter.setCard(card);
			}
		}
		if(tranLogSearchComponent.getRrnField()!=null && tranLogSearchComponent.getRrnField().getValue()!=null 
				&& !"".equals(tranLogSearchComponent.getRrnField().getValue().trim())) {
			filter.setRrn(tranLogSearchComponent.getRrnField().getValue().trim());
		}
		wrapper.setFilter(filter);
		wrapper.refreshAll();
	}

	@Override
	public QIHelper createHelper() {
		return new TranLogHelper(getViewConfig());
	}

	public TranLogHelper getHelper() {
		return (TranLogHelper) super.getHelper();
	}

	private void loadLayersFromConfig() {
		Configuration config = getViewConfig().getConfiguration();
		String[] layersConfigs = config.getAll("layer");
		layers = new short[layersConfigs.length];
		int count = 0; 
		for (String s : layersConfigs) {
			try {
				layers[count] = Short.parseShort(s);
				count++;
			} catch (NumberFormatException exc) {
				getApp().getLog().error(exc.getMessage());
			}
		}
	}

	@Override
	public void onAttach(AttachEvent event) {
		tranLogSearchComponent.getRefreshButton().click();
	}
	
	private SSM getSSM() throws NameRegistrar.NotFoundException {
		return (SSM) NameRegistrar.get("ssm");
	}

	protected SecureDESKey getBDK(String bdkName) throws SMException, SecureKeyStoreException {
		try {
			SecureKeyStore ks = NameRegistrar.get("ks");
			return ks.getKey(bdkName);
		} catch (NotFoundException e) {
			throw new SMException(e.getMessage());
		}
	}

	protected Map getCardSecureMap(Card card) throws NotFoundException, SecureKeyStoreException, SMException {
		Map secureMap = card.getSecureMap();
		if (secureMap == null) {
			secureMap = getSSM().customDecryptMap(getBDK(card.getKid()), card.getSecureData());
			card.setSecureMap(secureMap);
		}
		return secureMap;
	}
}
