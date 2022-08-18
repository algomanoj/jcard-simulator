package org.jpos.qi.services;

import java.util.List;

import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.CardHolderManager;
import org.jpos.ee.CardManager;
import org.jpos.ee.DB;
import org.jpos.gl.GLException;
import org.jpos.iso.ISOUtil;
import org.jpos.qi.ViewConfig;
import org.jpos.security.SMException;
import org.jpos.security.jceadapter.SSM;
import org.jpos.util.NameRegistrar;

public class SearchHelper extends JCardQIHelper {

	public SearchHelper() {
		super(Card.class);
	}

	@Override
	public String getItemId(Object item) {
		return String.valueOf(((Card) item).getId());
	}

	public Card getCardById(long id) {
		try {
			return DB.exec((db) -> {
				CardManager mgr = new CardManager(db);
				return mgr.getById(id);
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	public Card getCardByNumber(String pan) throws NameRegistrar.NotFoundException, SMException {

		try {
			return DB.exec((db) -> {
				CardManager mgr = new CardManager(db);
				if (pan != null)
					return mgr.getByHash(ISOUtil.hexString(getSSM().SHA(pan)));
				return null;
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	public Card getCardByToken(String token) {
		try {
			return DB.exec((db) -> {
				CardManager mgr = new CardManager(db);
				return mgr.getByToken(token);
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	public List<Card> getCardByAcctCode(String acctCode) throws GLException {
		try {
			return DB.exec((db) -> {
				db.session().enableFetchProfile("qi_card_product_holders_account_view");
				CardManager mgr = new CardManager(db);
				return mgr.getByAcctCode(acctCode);
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	public List<CardHolder> getCardHolderByName(String name) {
		try {
			return (List<CardHolder>) DB.exec((db) -> {
				CardHolderManager mgr = new CardHolderManager(db);
				return mgr.getCardHolderByName(name);
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	public List<CardHolder> getCardHolderByRealId(String realId) {
		try {
			return (List<CardHolder>) DB.exec((db) -> {
				CardHolderManager mgr = new CardHolderManager(db);
				return mgr.getCardHoldersByRealId(realId);
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
		
		
	}

	protected SSM getSSM() throws NameRegistrar.NotFoundException {
		return (SSM) NameRegistrar.get("ssm");
	}
}
