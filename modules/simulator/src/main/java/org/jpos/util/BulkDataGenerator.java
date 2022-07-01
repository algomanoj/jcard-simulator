package org.jpos.util;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jpos.core.InvalidCardException;
import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.CardManager;
import org.jpos.ee.CardProduct;
import org.jpos.ee.CardState;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.Fee;
import org.jpos.ee.FeeManager;
import org.jpos.ee.Issuer;
import org.jpos.ee.IssuerManager;
import org.jpos.ee.State;
import org.jpos.ee.SysConfig;
import org.jpos.ee.SysConfigManager;
import org.jpos.ee.TranLog;
import org.jpos.ee.TranLogFollowUp;
import org.jpos.ee.VelocityProfile;
import org.jpos.gl.Account;
import org.jpos.gl.CompositeAccount;
import org.jpos.gl.FinalAccount;
import org.jpos.gl.GLException;
import org.jpos.gl.GLSession;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKeyStore;
import org.jpos.security.jceadapter.SSM;

public class BulkDataGenerator extends QBeanSupport {

	public static Random random = new SecureRandom();

    private int NO_OF_FEE_RECORDS = 7;
	private List<Fee> feeList = new ArrayList<>();
	private List<CardProduct> cardProductList = new ArrayList<>();
	List<String> schemeList = new ArrayList<String>();
	private int NO_OF_CARDHOLDER_RECORDS = 1000;
	private int NO_OF_CH_WITHOUT_CARD_RECORDS = 100;
	private Map<String, List<State>> countryStateMap = new HashMap<>();
	Random rand = new Random();
	List<Issuer> issuerList = null;
	List<Fee> fees = null;
	List<VelocityProfile> velProfList = null;
	Map<String, BigDecimal> velAmount = new HashMap<>();
	ArrayList<ArrayList<String>> aList = new ArrayList<ArrayList<String>>(4);
	List<String> cardtypes = new ArrayList<>();
	List<CardHolder> cardHolderList = new ArrayList<>();
	private int tokenCounter = 0;
	String realIdStart = null;
	int realIdCounter = 0;
	//pointing all cardholder to same account
	FinalAccount defaultAcct = null;
	FinalAccount savingsAcct = null;
	FinalAccount checkingAcct = null;
	FinalAccount refundAcct = null;


	{
		DateFormat df = new SimpleDateFormat("yyMMddHH");
		realIdStart = df.format(new Date());
		
		velAmount.put("MaxTwoHundred", new BigDecimal("200.00"));
		velAmount.put("MaxThreeHundred", new BigDecimal("300.00"));
		velAmount.put("MaxFourHundred", new BigDecimal("400.00"));
		velAmount.put("MaxFiveHundred", new BigDecimal("500.00"));

		cardtypes.add("Credit Card");
		cardtypes.add("Debit Card");
		cardtypes.add("Charge Card");
		cardtypes.add("ATM Card");
		cardtypes.add("Stored-value Card");
		cardtypes.add("Fleet Card");
		cardtypes.add("Gift Card");
		cardtypes.add("Others");

		schemeList.add("VISA");
		schemeList.add("MasterCard"); 
		schemeList.add("AmEx");
		schemeList.add("Discover");
		 
		ArrayList<String> cardProductVisa = new ArrayList<String>();
		cardProductVisa.add("Classic:455636");
		cardProductVisa.add("Gold:480286");
		cardProductVisa.add("Platinum:413348");
		cardProductVisa.add("Signature:423608");
		cardProductVisa.add("Infinite:489113");
		cardProductVisa.add("Elite:453226");
		cardProductVisa.add("Prime:452668");
		cardProductVisa.add("Travel:491618");
		cardProductVisa.add("Dine:455682");
		cardProductVisa.add("Reward:456178");
		aList.add(cardProductVisa);

		ArrayList<String> cardProductMaster = new ArrayList<String>();
		cardProductMaster.add("Standard:517942");
		cardProductMaster.add("Titanium:532372");
		cardProductMaster.add("Platinum:541110");
		cardProductMaster.add("WorldMast:547837");
		cardProductMaster.add("WorldElite:512673");
		cardProductMaster.add("Select:519696");
		cardProductMaster.add("PremierMiles:525640");
		cardProductMaster.add("Business:534249");
		cardProductMaster.add("NEO:531789");
		cardProductMaster.add("SmartValue:513415");
		aList.add(cardProductMaster);

		ArrayList<String> cardProductAmeExp = new ArrayList<String>();
		cardProductAmeExp.add("Standard:371559");
		cardProductAmeExp.add("PlatinumEdge:371560");
		cardProductAmeExp.add("PlatinumCard:371566");
		cardProductAmeExp.add("PlatinumReserve:371701");
		cardProductAmeExp.add("WorldElite:371727");
		cardProductAmeExp.add("Gold:376064");
		cardProductAmeExp.add("SmartEarn:376066");
		cardProductAmeExp.add("MembershipRewards:376522");
		cardProductAmeExp.add("PlatinumTravel:376627");
		cardProductAmeExp.add("Delight:375001");
		aList.add(cardProductAmeExp);

		ArrayList<String> cardProductDiscover = new ArrayList<String>();
		cardProductDiscover.add("CashBack:601100");
		cardProductDiscover.add("Travel:601120");
		cardProductDiscover.add("Gas&Restaurant:659524");
		cardProductDiscover.add("StudentChrome:651621");
		cardProductDiscover.add("Business:655066");
		cardProductDiscover.add("Freedom:659524");
		cardProductDiscover.add("Goldclub:601149");
		aList.add(cardProductDiscover);
		 
	}

	public BulkDataGenerator() {
		
	}
	
	
    protected void initService() throws Exception {
    	log.info("InitService BulkData Generator");
	}

	protected void startService() throws Exception {
		log.info("Starting BulkData Generator");
		
		if(cfg.getBoolean("clean-up", true))
			cleanOldData();

		if(cfg.getBoolean("generate-data", true)) {
			GLSession gls = new GLSession(new DB(), "admin");
			defaultAcct = gls.getFinalAccount("jcard", "21.0000001.00");
			savingsAcct = gls.getFinalAccount("jcard", "21.0000001.10");
			checkingAcct = gls.getFinalAccount("jcard", "21.0000001.20");
			refundAcct = gls.getFinalAccount("jcard", "21.0000001.31");

			generateMockData();
		}
		
		log.info("BulkData Generator Completed");
	}

	protected void stopService() throws Exception {
		log.info("Stopping BulkData Generator");
	}

	protected void destroyService() throws Exception {
		log.info("Destroying BulkData Generator");
	}


    private Card createCard(DB db, CardProduct cp, CardHolder ch) throws NameRegistrar.NotFoundException, ISOException, InvalidCardException {
        CardManager cmgr = new CardManager(db);
		if (cp.getBin() == null)
			return null;
        
        String pan = cp.getBin() + ISOUtil.getRandomDigits(random, 9, 10);
        pan = pan + calcLUHN(pan.substring(0,15));

        Card card = cmgr.createCard(ch, pan.substring(0,15), null, null,"000", getSSM(), getCurrentBDK(), getCurrentBDKName());
        card.setActive(true);
        card.setCardHolder(ch);
        card.setStartDate(ISODate.parseISODate("20050101000000"));
        card.setEndDate(ISODate.parseISODate("20490101235959"));
        card.setToken(ISOUtil.zeropad(tokenCounter,12));
        card.setBin(cp.getBin());
        card.setState(CardState.ACTIVE);
        tokenCounter++;
        card.setCardProduct(cp);
        db.save(card);
        return card;
    }

    private SSM getSSM() throws NameRegistrar.NotFoundException {
        return (SSM) NameRegistrar.get("ssm");
    }

    private String getCurrentBDKName() {
        return "bdk.001";
    }
    private SecureDESKey getCurrentBDK() throws NameRegistrar.NotFoundException, SecureKeyStore.SecureKeyStoreException {
        SecureKeyStore ks = (SecureKeyStore) NameRegistrar.get("ks");
        return (SecureDESKey) ks.getKey(getCurrentBDKName());
    }

	private void createFeeRecords() throws Exception {
		for (Integer count = 1; count <= NO_OF_FEE_RECORDS; count++) {
			Fee feePercentage = createFeePercentage(count.toString());
			feeList.add(feePercentage);
			Fee feeFlat = createFeeFlat(count.toString());
			feeList.add(feeFlat);
		}
	}

	private Fee createFeePercentage(String amount) throws Exception {
		return DB.execWithTransaction(db -> {
			Fee cashAdvancePercentage = new Fee.FeeBuilder().type("CashAdvance.%.840").amount(new BigDecimal(amount))
					.build();
			db.save(cashAdvancePercentage);
			return cashAdvancePercentage;
		});
	}

	private Fee createFeeFlat(String amount) throws Exception {
		return DB.execWithTransaction(db -> {
			Fee cashAdvanceFlat = new Fee.FeeBuilder().type("CashAdvance.flat.840").amount(new BigDecimal(amount))
					.build();
			db.save(cashAdvanceFlat);
			return cashAdvanceFlat;
		});
	}

	public void generateMockData() throws Exception {
		log.info("Starting data population");
		
		log.info("Genarating Fee Records");
		createFeeRecords();
		
		log.info("Creating Issuer Records");
		createIssuer("First Transactility Republic Bank", "100001111", "Test Contact", "CTO",
				"testIssuer@transactility.com");
		initiateData();

		Integer cpCount = 0;
		Integer account = 0;
		int brandNo=0;
		for (String brand : schemeList) {
			
			for (Entry<String, BigDecimal> map : velAmount.entrySet()) {
				velProfList.add(createVP(brand + "_" + map.getKey(), "840", true, false, false, true, true, true, false,
						0, 1, map.getValue()));
			}

			for (String cp : aList.get(brandNo)) {
				log.info("Generating CardProduct for :"+brand +" , "+cp);
				cpCount++;
				String[] nameBin = cp.split(":");
				String name = brand + "_" + nameBin[0];

				// CardProduct -> CardHolder -> Card where cardproduct is used so order correct.
				CardProduct cpn = createCardProduct(brand, name, nameBin[1], "11.001.00", "31.001.00","41.001.00", cpCount.toString());
				cardProductList.add(cpn);
				
				log.info("Generating Cardholder under :"+cp);
				for (Integer cardNo = 1; cardNo <= NO_OF_CARDHOLDER_RECORDS; cardNo++) {
					account++;
					cardHolderList.add(createCardHolders("CH", account.toString(), true));
				}

			}
			brandNo++;
		}

		for (int i = 1; i <= NO_OF_CH_WITHOUT_CARD_RECORDS; i++) {
			account++;
			cardHolderList.add(createCardHolders("CH", account.toString(), false));
		}

	}

	private CardHolder createCardHolders(String name, String count, Boolean createCard) throws Exception {
		return DB.execWithTransaction(db -> {
			CardHolder ch = new CardHolder();
			String realId = realIdStart + (realIdCounter++);
			ch.setRealId(realId);
			ch.setFirstName(name + " FName " + rand.nextInt(50000));
			ch.setMiddleName(name + " MName " + rand.nextInt(50000));
			ch.setLastName(name + " LName " + rand.nextInt(50000));
			ch.setLastName2(name + " LName2 " + rand.nextInt(50000));
			ch.setActive(true);
			ch.setAddress1("Address Sector - " + rand.nextInt(1000));
			ch.setStartDate(getStartDate());
			ch.setEndDate(ISODate.parseISODate("20490101235959"));
			List<String> keysAsArray = new ArrayList<String>(countryStateMap.keySet());
			if (countryStateMap != null && countryStateMap.size() > 0) {
				String randomCountryId = keysAsArray.get(rand.nextInt(keysAsArray.size()));
				List<State> stateList = countryStateMap.get(randomCountryId);
				ch.setCountry(randomCountryId);
				ch.setState(stateList.get(rand.nextInt(stateList.size())));
			}

			ch.setZip(String.valueOf(rand.nextInt(99999)));
			ch.setEmail("ch" + String.valueOf(rand.nextInt(50000)) + "@ts.com");
			ch.setBirthDate(getBirthDate());

			
			
		/*
		 * DO IT LATER
			CompositeAccount parentAccount = gls.getCompositeAccount("jcard", "21");
			
			//setting up Composite account for customer
			CompositeAccount ca = new CompositeAccount();
			ca.setCode("21." + realId);
			ca.setParent(parentAccount);
			ca.setRoot((CompositeAccount) gls.getChart("jcard"));
			ca.setCreated(new Date());
			ca.setType(Account.CREDIT);
			gls.addAccount(parentAccount, ca);
			
			//setting up final accounts for customer
			Map accounts = new HashMap();

			FinalAccount cardHolderAccount = new FinalAccount();
			cardHolderAccount.setCode("21." + realId + ".00");
			cardHolderAccount.setParent(ca);
			cardHolderAccount.setDescription("CardHolder " + realId +" SV Account");
			cardHolderAccount.setRoot((CompositeAccount) gls.getChart("jcard"));
			cardHolderAccount.setType(Account.CREDIT);
			cardHolderAccount.setCreated(new Date());
			gls.addAccount(ca, cardHolderAccount);
			accounts.put("00.840", cardHolderAccount);
			
			FinalAccount svAcct = new FinalAccount();
			svAcct.setCode("21." + realId + ".10");
			svAcct.setParent(ca);
			svAcct.setDescription("CardHolder " + realId +" SV Savings");
			svAcct.setRoot((CompositeAccount) gls.getChart("jcard"));
			svAcct.setType(Account.CREDIT);
			cardHolderAccount.setCreated(new Date());
			gls.addAccount(ca, svAcct);
			accounts.put("10.840", svAcct);

			FinalAccount chkAcct = new FinalAccount();
			chkAcct.setCode("21." + realId + ".20");
			chkAcct.setParent(ca);
			chkAcct.setDescription("CardHolder " + realId +" SV Checking");
			chkAcct.setRoot((CompositeAccount) gls.getChart("jcard"));
			chkAcct.setType(Account.CREDIT);
			chkAcct.setCreated(new Date());
			gls.addAccount(ca, chkAcct);
			accounts.put("20.840", chkAcct);

			FinalAccount refAcct = new FinalAccount();
			refAcct.setCode("21." + realId + ".31");
			refAcct.setParent(ca);
			refAcct.setDescription("CardHolder " + realId +" SV Refunds");
			refAcct.setRoot((CompositeAccount) gls.getChart("jcard"));
			refAcct.setType(Account.CREDIT);
			refAcct.setCreated(new Date());
			gls.addAccount(ca, refAcct);
			accounts.put("31.840", refAcct);
	*/
			/*
			21.0000001.00
			21.0000001.10
			21.0000001.20
			21.0000001.31
			*/
			Map accounts = new HashMap();
			accounts.put("00.840", defaultAcct);
			accounts.put("10.840", savingsAcct);
			accounts.put("20.840", checkingAcct);
			accounts.put("31.840", refundAcct);
			ch.setAccounts(accounts);
			db.save(ch);

			if (createCard) {
				
				Card card = createCard(db, cardProductList.get(rand.nextInt(cardProductList.size())), ch);
				Set<Card> set = new HashSet<>();
				set.add(card);
				ch.setCards(set);
				db.save(ch);
				db.save(card);
			} else {
				db.save(ch);
			}
			return ch;
		});

	}

	private CardProduct createCardProduct(String scheme, String name, String bin, String issuedAccount, String feeAccount,
			String lossesAct, String code) throws Exception {
		return (CardProduct) DB.execWithTransaction(db -> {
			CardProduct cp = new CardProduct();
			Issuer issuer=issuerList.get(rand.nextInt(issuerList.size()));
			cp.setIssuer(issuer);
			cp.setScheme(scheme);
			cp.setName(name);
			cp.setStartDate(ISODate.parseISODate("20050101000000"));
			cp.setEndDate(ISODate.parseISODate("20490101235959"));
			cp.setActive(true);
			cp.setCode(code);
			GLSession gls = new GLSession(db, "admin");
			cp.setIssuedAccount(gls.getFinalAccount("jcard", issuedAccount));
			cp.setFeeAccount(gls.getFinalAccount("jcard", feeAccount));
			FinalAccount losAct=gls.getFinalAccount("jcard", lossesAct);
			if(losAct == null) {
				losAct=createLossAccount(issuer,"jcard", lossesAct,gls);
			}
			cp.setLossesAccount(losAct);
			
			
			Set<Fee> fees = new HashSet<>();
			feeList = getFees();
			Random rand = new Random();
			for (int count = 0; count < 5; count++) {
				fees.add(feeList.get(rand.nextInt(feeList.size())));
			}
			cp.setFees(fees);
			Set<VelocityProfile> selectedVel = new HashSet<>();
			for (int count = 0; count < 5; count++) {
				selectedVel.add(velProfList.get(rand.nextInt(velProfList.size())));
			}
			cp.setVelocityProfiles(selectedVel);

			cp.setPanLength(16);
			cp.setBin(bin);
			cp.setPanStart(bin+"0000000000");
			cp.setPanEnd(bin+"9999999999");
			cp.setCardType(cardtypes.get(rand.nextInt(cardtypes.size())));
			db.save(cp);
			return cp;
		});

	}

	private FinalAccount createLossAccount(Issuer issuer, String string, String lossesAct,GLSession gls) {
		FinalAccount newAcct= new FinalAccount();
		try {
			Account parent= getRefreshedAccount(issuer.getLossesAccount());
			newAcct.setCode(lossesAct);
			newAcct.setCreated(new Date());
			newAcct.setRoot(parent.getRoot());
			newAcct.setType(parent.getType());
	        gls.session().refresh(parent);
			gls.addAccount((CompositeAccount) parent, newAcct);
			} catch (Exception  e) {
				e.printStackTrace();
			}
	
	        return (FinalAccount) newAcct;
	}
	
	private Account getRefreshedAccount (Account acct) throws Exception {
        return DB.exec(db-> (acct != null) ? db.session().get(Account.class, acct.getId()) : null);
    }


	private VelocityProfile createVP(String name, String currencyCode, boolean scopeCard, boolean scopeAccount,
			boolean approvalsOnly, boolean onPurchase, boolean onWithdrawal, boolean onTransfer, boolean onCredit,
			int numberOfDays, int usageLimit, BigDecimal amountLimit) throws Exception {
		return (VelocityProfile) DB.execWithTransaction(db -> {
			VelocityProfile vp = new VelocityProfile();
			vp.setName(name);
			vp.setActive(true);
			vp.setCurrencyCode(currencyCode);
			vp.setNumberOfDays(numberOfDays);
			vp.setScopeCard(scopeCard);
			vp.setScopeAccount(scopeAccount);
			vp.setApprovalsOnly(approvalsOnly);
			vp.setUsageLimit(usageLimit);
			vp.setAmountLimit(amountLimit);
			vp.setValidOnPurchase(onPurchase);
			vp.setValidOnWithdrawal(onWithdrawal);
			vp.setValidOnTransfer(onTransfer);
			vp.setValidOnCredit(onCredit);
			db.save(vp);
			return vp;
		});
	}

	private Issuer createIssuer(String issuerName, String realId, String contactName, String contactPosition,
			String contactEmail) throws Exception {
		return DB.execWithTransaction(db -> {
			Issuer issuer = new Issuer();
			issuer.setActive(true);
			issuer.setInstitutionId(realId);
			issuer.setName(issuerName);
			issuer.setTz("UTC");
			issuer.setStartDate(ISODate.parseISODate("20050101000000"));
			issuer.setEndDate(ISODate.parseISODate("20490101235959"));
			GLSession gls = new GLSession(db, "admin");
			issuer.setJournal(gls.getJournal("jcard"));
			issuer.setAssetsAccount((CompositeAccount) gls.getAccount("jcard", "11.001"));
			issuer.setEarningsAccount((CompositeAccount) gls.getAccount("jcard", "31.001"));
			issuer.setLossesAccount((CompositeAccount) gls.getAccount("jcard", "41.001"));
			issuer.setContactName(contactName);
			issuer.setContactPosition(contactPosition);
			issuer.setContactEmail(contactEmail);
			issuer.setZip(String.valueOf(rand.nextInt(99999)));
			db.save(issuer);
			return issuer;
		});
	}

	private void initiateData() throws Exception {
		List<SysConfig> items = null;
		items = DB.exec(db -> {
			SysConfigManager mgr = new SysConfigManager(db, "country.");
			return mgr.getAllByValue();
		});

		for (SysConfig sys : items) {
			String countryCode = sys.getId().substring("country.".length());
			List<State> stateList = getStates("858");
			if (stateList != null && stateList.size() > 0) {
				countryStateMap.put(countryCode, stateList);
			}
		}

		issuerList = getIssuers();
		feeList = getFees();
		velProfList = getVelocityProfiles();
	}

	public List<Issuer> getIssuers() {
		try {
			Issuer issuer  = DB.exec((db) -> {
				IssuerManager mgr = new IssuerManager(db);
				return mgr.getIssuerById("67");
			});
			List<Issuer> list = new ArrayList<Issuer>();
			list.add(issuer);
			return list;
			
		} catch (Exception e) {
			return null;
		}
	}

	public List<Fee> getFees() {
		try {
			return DB.exec((db) -> {
				FeeManager mgr = new FeeManager(db);
				return mgr.getAll();
			});
		} catch (Exception e) {
			return null;
		}
	}

	public List<VelocityProfile> getVelocityProfiles() {
		try {
			return DB.exec((db) -> {
				DBManager mgr = new DBManager<VelocityProfile>(db, VelocityProfile.class);
				return mgr.getAll();
			});
		} catch (Exception e) {
			return null;
		}
	}

	public List<State> getStates(String countryCode) {
		try {
			return DB.exec(db -> {
				CriteriaBuilder criteriaBuilder = db.session().getCriteriaBuilder();
				CriteriaQuery<State> query = criteriaBuilder.createQuery(State.class);
				Root<State> root = query.from(State.class);
				query.select(root);
				if (countryCode != null) {
					Predicate notSelf = criteriaBuilder.equal(root.get("countryCode"), countryCode);
					query.where(notSelf);
				}
				List<State> list = db.session().createQuery(query).setMaxResults(100).setFirstResult(0).getResultList();
				return list;
			});
		} catch (Exception e) {
			return null;
		}
	}

	private Date getStartDate() {
		long aDay = TimeUnit.DAYS.toMillis(1);
		long now = new Date().getTime();
		Date hundredYearsAgo = new Date(now - aDay * 365 * 2);
		long startMillis = hundredYearsAgo.getTime();
		long randomMillisSinceEpoch = ThreadLocalRandom.current().nextLong(startMillis, now);

		return new Date(randomMillisSinceEpoch);
	}

	private Date getBirthDate() {
		long aDay = TimeUnit.DAYS.toMillis(1);
		long now = new Date().getTime();
		Date hundredYearsAgo = new Date(now - aDay * 365 * 50);
		Date tenDaysAgo = new Date(now - aDay * 365 * 10);
		long startMillis = hundredYearsAgo.getTime();
		long endMillis = tenDaysAgo.getTime();
		long randomMillisSinceEpoch = ThreadLocalRandom.current().nextLong(startMillis, endMillis);

		return new Date(randomMillisSinceEpoch);
	}
	
	public static char calcLUHN (CharSequence p) {
        int i, crc;

        int odd = p.length() % 2;

        for (i=crc=0; i<p.length(); i++) {
            char c = p.charAt(i);
            if (!Character.isDigit (c)) {
                throw new IllegalArgumentException("Invalid PAN " + p);
            }
            c = (char) (c - '0');
            if (i % 2 != odd)
                crc+=(c*2) >= 10 ? ((c*2)-9) : (c*2);
            else
                crc+=c;
        }

        return (char) ((crc % 10 == 0 ? 0 : (10 - crc % 10)) + '0');
    }
	
	
	private void cleanOldData() throws Exception {
		log.info("Started cleaning old generated data");
		/*
		 * 
			delete  from tranlog_followups where id > 1399
			delete  from tranlog where id > 1625
			delete  from card where id > 1566
			delete  from cardproduct_fees where cardproduct > 73;
			delete  from fees where id > 8
			delete  from cardholder_accounts where id > 1561
			delete  from acct where id > 1631
			delete  from cardholder where id > 1601
			delete  from cardproduct_velocityprofiles where cardproduct > 73
			delete  from velocity_profiles where id > 70
			delete  from card_products where id > 73
			delete  from issuers where id > 67
		 */
		deleteTableData(TranLogFollowUp.class.getName(), 1399);
		deleteTableData(TranLog.class.getName(), 1625);
		deleteTableData(Card.class.getName(), 1566);
		deleteTableDataNative("cardproduct_fees", "cardproduct", 73);
		deleteTableData(Fee.class.getName(), 8);
		deleteTableDataNative("cardholder_accounts", "id", 1561);
		deleteTableDataNative("acctlock", "account", 1651);
		deleteTableDataNative("balance_cache", "account", 1651);
		deleteTableDataNative("transentry", "account", 1651);
		deleteTableData(Account.class.getName(), 1651);
		deleteTableData(CardHolder.class.getName(), 1601);
		deleteTableDataNative("cardproduct_velocityprofiles", "cardproduct", 73);
		deleteTableData(VelocityProfile.class.getName(), 70);
		deleteTableData(CardProduct.class.getName(), 73);
		deleteTableData(Issuer.class.getName(), 67);
		
		log.info("Old generated data cleaned up");		
	}
	
	
	public void deleteTableData(String className, long id) throws Exception {
		log.info("Deleting records from table :"+className + ", starting id :"+id);
		
		Class entityClass = Class.forName(className);
        
        DB.execWithTransaction(db -> {
			CardProduct cp = new CardProduct();

	        CriteriaBuilder cb = db.session().getCriteriaBuilder();

	        CriteriaDelete delete = cb.createCriteriaDelete(entityClass);
	        Root e = delete.from(entityClass);
	 
	        // set where clause
	        delete.where(cb.greaterThan(e.get("id"), id));
	        Query cq = db.session().createQuery(delete);
	        
	        cq.executeUpdate();
			return null;
		});
    }
	
	
	public void deleteTableDataNative(String tableName, String columnName, long value) throws Exception {
		String query  = "delete from "+tableName + " where " + columnName + " > "+value;
		log.info(query);
        
        DB.execWithTransaction(db -> {

	        Query cq = db.session().createSQLQuery(query);
	        cq.executeUpdate();
			return null;
		});
    }
}