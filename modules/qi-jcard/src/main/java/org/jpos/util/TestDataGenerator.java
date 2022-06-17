package org.jpos.util;

import org.jpos.core.InvalidCardException;
import org.jpos.ee.*;
import org.jpos.gl.*;
import org.jpos.gl.tools.Import;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.CLIContext;
import org.jpos.q2.QBeanSupport;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKeyStore;
import org.jpos.security.jceadapter.SSM;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by jr on 9/18/17.
 */

//todo: place on correct package, only here temporarily
public class TestDataGenerator extends QBeanSupport {

    private CLIContext cli;
    private Issuer issuer;
    private Map urls;

    private String miniGLSetupPath= "../../../../jcard/src/setup/dist/cfg/minigl-setup.xml";
    private int NO_OF_FEE_RECORDS = 7;
	private List<Fee> feeList = new ArrayList<>();
	private List<CardProduct> cardProductList = new ArrayList<>();
	List<String> schemeList = new ArrayList<String>();
	private int NO_OF_CARDHOLDER_RECORDS = 1000;
	private int NO_OF_CH_WITHOUT_CARD_RECORDS = 1000;
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
	static int cardNo = 1234567;

	{
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
		cardProductVisa.add("Classic");
		cardProductVisa.add("Gold");
		cardProductVisa.add("Platinum");
		cardProductVisa.add("Signature");
		cardProductVisa.add("Infinite");
		cardProductVisa.add("Elite");
		cardProductVisa.add("Prime");
		cardProductVisa.add("Travel");
		cardProductVisa.add("Dine");
		cardProductVisa.add("Reward");
		aList.add(cardProductVisa);

		ArrayList<String> cardProductMaster = new ArrayList<String>();
		cardProductMaster.add("Standard");
		cardProductMaster.add("Titanium");
		cardProductMaster.add("Platinum");
		cardProductMaster.add("WorldMast");
		cardProductMaster.add("WorldElite");
		cardProductMaster.add("Select");
		cardProductMaster.add("PremierMiles");
		cardProductMaster.add("Business");
		cardProductMaster.add("NEO");
		cardProductMaster.add("SmartValue");
		aList.add(cardProductMaster);

		ArrayList<String> cardProductAmeExp = new ArrayList<String>();
		cardProductAmeExp.add("Standard");
		cardProductAmeExp.add("PlatinumEdge");
		cardProductAmeExp.add("PlatinumCard");
		cardProductAmeExp.add("PlatinumReserve");
		cardProductAmeExp.add("WorldElite");
		cardProductAmeExp.add("Gold");
		cardProductAmeExp.add("SmartEarn");
		cardProductAmeExp.add("MembershipRewards");
		cardProductAmeExp.add("PlatinumTravel");
		cardProductAmeExp.add("Delight");
		aList.add(cardProductAmeExp);

		ArrayList<String> cardProductDiscover = new ArrayList<String>();
		cardProductDiscover.add("CashBack");
		cardProductDiscover.add("Travel");
		cardProductDiscover.add("Gas&Restaurant");
		cardProductDiscover.add("StudentChrome");
		cardProductDiscover.add("Business");
		cardProductDiscover.add("Freedom");
		cardProductDiscover.add("Goldclub");
		aList.add(cardProductDiscover);

	}

	public TestDataGenerator() {
		
	}

    public TestDataGenerator(CLIContext cli, String fileUrl, String mglSetup) throws Exception  {
        this.cli=cli;

        miniGLSetupPath= mglSetup != null ? mglSetup : miniGLSetupPath;
        File setupFile= new File(miniGLSetupPath);
        if (!setupFile.exists()) {
            System.out.println("Invalid minigl setup path: '"+miniGLSetupPath+"'");
            throw new FileNotFoundException(miniGLSetupPath);
        }

        File urlFile = new File(fileUrl);
        try {
            CSVParser parser = new CSVParser(urlFile);
            Map urls = parser.readLine();
            this.urls = urls;
            parser.close();
        } catch (FileNotFoundException f) {
            System.out.println("Invalid url. Please check.");
            throw f;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public void generateData() {
        try {
            String rolesURL = (String) urls.get("roles");
            if (rolesURL != null) {
                generateRoles(rolesURL);
            }
            String usersURL = (String) urls.get("users");
            if (usersURL != null) {
                generateUsers(usersURL);
            }
            String rcURL = (String) urls.get("rc");
            if (rcURL != null) {
                generateRCs(rcURL);
            }
            generateMiniGL();
            String cardholdersURL = (String) urls.get("cardholders");
            if (cardholdersURL != null) {
                generateCardHolders(cardholdersURL);
            }
            System.out.println("Finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateCardHolders(String cardholdersURL) throws Exception {
        if (isEmpty(CardHolder.class)) {
            System.out.println("Generating Cardholders...");
            CSVParser cardholderParser = new CSVParser(new File(cardholdersURL));
            Map cardholderMap = cardholderParser.readLine();
            Random r = new Random();
            while (cardholderMap != null) {
                int i = r.nextInt(cardProductList.size());
                createCardHolder(cardProductList.get(i), cardholderMap);
                cardholderMap = cardholderParser.readLine();
            }
            cardholderParser.close();
        } else {
            throw new BLException("Database not empty. Please verify");
        }
    }

    private void generateRCs(String rcURL) throws Exception {
        if (isEmpty(ResultCode.class)) {
            System.out.println("Generating Response Codes...");
            CSVParser rcParser = new CSVParser((new File(rcURL)));
            Map rc = rcParser.readLine();
            while (rc != null) {
                createRC(rc);
                rc = rcParser.readLine();
            }
            rcParser.close();
        } else {
            throw new BLException("Database not empty. Please verify");
        }
    }

    private void generateUsers(String usersURL) throws Exception {
        if (usersEmptyOrAdmin()) {
            System.out.println("Generating Users...");
            CSVParser userParser = new CSVParser(new File(usersURL));
            Map user = userParser.readLine();
            while (user != null) {
                createUser(user);
                user = userParser.readLine();
            }
            userParser.close();
        } else {
            throw new BLException("Database not empty. Please verify");
        }
    }

    private void generateRoles(String rolesURL) throws Exception {
        if (isEmpty(Role.class)) {
            System.out.println("Generating Roles...");
            CSVParser roleParser = new CSVParser(new File(rolesURL));
            Map role = roleParser.readLine();
            while (role != null) {
                createRole(role);
                role = roleParser.readLine();
            }
            roleParser.close();
        } else {
            throw new BLException("Database not empty. Please verify");
        }
    }

    private void generateMiniGL() {
        System.out.println("Setting up miniGL from '"+miniGLSetupPath+"'");
        try {
            new Import().parse(miniGLSetupPath);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //ISSUER AND ACQUIRER AND CARDPRODUCTS
        cardProductList = new ArrayList<>();
        try {
            issuer = createIssuer("jcard","00000000001");
            CardProduct cpn = createCardProduct(issuer, "jCard", "11.001.00","31.001.00");
            createVelocityProfiles(cpn);
            cardProductList.add(cpn);
            cardProductList.add(createCardProduct (issuer, "jCard Gold","11.001.00","31.001.00"));
            CardProduct cpGift = createCardProduct (issuer, "Gift Cards", "11.001.00","31.001.00");
            cpGift.setAnonymous(true);
            cardProductList.add(cpGift);
            Acquirer acquirer = createTestAcquirer(issuer);
            Merchant m = createMerchant(acquirer);
            generateStoresAndTerminals(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createVelocityProfiles(CardProduct cpn) throws Exception {
        DB.execWithTransaction(db -> {
            db.session().refresh(cpn);
            cpn.getVelocityProfiles ().add(
                    createVP("MaxTwoHundred", cpn, "840", true, false, false,
                            true, true, true,false, 0, 1, new BigDecimal("200.00"))
            );
            cpn.getVelocityProfiles ().add (
                    createVP ("FiveHundred", cpn, "840", true, true, true,
                            true, true, true, false, 5, 5, new BigDecimal ("500.00"))
            );
            db.save(cpn);
            return null;
        });
    }

    private void createRC(Map rcMap) throws Exception {
        DB.execWithTransaction(db -> {
            ResultCode rc = new ResultCode();
            rc.setId(Long.valueOf((String) rcMap.get("id")));
            rc.setMnemonic((String) rcMap.get("mnemonic"));
            rc.setDescription((String) rcMap.get("description"));
            Map<String,ResultCodeInfo> locales = new HashMap();
            ResultCodeInfo rci = new ResultCodeInfo();
            rci.setResultCode(ISOUtil.zeropad(rc.getId(),4));
            rci.setResultInfo(rc.getDescription());
            locales.put("JCARD", rci);
            rc.setLocales(locales);
            db.save(rc);
            return null;
        });
    }

    private void createRole(Map rolesMap) throws Exception {
        DB.execWithTransaction(db -> {
            Role r = new Role((String) rolesMap.get("name"));
            db.save(r);
            return null;
        });
    }

    private void createUser(Map userMap) throws Exception {
        DB.execWithTransaction(db -> {
            // System.out.println("      User '"+userMap.get("nick")+"'");
            User user = new User();
            user.setNick ((String) userMap.get("nick"));
            user.setActive (true);
            user.setName ((String) userMap.get("name"));
            UserManager mgr = new UserManager(db);
            mgr.setPassword(user, "guest");
            db.save (user);
            return null;
        });
    }

    private Issuer createIssuer(String issuerName, String realId) throws Exception {
        return DB.execWithTransaction(db -> {
            Issuer issuer = new Issuer();
            issuer.setActive (true);
            issuer.setInstitutionId(realId);
            issuer.setName(issuerName);
            issuer.setTz ("UTC");
            issuer.setStartDate (ISODate.parseISODate("20050101000000"));
            issuer.setEndDate (ISODate.parseISODate ("20490101235959"));
            GLSession gls = new GLSession(db,"admin");
            issuer.setJournal (gls.getJournal ("jcard"));
            issuer.setAssetsAccount ((CompositeAccount) gls.getAccount ("jcard", "11.001"));
            issuer.setEarningsAccount ((CompositeAccount) gls.getAccount ("jcard", "31.001"));
            issuer.setLossesAccount ((CompositeAccount) gls.getAccount ("jcard", "41.001"));
            db.save (issuer);
            return issuer;
        });
    }

    private CardProduct createCardProduct (Issuer issuer, String name, String issuedAccount, String feeAccount) throws Exception {
        return (CardProduct) DB.execWithTransaction(db -> {
            CardProduct cp = new CardProduct();
            cp.setIssuer(issuer);
            cp.setName(name);
            cp.setStartDate(ISODate.parseISODate("20050101000000"));
            cp.setEndDate(ISODate.parseISODate("20490101235959"));
            cp.setActive(true);
            GLSession gls = new GLSession(db,"admin");
            cp.setIssuedAccount(gls.getFinalAccount("jcard",issuedAccount));
            cp.setFeeAccount(gls.getFinalAccount("jcard",feeAccount));
            db.save(cp);
            Fee cashAdvanceFlat = new Fee.FeeBuilder().type("CashAdvance.flat.840").amount(new BigDecimal("3.50"))/*.cardProduct(cp)*/.build();
            Fee cashAdvancePercentage = new Fee.FeeBuilder().type("CashAdvance.%.840").amount(new BigDecimal("3.2500"))/*.cardProduct(cp)*/.build();
            db.save(cashAdvanceFlat);
            db.save(cashAdvancePercentage);
            return cp;
        });

    }

    private VelocityProfile createVP (String name, CardProduct cp, String currencyCode,
            boolean scopeCard, boolean scopeAccount, boolean approvalsOnly,
            boolean onPurchase, boolean onWithdrawal, boolean onTransfer, boolean onCredit,
            int numberOfDays, int usageLimit, BigDecimal amountLimit)
    {
        VelocityProfile vp = new VelocityProfile();
        vp.setName (name);
        vp.setActive (true);
        vp.setCurrencyCode (currencyCode);
        vp.setNumberOfDays (numberOfDays);
        vp.setScopeCard (scopeCard);
        vp.setScopeAccount (scopeAccount);
        vp.setApprovalsOnly (approvalsOnly);
        vp.setUsageLimit (usageLimit);
        vp.setAmountLimit (amountLimit);
        vp.setValidOnPurchase (onPurchase);
        vp.setValidOnWithdrawal (onWithdrawal);
        vp.setValidOnTransfer (onTransfer);
        vp.setValidOnCredit(onCredit);
        //vp.setCardProduct(cp);
        return vp;
    }

    private Acquirer createTestAcquirer(Issuer issuer) throws Exception {
        return (Acquirer) DB.execWithTransaction(db -> {
            Acquirer acquirer = new Acquirer();
            acquirer.setInstitutionId(issuer.getInstitutionId()); // use same ID as Issuer for testing purposes
            acquirer.setActive (true);
            acquirer.setIssuer (issuer);
            acquirer.setName ("Test Acquirer");
            GLSession gls = new GLSession(db,"admin");
            acquirer.setTransactionAccount (
                    gls.getFinalAccount ("jcard", "20.001.001")
            );
            acquirer.setFeeAccount (
                    gls.getFinalAccount ("jcard", "20.001.002")
            );
            acquirer.setRefundAccount (
                    gls.getFinalAccount ("jcard", "12.001.31")
            );
            acquirer.setDepositAccount (
                    gls.getFinalAccount ("jcard", "12.001.32")
            );
            db.save (acquirer);
            return acquirer;
        });
    }
    private Merchant createMerchant(Acquirer acquirer) throws Exception {
        return (Merchant) DB.execWithTransaction(db-> {
            Merchant merchant = new Merchant();
            merchant.setMerchantId("000000001");
            merchant.setActive(true);
            merchant.setName("Merchant A");
            merchant.setAcquirer (acquirer);
            db.save(merchant);
            return merchant;
        });
    }

    private void generateStoresAndTerminals(Merchant m) throws Exception {
        for (int i=1; i<=10; i++)
            createStoreAndTerminals(m, i);
    }

    private void createStoreAndTerminals (Merchant m, int i) throws Exception {
        Store s = (Store) DB.execWithTransaction(db -> {
            Store store = new Store();
            store.setMerchantId(m.getId() + ISOUtil.zeropad(Integer.toString(i), 3));
            store.setActive(true);
            store.setParent(m);
            store.setName(m.getName() + " store " + i);
            store.setAcquirer(m.getAcquirer());
            db.save(store);
            return store;
        });
        for (int j = 1; j <= 100; j++)
            createTerminal(s, j);
    }

    private void createTerminal (Merchant m, int i) throws Exception {
        DB.execWithTransaction(db -> {
            Terminal t = new Terminal();
            t.setTerminalId (ISOUtil.zeropad(Integer.toString(i), 8));
            t.setActive (true);
            t.setMerchant (m);
            db.save (t);
            return null;
        });

    }

    private void createCardHolder(CardProduct cp, Map cardholder) throws Exception {
        DB.execWithTransaction(db ->{
            // System.out.println("      CardHolder "+cardholder.get("realId"));
            CardHolder ch = new CardHolder();
            String realId = (String) cardholder.get("realId");
            ch.setRealId(realId);
            ch.setFirstName((String) cardholder.getOrDefault("firstName",""));
            ch.setMiddleName((String) cardholder.getOrDefault("middleName",""));
            ch.setLastName((String) cardholder.getOrDefault("lastName",""));
            ch.setLastName2((String) cardholder.getOrDefault("lastName2",""));
            ch.setHonorific((String) cardholder.getOrDefault("honorific",""));
            ch.setEmail((String) cardholder.get("email"));
            ch.setGender((String) cardholder.get("gender"));
            ch.setActive(true);
            ch.setStartDate(ISODate.parseISODate("20050101000000"));
            ch.setEndDate(ISODate.parseISODate("20490101235959"));
            ch.setIssuer(issuer);

            GLSession gls = new GLSession(db,"admin");
            CompositeAccount parentAccount = gls.getCompositeAccount("jcard","21");
            FinalAccount cardHolderAccount = new FinalAccount();
            cardHolderAccount.setCode("21." + realId);
            cardHolderAccount.setParent(parentAccount);
            cardHolderAccount.setDescription("CardHolder " + realId);
            cardHolderAccount.setRoot((CompositeAccount) gls.getChart("jcard"));
            cardHolderAccount.setType(Account.CREDIT);
            cardHolderAccount.setCreated(new Date());


            Card card = createCard(db, cp, ch,(String) cardholder.get("pan"));
            Set<Card> set = new HashSet<>();
            set.add(card);
            ch.setCards(set);
            db.save(cardHolderAccount);
            db.save(ch);
            db.save(card);
            Map accounts = new HashMap();
            accounts.put("00.840",cardHolderAccount);
            ch.setAccounts(accounts);

            GLTransaction txn = new GLTransaction("Initial balance");
            txn.createCredit(cardHolderAccount,BigDecimal.valueOf(1000),"Initial funds",(short)840);
            FinalAccount tempLiabilities = gls.getFinalAccount("jcard","29.001");
            txn.createDebit(tempLiabilities,BigDecimal.valueOf(1000),"Initial funds for CardHolder: " + ch.getRealId(),(short)840);
            gls.post(gls.getJournal("jcard"),txn);
            return null;
        });
    }

    private Card createCard(DB db, CardProduct cp, CardHolder ch, String pan) throws NameRegistrar.NotFoundException, ISOException, InvalidCardException {
        CardManager cmgr = new CardManager(db);
        if (pan.length() < 15) {
            pan = ISOUtil.zeropad(pan,15);
        }
        Card card = cmgr.createCard(ch, pan.substring(0,15), null, null,"000", getSSM(), getCurrentBDK(), getCurrentBDKName());
        card.setActive(true);
        card.setCardHolder(ch);
        card.setStartDate(ISODate.parseISODate("20050101000000"));
        card.setEndDate(ISODate.parseISODate("20490101235959"));
        card.setToken(ISOUtil.zeropad(tokenCounter,12));
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

    private boolean isEmpty(Class c) throws Exception {
        int count = (int) DB.exec(db ->  {
            CriteriaBuilder criteriaBuilder = db.session().getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            query.select(criteriaBuilder.count(query.from(c)));
            return db.session().createQuery(query).uniqueResult().intValue();
        });
        return count == 0;
    }

    // check that, at most, only the admin user exists
    private boolean usersEmptyOrAdmin() throws Exception {
        boolean succ= (boolean) DB.exec(db ->  {
            CriteriaBuilder criteriaBuilder = db.session().getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            query.select(criteriaBuilder.count(query.from(User.class)));
            int usersCount= db.session().createQuery(query).uniqueResult().intValue();

            if (usersCount == 0)
                return true;
            else if (usersCount > 1)
                return false;
            else  {
                // only 1 user in db, check if it's the admin
                UserManager mgr= new UserManager(db);
                User u = mgr.getUserByNick("admin", true);
                if (u != null)
                    return true;    // success
                else
                    return false;   // only 1 user, and not the adminn
            }
        });

        return succ;
    }
    
    protected void initService() throws Exception {
		System.out.println("initService");
	}

	protected void startService() throws Exception {
		System.out.println("startService");
		//generateData();
		
		// This function used  for creating bulk data in qi2g project
		generateMockData();
	}

	protected void stopService() throws Exception {
		System.out.println("stopService");
	}

	protected void destroyService() throws Exception {
		System.out.println("destroyService");
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
		createFeeRecords();
		createIssuer("First Transactility Republic Bank", "100001111", "Test Contact", "CTO",
				"testIssuer@transactility.com");
		initiateData();

		Integer cpCount = 0;
		Integer account = 0;
		Integer brandNo = 0;
		for (String brand : schemeList) {
			brandNo++;
			for (Entry<String, BigDecimal> map : velAmount.entrySet()) {
				velProfList.add(createVP(brand + "_" + map.getKey(), "840", true, false, false, true, true, true, false,
						0, 1, map.getValue()));
			}

			for (String cp : aList.get(brandNo - 1)) {
				cpCount++;
				String name = brand + "_" + cp;

				// CardProduct -> CardHolder -> Card where cardproduct is used so order correct.
				CardProduct cpn = createCardProduct(brand, name, "11.001.00", "31.001.00", cpCount.toString());
				cardProductList.add(cpn);

				for (Integer cardNo = 1; cardNo <= NO_OF_CARDHOLDER_RECORDS; cardNo++) {
					account++;
					cardHolderList.add(createCardHolders("CH", account.toString(), true));
				}

			}
		}

		for (int i = 1; i <= NO_OF_CH_WITHOUT_CARD_RECORDS; i++) {
			account++;
			cardHolderList.add(createCardHolders("CH", account.toString(), false));
		}

	}

	private CardHolder createCardHolders(String name, String count, Boolean createCard) throws Exception {
		return DB.execWithTransaction(db -> {
			CardHolder ch = new CardHolder();
			String realId = "12345678";
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

			GLSession gls = new GLSession(db, "admin");
			CompositeAccount parentAccount = gls.getCompositeAccount("jcard", "21");
			FinalAccount cardHolderAccount = new FinalAccount();
			cardHolderAccount.setCode("21." + count);
			cardHolderAccount.setParent(parentAccount);
			cardHolderAccount.setDescription("CardHolder " + realId);
			cardHolderAccount.setRoot((CompositeAccount) gls.getChart("jcard"));
			cardHolderAccount.setType(Account.CREDIT);
			cardHolderAccount.setCreated(new Date());
			Map accounts = new HashMap();
			accounts.put("00.840", cardHolderAccount);
			ch.setAccounts(accounts);
			db.save(cardHolderAccount);

			if (createCard) {
				Card card = createCard(db, cardProductList.get(rand.nextInt(cardProductList.size())), ch,
						Integer.toString(cardNo++));
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

	private CardProduct createCardProduct(String scheme, String name, String issuedAccount, String feeAccount,
			String code) throws Exception {
		return (CardProduct) DB.execWithTransaction(db -> {
			CardProduct cp = new CardProduct();
			cp.setIssuer(issuerList.get(rand.nextInt(issuerList.size())));
			cp.setScheme(scheme);
			cp.setName(name);
			cp.setStartDate(ISODate.parseISODate("20050101000000"));
			cp.setEndDate(ISODate.parseISODate("20490101235959"));
			cp.setActive(true);
			cp.setCode(code);
			GLSession gls = new GLSession(db, "admin");
			cp.setIssuedAccount(gls.getFinalAccount("jcard", issuedAccount));
			cp.setFeeAccount(gls.getFinalAccount("jcard", feeAccount));

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

			cp.setBin("554433");
			cp.setPanStart("5544330000000000");
			cp.setPanEnd("5544339999999999");
			cp.setCardType(cardtypes.get(rand.nextInt(cardtypes.size())));
			db.save(cp);
			return cp;
		});

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
			return DB.exec((db) -> {
				IssuerManager mgr = new IssuerManager(db);
				return mgr.getAll();
			});
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
}