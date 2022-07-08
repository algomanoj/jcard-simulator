package org.jpos.util;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

public class RequestHandler extends Log implements ISORequestListener {

	@Override
	public boolean process(ISOSource source, ISOMsg m) {
		try {
			if ("2800".equals(m.getMTI())) {
				String responseCode = "0000";
				String action = m.getString(70);
				TransactionGenerator tg = NameRegistrar.get("txn-generator");
				switch (action) {
				case "100":
					tg.setRunningStatus("RUNNING");
					break;
				case "200":
					tg.setRunningStatus("STOPPED");
					break;
				case "300":
					responseCode = tg.isRunning()?"0000":"9999";
					break;
				}
				m.setResponseMTI();
				m.set(39, responseCode);
				source.send(m);
				return true;
			}
		} catch (Exception e) {
			warn("request-handler", e);
		}
		return false;
	}
}