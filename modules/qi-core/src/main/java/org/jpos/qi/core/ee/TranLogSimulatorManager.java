package org.jpos.qi.core.ee;

import java.util.Date;

import org.jpos.ee.Card;
import org.jpos.ee.DB;
import org.jpos.ee.TranLogManager;

public class TranLogSimulatorManager extends TranLogManager {
	private String rrn;

	public TranLogSimulatorManager(DB db) {
		super(db);
	}
	public TranLogSimulatorManager(DB db, TranLogSimulatorFilter filter) {
		super(db, filter);
		this.rrn=filter.getRrn();
	}
	public TranLogSimulatorManager(DB db, Date date1, Date date2) {
		super(db, date1,date2);
	}
	public TranLogSimulatorManager(DB db, Date date1, Date date2, Card card) {
		super(db, date1,date2, card);
	}

	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
}
