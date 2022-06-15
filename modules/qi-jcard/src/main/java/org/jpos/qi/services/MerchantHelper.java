package org.jpos.qi.services;

import java.util.List;

import org.jpos.ee.DB;
import org.jpos.ee.Merchant;
import org.jpos.ee.State;
import org.jpos.qi.ViewConfig;
import org.jpos.qi.util.StateManager;

public class MerchantHelper extends QIHelper {

	public MerchantHelper(ViewConfig viewConfig) {
		super(Merchant.class, viewConfig);
	}

	@Override
	public String getItemId(Object item) {
		return String.valueOf(((Merchant) item).getId());
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
}
