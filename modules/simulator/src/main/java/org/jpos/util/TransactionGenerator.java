package org.jpos.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.jpos.ee.Card;
import org.jpos.ee.DB;
import org.jpos.iso.ISOAmount;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.iso.PosDataCode;
import org.jpos.iso.PosDataCode.POSEnvironment;
import org.jpos.iso.PosDataCode.ReadingMethod;
import org.jpos.iso.PosDataCode.SecurityCharacteristic;
import org.jpos.iso.PosDataCode.VerificationMethod;
import org.jpos.q2.QBeanSupport;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKeyStore;
import org.jpos.security.SecureKeyStore.SecureKeyStoreException;
import org.jpos.security.jceadapter.SSM;
import org.jpos.util.NameRegistrar.NotFoundException;

public class TransactionGenerator extends QBeanSupport implements Runnable {
	DB db;
	long txnPauseinterval;
	List<String> merchants;
	Map<String, ISOMsg> merchantDetail = new HashMap<>();
	Map<String, List<String>> terminals = new HashMap<>();

	public void initService() throws Exception {
		db = new DB();
		db.open();
		
		txnPauseinterval = cfg.getLong("txn-pause-interval", 60000L);
		merchants = Arrays.asList(cfg.get("mid").split(","));
		for (String mid : merchants) {
			terminals.put(mid, Arrays.asList(cfg.get(mid + "." + "tid").split(",")));

			// populate merchant details
			String details = cfg.get(mid + "." + "details");
			if (details != null && details.length() >= 44) {
				ISOMsg f43 = new ISOMsg(43);
				f43.set(2, details.substring(0, 20));
				f43.set(4, details.substring(20, 33));
				f43.set(6, details.substring(33, 42));
				f43.set(7, details.substring(42));
				merchantDetail.put(mid, f43);
			}
		}
	}

	public void startService() {
		long initialDelay = cfg.getLong("initial-delay", 1000);
		log.info("Initial delay found for "+ initialDelay);
		ISOUtil.sleep(initialDelay);
		
		log.info("Starting txns ");
		new Thread(this).start();
	}

	public void run() {

		while (running()) {
			try {
				MUX mux = NameRegistrar.getIfExists("mux.jcard");
				int start = 7;
				Criteria crit = db.session().createCriteria(Card.class).setFirstResult(start).setMaxResults(100);

				List<Card> list = crit.list();
				for (Card card : list) {

					// Get the active card from database
					ISOMsg req = createRequest(card);
					ISOMsg resp = mux.request(req, 2000);
					
					ISOUtil.sleep(txnPauseinterval);				
				}
				start = start + list.size();
				if (list.size() < 100)
					start = 8;

			} catch (Throwable t) {
				getLog().error(t);
				ISOUtil.sleep(1000);
			} finally {
			}
			ISOUtil.sleep(txnPauseinterval*5);
		}

	}

	private ISOMsg createRequest(Card card) throws ISOException, NotFoundException {
		ISOMsg m = new ISOMsg("2100");

		// descrypt and get the pan
		Map secureMap = getCardSecureMap(card);
		m.set(2, (String) secureMap.get("P"));
		m.set(3, "000000"); // Processing Code

		Random rand = new Random();
		int rand_int1 = rand.nextInt(10);
		BigDecimal amount = new BigDecimal(rand_int1);
		m.set(new ISOAmount(4, 840, amount));

		Date date = new Date();
		m.set(7, ISODate.getDateTime(date)); // date

		m.set(7, ISODate.getDateTime(new Date()));
		m.set(11, ISOUtil.zeropad(Integer.toString(rand.nextInt(1000000)), 12));
		m.set(12, ISODate.formatDate(date, "yyyyMMddHHmmss"));

		// take expiry from card
		m.set(14, ISODate.getExpirationDate(card.getEndDate())); // expiry date

		PosDataCode pdc = new PosDataCode(ReadingMethod.PHYSICAL.intValue(), VerificationMethod.NONE.intValue(),
				POSEnvironment.E_COMMERCE.intValue(), SecurityCharacteristic.UNKNOWN.intValue());

		m.set(22, pdc.getBytes());

		m.set(32, cfg.get("acquirer_id"));
		m.set(37, ISOUtil.zeropad(Integer.toString(rand.nextInt(1000000)), 12));

		int merchantIdx = rand.nextInt(10);
		String mid = merchants.get(merchantIdx);

		int terminalIdx = rand.nextInt(10);
		String tid = terminals.get(mid).get(terminalIdx);

		m.set(41, tid);
		m.set(42, mid);
		ISOMsg f43 = merchantDetail.get(mid);

		m.set(f43);

		m.set("113.2", "106");
		m.set("113.25", "MINIATM");
		
		return m;
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