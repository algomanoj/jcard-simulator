package org.jpos.qi.views.tranlog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jpos.core.Configuration;
import org.jpos.ee.CardHolder;
import org.jpos.ee.TranLog;
import org.jpos.ee.TranLogFilter;
import org.jpos.qi.QI;
import org.jpos.qi.ViewConfig;
import org.jpos.qi.ViewConfig.Position;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.services.TranLogHelper;
import org.jpos.qi.util.AmountConverter;
import org.jpos.qi.util.DateRange;
import org.jpos.qi.views.QIEntityView;
import org.jpos.qi.views.card.CardConverter;
import org.jpos.qi.views.minigl.GLEntriesGrid;
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
		// crud.getCrudFormFactory().setConverter("card", new CardConverter());
//        crud.getGrid().addSelectionListener((SelectionListener<Grid<TranLog>, TranLog>) selectionEvent -> {
//            UI ui = UI.getCurrent();
//            ui.getPage().fetchCurrentURL(currentUrl -> {
//                Optional optional = selectionEvent.getFirstSelectedItem();
//                if (optional.isPresent()) {
//                	
//                    ui.getPage().getHistory().pushState(null, getGeneralRoute());
//                }
//            });
//        });

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
			case ("localId"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return String.valueOf(tl.getLocalId());
				}, null);
				return field;
			}

			case ("pdc"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);

				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					if (tl.getPdc() != null) {
						return getHelper().getPdcAsString(tl.getPdc());
					} else {
						return "";
					}
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

			case ("node"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getNode() != null ? tl.getNode() : "";
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

			case ("batchNumber"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return String.valueOf(tl.getBatchNumber());
				}, null);
				return field;
			}

			case ("duration"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return String.valueOf(tl.getDuration());
				}, null);
				return field;
			}
			case ("outstanding"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> {
					TranLog tl = (TranLog) bean;
					return String.valueOf(tl.getOutstanding());
				}, null);
				return field;
			}
			case ("captureDate"): {
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
					return tl.getCaptureDate();
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
			case ("replacementAmount"): {
				TextField field = new TextField();
				field.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
				Binder.BindingBuilder builder = binder.forField(field);
				builder.withNullRepresentation("").withConverter(new AmountConverter(getApp().getMessage("errorMessage.NaN"))).bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getReplacementAmount();
				}, null);
				return field;
			}
			case ("additionalAmount"): {
				TextField field = new TextField();
				field.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
				Binder.BindingBuilder builder = binder.forField(field);
				builder.withNullRepresentation("").withConverter(new AmountConverter(getApp().getMessage("errorMessage.NaN"))).bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getAdditionalAmount();
				}, null);
				return field;
			}
			case ("acquirerFee"): {
				TextField field = new TextField();
				field.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
				Binder.BindingBuilder builder = binder.forField(field);
				builder.withNullRepresentation("").withConverter(new AmountConverter(getApp().getMessage("errorMessage.NaN"))).bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getAcquirerFee();
				}, null);
				return field;
			}
			case ("issuerFee"): {
				TextField field = new TextField();
				field.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
				Binder.BindingBuilder builder = binder.forField(field);
				builder.withNullRepresentation("").withConverter(new AmountConverter(getApp().getMessage("errorMessage.NaN"))).bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getIssuerFee();
				}, null);
				return field;
			}
			case ("returnedBalances"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.withNullRepresentation("").bind(bean -> {
					TranLog tl = (TranLog) bean;
					return tl.getReturnedBalances() != null ? tl.getReturnedBalances() : "";
				}, null);
				return field;
			}
			case ("glTransaction"): {
				return item.getGlTransaction() != null ? new GLEntriesGrid(item.getGlTransaction()) : null;
			}
//	            case ("tags"): {
//	                TextField field = new TextField();
//	                Binder.BindingBuilder<TranLog, String> builder = formatField(propertyId, field);
//	                builder.withNullRepresentation("").withConverter(new StringToTagConverter()).bind(propertyId);
//	                return field;
//	            }
			case ("card"):
			case ("card2"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.withNullRepresentation("").withConverter(new CardConverter())
					.bind(bean-> ((TranLog)bean).getCard(), null);
				return field;
			}
			case ("cardProduct"): {
				TextField field = new TextField();
				Binder.BindingBuilder builder = binder.forField(field);
				builder.bind(bean -> ((TranLog) bean).getCardProduct() != null ? ((TranLog) bean).getCardProduct().getName()
				: "", null);
				return field;
			}
//	            case ("additionalData"): {
//	                TextArea field = new TextArea();
//	                Binder.BindingBuilder builder = binder.forField(field);
//	                builder.bind(bean -> ((TranLog)bean).getAdditionalData() != null ?
//	                  ((TranLog)bean).getAdditionalData().toPrettyString() : "", null);
//	                return field;
//	            }
		}
		return null;
	}

	private void initiateGridColumns(Grid<TranLog> grid) {
		grid.removeAllColumns();
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
	}

	@Override
	public Component initContent() {
		currencyCodeFilterField.setPlaceholder("Filter by currency code...");
		currencyCodeFilterField.setClearButtonVisible(true);
		currencyCodeFilterField.setValueChangeMode(ValueChangeMode.EAGER);
		// currencyCodeFilterField.addValueChangeListener(e -> updateList());
		Component component = super.initContent();

		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(getApp().getMessage(getName()));
		viewTitle.addClassNames("mt-s", "text-l");
		crud = createCrud();
		crud.getFindAllButton().addClickListener(e -> {
			tranLogSearchComponent.resetFilter();
		});
		tranLogSearchComponent = new TranLogSearchComponent(defaultDateRange.getRange(), layers, viewTitle, getHelper(),
		vl) {
			@Override
			protected void refresh() {
				System.out.println(" refresh called ");
				refreshGrid(getValue());
			}
		};
		vl.add(/* viewTitle, */ tranLogSearchComponent, crud);
		if (getEntityId() != null)
			setBean((TranLog) getHelper().getEntityById(getEntityId()));
		if (getBean() != null)
			crud.getGrid().select(getBean());
		return vl;
	}

	public void refreshGrid(TranLogFilter filter) {
		ConfigurableFilterDataProvider wrapper = (ConfigurableFilterDataProvider) crud.getGrid().getDataProvider();
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
}
