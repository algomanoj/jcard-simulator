package org.jpos.qi.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jpos.ee.BLException;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.Issuer;
import org.jpos.ee.State;
import org.jpos.ee.SysConfigManager;
import org.jpos.gl.Account;
import org.jpos.gl.CompositeAccount;
import org.jpos.gl.GLException;
import org.jpos.gl.GLSession;
import org.jpos.gl.Journal;
import org.jpos.qi.ViewConfig;
import org.jpos.qi.minigl.JournalManager;
import org.jpos.qi.util.StateManager;

public class IssuerHelper extends QIHelper {

	public IssuerHelper(ViewConfig viewConfig) {
		super(Issuer.class, viewConfig);
	}

	@Override
	public String getItemId(Object item) {
		return String.valueOf(((Issuer) item).getId());
	}

	@Override
	public Object getEntityById(String param) {
		try {
			return DB.exec(db -> {
				db.session().enableFetchProfile("qi-issuers-view");
				return db.session().get(clazz, Long.parseLong(param));
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	public List<Journal> getJournals() {
		try {
			return DB.exec((db) -> {
				JournalManager mgr = new JournalManager(db);
				return mgr.getAll();
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	public List<State> getStates(String o) {
		try {
			return DB.exec((db) -> {
				StateManager mgr = null;
				if (o != null) {
					mgr = new StateManager(db, o);
				} else {
					mgr = new StateManager(db);
				}
				return mgr.getAll();
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}


	@Override
	public Stream getAll(int offset, int limit, Map<String, Boolean> orders) throws Exception {
		List<Issuer> items = DB.exec(db -> {
			db.session().enableFetchProfile("qi-issuers-view");
			DBManager<Issuer> mgr = new DBManager(db, clazz);
			List<Issuer> mgrIssuers = mgr.getAll(offset, limit, orders);
			// !CollectionUtils.isEmpty(mgrIssuers)
			if (mgrIssuers != null && mgrIssuers.size() > 0) {
				for (Issuer mgrIssuer : mgrIssuers) {
					if (mgrIssuer.getJournal() != null)
						mgrIssuer.getJournal().getName();
					if (mgrIssuer.getAssetsAccount() != null)
						mgrIssuer.getAssetsAccount().getCurrencyCode();
					if (mgrIssuer.getEarningsAccount() != null)
						mgrIssuer.getEarningsAccount().getCurrencyCode();
					if (mgrIssuer.getLossesAccount() != null)
						mgrIssuer.getLossesAccount().getCurrencyCode();
					if (mgrIssuer.getCardHolders() != null && mgrIssuer.getCardHolders()
							.size() > 0) /* !CollectionUtils.isEmpty(mgrIssuer.getCardHolders()) */
						mgrIssuer.getCardHolders().iterator().next();
					if (mgrIssuer.getCardProducts() != null && mgrIssuer.getCardProducts()
							.size() > 0 /* !CollectionUtils.isEmpty(mgrIssuer.getCardProducts()) */)
						mgrIssuer.getCardProducts().iterator().next();
					if (mgrIssuer.getAcquirers() != null && mgrIssuer.getAcquirers()
							.size() > 0 /* !CollectionUtils.isEmpty(mgrIssuer.getAcquirers()) */)
						mgrIssuer.getAcquirers().iterator().next();
				}
			}
			return mgrIssuers;
		});
		return items.stream();
	}

	// @Override
	public Issuer updateEntity(Issuer issuer) throws BLException {
		try {
			return DB.execWithTransaction((db) -> {
				Issuer oldIssuer = (Issuer) ((Issuer) getOriginalEntity()).clone();
				if (issuer != null) {
					Account assetsAcct;
					Account earningsAcct;
					Account lossesAcct;
					if (oldIssuer.getAssetsAccount() == null && issuer.getAssetsAccount() != null) {
						assetsAcct = db.session().get(Account.class, issuer.getAssetsAccount().getId());
						if (assetsAcct == null) // Account entered does not exist
							// New account
							issuer.setAssetsAccount(createNewAssetsAccount(issuer, db));
					}
					if (oldIssuer.getEarningsAccount() == null && issuer.getEarningsAccount() != null) {
						earningsAcct = db.session().get(Account.class, issuer.getEarningsAccount().getId());
						if (earningsAcct == null) // Account entered does not exist
							// New account
							issuer.setEarningsAccount(createNewEarningsAccount(issuer, db));
					}
					if (oldIssuer.getLossesAccount() == null && issuer.getLossesAccount() != null) {
						lossesAcct = db.session().get(Account.class, issuer.getLossesAccount().getId());
						if (lossesAcct == null) // Account entered does not exist
							// New account
							issuer.setLossesAccount(createNewLossesAccount(issuer, db));
					}
					db.session().merge(issuer);
					addRevisionUpdated(db, getEntityName(), String.valueOf(issuer.getId()), oldIssuer, issuer,
							new String[] { "institutionId", "active", "name", "tz", "startDate", "endDate", "journal",
									"assetsAccount", "earningsAccount", "lossesAccount" });
					return issuer;
				}
				return issuer;
			});
		} catch (Exception e) {
			throw new BLException(e.getMessage());
		}
	}

	@Override
	public Object saveEntity(Object o) {
		try {
			return DB.execWithTransaction(db -> {
				Issuer issuer = (Issuer) o;
				Account issuedAcct = null;
				Account feeAcct = null;
				Account lossesAcct = null;
				if (issuer.getAssetsAccount() != null)
					issuedAcct = db.session().get(Account.class, issuer.getAssetsAccount().getId());
				if (issuedAcct == null) {// Account entered does not exist New account.
					issuer.setAssetsAccount(createNewAssetsAccount(issuer, db));
				}
				if (issuer.getEarningsAccount() != null)
					feeAcct = db.session().get(Account.class, issuer.getEarningsAccount().getId());
				if (feeAcct == null) { // Account entered does not exist New account.
					issuer.setEarningsAccount(createNewEarningsAccount(issuer, db));
				}
				if (issuer.getLossesAccount() != null)
					lossesAcct = db.session().get(Account.class, issuer.getLossesAccount().getId());
				if (lossesAcct == null) { // Account entered does not exist New account.
					issuer.setLossesAccount(createNewLossesAccount(issuer, db));
				}
				db.save(issuer);
				addRevisionCreated(db, getEntityName(), getItemId(issuer));
				return issuer;
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			getApp().displayNotification(getApp().getMessage("errorMessage.unexpected"));
			return null;
		}
	}

	private CompositeAccount createNewAssetsAccount(Issuer issuer, DB db) throws Exception {
		Account root = getRootIssuersAssetsAccount();
		return createNewAccount(root, issuer.getAssetsAccount(), issuer.getName(), db);
	}

	private CompositeAccount createNewEarningsAccount(Issuer issuer, DB db) throws Exception {
		Account root = getRootIssuersEarningsAccount();
		return createNewAccount(root, issuer.getEarningsAccount(), issuer.getName(), db);
	}

	private CompositeAccount createNewLossesAccount(Issuer issuer, DB db) throws Exception {
		Account root = getRootIssuersLossesAccount();
		return createNewAccount(root, issuer.getLossesAccount(), issuer.getName(), db);
	}

	private CompositeAccount createNewAccount(Account parent, Account newAcct, String description, DB db)
			throws GLException {

		if (parent == null || newAcct == null) {
			return null;
		}
		newAcct.setCreated(new Date());
		newAcct.setType(parent.getType());
		newAcct.setDescription(description);
		GLSession gls = new GLSession(db);
		gls.session().refresh(parent);
		gls.addAccount((CompositeAccount) parent, newAcct);
		return (CompositeAccount) newAcct;
	}

	public Account getRootIssuersAssetsAccount() throws Exception {
		return getAccountByCodeInSysconfig("GL_ISSUERS_ASSETS_ACCT");
	}

	public Account getRootIssuersEarningsAccount() throws Exception {
		return getAccountByCodeInSysconfig("GL_ISSUERS_EARNINGS_ACCT");
	}

	public Account getRootIssuersLossesAccount() throws Exception {
		return getAccountByCodeInSysconfig("GL_ISSUERS_LOSSES_ACCT");
	}

	private Account getAccountByCodeInSysconfig(String sysconfigAcctId) throws Exception {
		return DB.exec(db -> {
			SysConfigManager sysConfigManager = new SysConfigManager(db);
			String chart = sysConfigManager.get("GL_CHART");
			if (sysConfigManager.hasProperty(sysconfigAcctId)) {
				String code = sysConfigManager.get(sysconfigAcctId);
				GLSession gls = new GLSession(db);
				return gls.getAccount(chart, code);
			}
			getApp().displayNotification(getApp().getMessage("errorMessage.missingConfig", sysconfigAcctId));
			return null;
		});
	}

}
