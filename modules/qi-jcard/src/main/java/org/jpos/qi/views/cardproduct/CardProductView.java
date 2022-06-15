package org.jpos.qi.views.cardproduct;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import org.jpos.ee.BLException;
import org.jpos.ee.CardProduct;
import org.jpos.ee.Fee;
import org.jpos.ee.Issuer;
import org.jpos.ee.SysConfig;
import org.jpos.ee.TransactionType;
import org.jpos.ee.VelocityProfile;
import org.jpos.gl.Account;
import org.jpos.qi.QIUtils;
import org.jpos.qi.services.CardProductHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.util.NotificationUtils;
import org.jpos.qi.util.SysConfigComboBox;
import org.jpos.qi.views.QIEntityView;
import org.jpos.qi.views.minigl.AccountConverter;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;

/**
 * Created by jr on 9/29/15.
 */
@SuppressWarnings("unchecked")
public class CardProductView extends QIEntityView<CardProduct> {

	private static final long serialVersionUID = -4585753567332962027L;

	private ComboBox<Issuer> issuerComboBox;
	private TextField codeField;
	private TextField issuedAccountField;
	private TextField feeAccountField;
	private TextField lossesAccountField;
	private GridCrud<CardProduct> crud;
	private ComboBox<SysConfig> schemeComboxBox;
	ConfigurableFilterDataProvider filteredDataProvider;
	DataProvider<CardProduct, String> dataProvider;
	private String selectedScheme;
	private TextField panStartField;
	private TextField panEndField;
	private TextField binField;
	private TextField panLengthField;
	
	private ComboBox<String>  schemeCrudCombo;
	private CheckboxGroup<Fee> feeListBox;
	private CheckboxGroup<VelocityProfile> velocityProfileListBox = new CheckboxGroup<>();

	private VerticalLayout vl;
	private Notification errorNotification;
	
	public CardProductView() {
		super(CardProduct.class, "cardproducts");
	}

	@Override
	public GridCrud<CardProduct> createCrud() {
		crud = super.createCrud();
//		String[] captions = {  null, null, null,  getApp().getMessage("virtual", null),null, null, getApp().getMessage("expiryDate", null), 
//				null };
		String[] captions = {  getApp().getMessage("cardproduct.scheme"),   getApp().getMessage("cardproduct.panLength"),
				getApp().getMessage("cardproduct.name"), getApp().getMessage("cardproduct.bin"),
				 getApp().getMessage("cardproduct.cardType"), getApp().getMessage("cardproduct.panStart"), 
				 getApp().getMessage("cardproduct.expAfterMonth"),getApp().getMessage("cardproduct.panEnd"),
				 getApp().getMessage("cardproduct.issuer"),getApp().getMessage("cardproduct.code"), 
				 getApp().getMessage("cardproduct.issuedAccount"), getApp().getMessage("cardproduct.feeAccount"), 
				 getApp().getMessage("cardproduct.lossesAccount"), getApp().getMessage("cardproduct.externalAccount"),
				 getApp().getMessage("cardproduct.active"), getApp().getMessage("cardproduct.renewable"), 
				 getApp().getMessage("cardproduct.velocityProfiles") , getApp().getMessage("cardproduct.fees"),
				 getApp().getMessage("cardproduct.transactionTypes")
				 };
		crud.getCrudFormFactory().setFieldCaptions(captions);
		dataProvider = getHelper().getDataProvider();
		filteredDataProvider = dataProvider.withConfigurableFilter();
		crud.getGrid().setDataProvider(filteredDataProvider);

		initiateGrid(crud.getGrid());

		setCustomField();

		setCrudOperation();

		crud.getAddButton().addClickListener(e -> {
			if (schemeComboxBox.getValue() == null) {
				schemeComboxBox.setReadOnly(false);
				crud.getCrudLayout().hideForm();
				NotificationUtils.getInstance().showErrorNotification(getApp().getMessage("cardproduct.scheme.notselected"), this.errorNotification);
			}else {
				schemeCrudCombo.setReadOnly(true);
				List<String> sys=new ArrayList<>();
				SysConfig conf=schemeComboxBox.getValue();
				sys.add(conf.getValue());
				schemeCrudCombo.setItems(sys);
				schemeCrudCombo.setValue(conf.getValue());
			} 
		});

		crud.getFindAllButton().addClickListener(e -> {
			schemeComboxBox.setValue(null);
			schemeCrudCombo.setValue(null);
		});

		return crud;
	}

	private void setCrudOperation() {
		crud.setAddOperation(cardProduct -> getHelper().saveEntity(cardProduct));
		crud.setUpdateOperation(cardProduct -> {
			try {
				return getHelper().updateEntity(cardProduct);
			} catch (BLException e) {
				e.printStackTrace();
				return null;
			}
		});

	}

	private void setCustomField() {
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, 
				"scheme","panLength",
				"name",  "bin",
				"cardType", "panStart",
				"expAfterMonth", "panEnd",
				"issuer","code", 
				"issuedAccount", "feeAccount",
				"lossesAccount", "externalAccount",
				"active", "renewable",
				"velocityProfiles","fees",
				"transactionTypes");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE,
				"scheme","panLength",
				"name",  "bin",
				"cardType", "panStart",
				"expAfterMonth", "panEnd",
				"issuer","code", 
				"issuedAccount", "feeAccount",
				"lossesAccount", "externalAccount",
				"active", "renewable",
				"velocityProfiles","fees",
				"transactionTypes");
		
		crud.getCrudFormFactory().setFieldProvider("issuer", new ComboBoxProvider<>("issuers", getHelper().getIssuers(),
				null, (ItemLabelGenerator<Issuer>) issuer -> issuer != null ? issuer.getName() : ""));
		crud.getCrudFormFactory().setFieldType("code", TextField.class);
		crud.getCrudFormFactory().setFieldType("issuedAccount", TextField.class);
		crud.getCrudFormFactory().setFieldType("feeAccount", TextField.class);
		crud.getCrudFormFactory().setFieldType("lossesAccount", TextField.class);
		crud.getCrudFormFactory().setConverter("issuedAccount", new AccountConverter(true, true, true));
		crud.getCrudFormFactory().setConverter("feeAccount", new AccountConverter(true, true, true));
		crud.getCrudFormFactory().setConverter("lossesAccount", new AccountConverter(true, true, true));

		crud.getCrudFormFactory().setFieldType("panStart", TextField.class);
		crud.getCrudFormFactory().setFieldType("panEnd", TextField.class);
		crud.getCrudFormFactory().setFieldType("bin", TextField.class);
		crud.getCrudFormFactory().setFieldType("panLength", TextField.class);
		crud.getCrudFormFactory().setFieldType("expAfterMonth", TextField.class);
		crud.getCrudFormFactory().setFieldCreationListener(CrudOperation.ADD, "panLength", panLen -> panLen.setValue("16"));
		crud.getCrudFormFactory().setFieldCreationListener(CrudOperation.ADD, "expAfterMonth", exp -> exp.setValue("36"));
		
		crud.getCrudFormFactory().setFieldCreationListener("issuer", field -> {
			issuerComboBox = (ComboBox) field;
			issuerComboBox.addValueChangeListener(event -> {
				if (codeField != null && codeField.getValue() != null && !"".equals(codeField.getValue())) {
					preLoadAccountFieldsWithCode(issuerComboBox.getValue(), codeField.getValue());
				}
			});
		});

		crud.getCrudFormFactory().setFieldCreationListener("code", field -> {
			codeField = (TextField) field;
			codeField.addValueChangeListener(event -> {
				if (issuerComboBox != null && issuerComboBox.getValue() != null) {
					System.out.println("=====> CodeField valuechangelistener");
					preLoadAccountFieldsWithCode(issuerComboBox.getValue(), event.getValue());
				}
			});
		});
		crud.getCrudFormFactory().setFieldCreationListener("issuedAccount",
			field -> issuedAccountField = (TextField) field);

		crud.getCrudFormFactory().setFieldCreationListener("feeAccount",
			field -> feeAccountField = (TextField) field);

		crud.getCrudFormFactory().setFieldCreationListener("lossesAccount",
			field -> lossesAccountField = (TextField) field);

		crud.getCrudFormFactory().setFieldProvider("cardType", () -> createCardTypeCombo("cardType"));
		
		crud.getCrudFormFactory().setFieldProvider("scheme", this::createCrudSchemeCombo);
		
		crud.getCrudFormFactory().setFieldCreationListener("scheme",
			field -> schemeCrudCombo = (ComboBox<String>) field);
		
		
		CheckboxGroup<String> f =createTransactionTypeCombo("transactionTypes");
		crud.getCrudFormFactory().setFieldProvider("transactionTypes", () -> f);
		f.setMaxWidth("100%");

		feeListBox = new CheckboxGroup<>();
		feeListBox.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		crud.getCrudFormFactory().setFieldProvider("fees", ()-> feeListBox);
		feeListBox.setItems(getHelper().getFees());
		feeListBox.setItemLabelGenerator(((ItemLabelGenerator<Fee>) fee -> fee != null ? fee.getType() : ""));
//		feeListBox.setMaxWidth("100%");
//		feeListBox.setWidth("calc(47.5% - 0.75rem) !important");
//		feeListBox.setHeight("210px");
//		feeListBox.getElement().getStyle().set("margin-right", "30px");

		velocityProfileListBox = new CheckboxGroup<>();
		velocityProfileListBox.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		crud.getCrudFormFactory().setFieldProvider("velocityProfiles", ()-> velocityProfileListBox);
		velocityProfileListBox.setItems(getHelper().getVelocityProfiles());
		velocityProfileListBox.setItemLabelGenerator(((ItemLabelGenerator<VelocityProfile>) velocityProfile -> velocityProfile != null ? velocityProfile.getName() : ""));
//		velocityProfileListBox.setMaxWidth("100%");
//		velocityProfileListBox.setWidth("calc(47.5% - 0.75rem) !important");
//		velocityProfileListBox.getElement().setProperty("label","velocityProfiles");
//		velocityProfileListBox.setHeight("210px");
		
		crud.getCrudFormFactory().setFieldCreationListener("panStart", field -> {
			panStartField = (TextField) field;
			panStartField.addValueChangeListener(event -> {
				try {
                    if (event.getValue() == null) {
                        field.setValue(binField.getValue());
                    } else {
                        String value = event.getValue();
                        if (!value.startsWith(binField.getValue())) {
                            field.setValue(binField.getValue());
                            return;
                        }

                        isBinHighLowValid();
                    }
                } catch (Exception e) {
                    getApp().getLog().error(e.getMessage());
                }
			});
		});
		
		crud.getCrudFormFactory().setFieldCreationListener("panEnd", field -> {
			panEndField = (TextField) field;
			panEndField.addValueChangeListener(event -> {
				try {
                    if (event.getValue() == null) {
                        field.setValue(binField.getValue());
                    } else {
                        String value = event.getValue();
                        if (!value.startsWith(binField.getValue())) {
                            field.setValue(binField.getValue());
                            return;
                        }

                        isBinHighLowValid();
                    }
                } catch (Exception e) {
                    getApp().getLog().error(e.getMessage());
                }
			});
		});
		
		crud.getCrudFormFactory().setFieldCreationListener("bin", field -> {
			binField = (TextField) field;
			binField.addValueChangeListener(event -> {
				try {
                    String value = event.getValue();
                    if (value.length() != 6) {
                    	panStartField.clear();
                    	panEndField.clear();
                        return;
                    }
                	resetBinHighLow();
                } catch (Exception e) {
                    getApp().getLog().error(e.getMessage());
                }
			});
		});
		
		crud.getCrudFormFactory().setFieldCreationListener("panLength", field -> {
			panLengthField = (TextField) field;
			panLengthField.setValue("16");
			panLengthField.addValueChangeListener(event -> {
				int cLen = Integer.parseInt(event.getValue());
				if (cLen < 16 || cLen > 19) {
					panLengthField.setInvalid(true);
					panLengthField.setErrorMessage(getApp().getMessage("CARD_NUM_LEN_ERROR"));
				}else{
					try {
						resetBinHighLow();
					} catch (Exception e) {
						panStartField.clear();
						panEndField.clear();
						getApp().getLog().error(e.getMessage());
					}
				}
				
			});
		});
		
	}
	
	private boolean isBinHighLowValid() {
        try {
            long hBin = Long.parseLong(panEndField.getValue());
            long lBin = Long.parseLong(panStartField.getValue());
            if (hBin < lBin) {
            	panEndField.setInvalid(true);
            	panStartField.setInvalid(true);
            	panEndField.setErrorMessage(getApp().getMessage("BIN_HIGH_LOW_ERROR"));
            	panStartField.setErrorMessage(getApp().getMessage("BIN_HIGH_LOW_ERROR"));
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
	
	private void resetBinHighLow() {
    	if(panLengthField == null || binField == null) {
    		return;
    	}
        int cardLen = Integer.parseInt(panLengthField.getValue());
        if (panStartField != null && binField.getValue() != null) {
        	// Just to check format.
        	Integer.parseInt(binField.getValue());
    		StringBuilder sb = new StringBuilder();
            sb.append(binField.getValue());
            while (sb.length() < cardLen) {
                sb.append('0');
            }
            panStartField.setValue(sb.toString());
        }
        if (panEndField != null && binField.getValue() != null) {
    		StringBuilder sb = new StringBuilder();
            sb.append(binField.getValue());
            while (sb.length() < cardLen) {
                sb.append('9');
            }
            panEndField.setValue(sb.toString());
        }
    }
	
	private ComboBox<String> createCrudSchemeCombo() {
		ComboBox<String> field = new ComboBox(QIUtils.getCaptionFromId("field." + "scheme"));
		field.setItemLabelGenerator(
				(ItemLabelGenerator<String>) captionGenerator -> captionGenerator != null ? captionGenerator : "");
		field.setReadOnly(true);
		try {
			List<String> schemeList = new ArrayList<String>();
			field.setItems(schemeList);
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return field;
	}

	private CheckboxGroup<String> createTransactionTypeCombo(String propertyId) {
		
		CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
		checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		checkboxGroup.setLabel("Select Transaction Type");
		Set<String> transactionTypeList = Stream.of(TransactionType.values())
               .map(TransactionType::name)
                .collect(Collectors.toSet());
		checkboxGroup.setItems(transactionTypeList);
		return checkboxGroup;
	}

	private ComboBox<String> createCardTypeCombo(String propertyId) {
		ComboBox<String> field = new ComboBox(QIUtils.getCaptionFromId("field." + propertyId));
		field.setItemLabelGenerator(
				(ItemLabelGenerator<String>) captionGenerator -> captionGenerator != null ? captionGenerator : "");
		field.setReadOnly(true);
		try {
			List<String> cardtypes = new ArrayList<>();
			cardtypes.add("Credit Card");
			cardtypes.add("Debit Card");
			cardtypes.add("Charge Card");
			cardtypes.add("ATM Card");
			cardtypes.add("Stored-value Card");
			cardtypes.add("Fleet Card");
			cardtypes.add("Gift Card");
			cardtypes.add("Others");

			field.setItems(cardtypes);
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return field;

	
	}

	private void initiateGrid(Grid<CardProduct> grid) {
		grid.removeAllColumns();
		grid.addColumn(CardProduct::getId)
			.setHeader(getApp().getMessage("issuers.id"))
			.setSortable(true)
			.setKey("id");
		grid.addColumn(cardP -> cardP.getIssuer() != null ? cardP.getIssuer().getName() : "")
			.setHeader(getApp().getMessage("issuers.issuer"))
			.setSortable(true)
			.setKey("issuer");
		grid.addColumn(CardProduct::getExternalAccount)
			.setHeader(getApp().getMessage("issuers.externalAccount"))
			.setSortable(true)
			.setKey("externalAccount");
		grid.addColumn(CardProduct::getName).setHeader(getApp().getMessage("issuers.name")).setSortable(true)
				.setKey("name");
		grid.addColumn(CardProduct::isActive).setHeader(getApp().getMessage("issuers.active")).setSortable(true)
				.setKey("active");
		grid.addColumn(CardProduct::getCode).setHeader(getApp().getMessage("issuers.code")).setSortable(true)
				.setKey("code");
	}

	private void preLoadAccountFieldsWithCode(Issuer issuer, String code) {
		try {
			if (isFieldEditable(issuedAccountField)) {
				Account root = getHelper().getIssuersAssetsAccount(issuer);
				if (root != null)
					issuedAccountField.setValue(root.getCode() + "." + code);
				else
					issuedAccountField.setErrorMessage("Issuer does not have an Assets Account set");
			}
			if (isFieldEditable(feeAccountField)) {
				Account root = getHelper().getIssuersEarningsAccount(issuer);
				if (root != null)
					feeAccountField.setValue(root.getCode() + "." + code);
				else
					feeAccountField.setErrorMessage("Issuer does not have an Earnings Account set");
			}
			if (isFieldEditable(lossesAccountField)) {
				Account root = getHelper().getIssuersLossesAccount(issuer);
				if (root != null)
					lossesAccountField.setValue(root.getCode() + "." + code);
				else
					lossesAccountField.setErrorMessage("Issuer does not have a Losses Account set");
			}
		} catch (Exception e) {
			getApp().getLog().error(e);
		}
	}

	private boolean isFieldEditable (TextField field) {
		return field != null && !field.isReadOnly() && field.isEnabled();
	}

	@Override
	public QIHelper createHelper() {
		return new CardProductHelper(getViewConfig());
	}

	public CardProductHelper getHelper() {
		return (CardProductHelper) super.getHelper();
	}

	@Override
	public Component initContent() {

		schemeComboxBox = createSchemeCombo();
		VerticalLayout vl = new VerticalLayout();
		vl.setHeightFull();
		H2 viewTitle = new H2(getApp().getMessage(getName()));
		viewTitle.addClassNames("mt-s", "text-l");
		crud = createCrud();
		errorNotification= NotificationUtils.getInstance().getErrorNotification(NotificationVariant.LUMO_PRIMARY, Notification.Position.BOTTOM_END);
		vl.add(viewTitle, schemeComboxBox, crud,errorNotification);
		if (getEntityId() != null)
			setBean((CardProduct) getHelper().getEntityById(getEntityId()));
		if (getBean() != null)
			crud.getGrid().select(getBean());

		schemeComboxBox.addValueChangeListener(e -> {
			try {
				filteredDataProvider = dataProvider.withConfigurableFilter();
				crud.getGrid().setDataProvider(filteredDataProvider);
				SysConfig sysConfig = e.getValue();
				this.selectedScheme = sysConfig != null ? sysConfig.getValue() : null;
				filteredDataProvider.setFilter(this.selectedScheme);
				dataProvider.refreshAll();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		this.vl=vl;
		return vl;
	}

	
	private ComboBox<SysConfig> createSchemeCombo() {
		SysConfigComboBox field;
		try {
			field = new SysConfigComboBox(QIUtils.getCaptionFromId("field.scheme"), "scheme");
		} catch (Exception e) {
			getApp().getLog().error(e.getMessage());
			return null;
		}
		return field;
	}
}
