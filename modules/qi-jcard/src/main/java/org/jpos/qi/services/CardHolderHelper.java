package org.jpos.qi.services;

import java.util.List;

import org.jpos.ee.CardHolder;
import org.jpos.ee.DB;
import org.jpos.ee.State;
import org.jpos.qi.ViewConfig;
import org.jpos.qi.util.StateManager;

public class CardHolderHelper extends QIHelper {

	public CardHolderHelper(ViewConfig viewConfig) {
		super(CardHolder.class, viewConfig);
	}

	@Override
	public Object getEntityById (String id)  {
		try {
			return  DB.exec(db -> {
				db.session().enableFetchProfile("qi_cardholders_view");
				return db.session().get(clazz, Long.parseLong(id));
			});
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	@Override
	public String getItemId(Object item) {
		return String.valueOf(((CardHolder) item).getId());
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
