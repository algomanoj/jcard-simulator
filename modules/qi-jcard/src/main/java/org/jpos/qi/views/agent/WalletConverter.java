/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.qi.views.agent;

import org.jpos.ee.DB;
import org.jpos.ee.Wallet;
import org.jpos.ee.WalletManager;
import org.jpos.gl.Account;
import org.jpos.qi.QI;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;


/**
 * Created by alcarraz on 15/09/15 and here copied by KK.
 */
public class WalletConverter implements Converter {

    private boolean createNew;
    private boolean createFinal;
    private boolean required;

    public WalletConverter() {
        //By default we allow to leave the field empty but if there is a value we require that an account exists
        this(false, false);
    }

    public WalletConverter (boolean required, boolean createNew) {
        //By default we allow to leave the field empty but if there is a value we require that an account exists
        this(required, createNew, true);
    }

    public WalletConverter (boolean required, boolean createNew, boolean createFinal) {
        super();
        this.required = required;
        this.createNew = createNew;
        this.createFinal = createFinal;
    }

    @Override
    public Result convertToModel(Object obj, ValueContext context) {
    	String value = String.valueOf(obj);
    	if (value == null || value.isEmpty())
            return Result.ok(null);
        Wallet w;
        try {
            w = (Wallet) DB.exec(db -> {
                WalletManager mgr = new WalletManager(db);
                return mgr.getByWalletCode(value);
            });
            if (w == null)
                return Result.error(QI.getQI().getMessage("errorMessage.notExists",value));
            return Result.ok(w);
        } catch (Exception e) {
            QI.getQI().getLog().error(e);
            return Result.error(QI.getQI().getMessage("errorMessage.unexpected"));
        }
    }

    @Override
    public Object convertToPresentation(Object value, ValueContext context) {
    	Wallet wallet = null;
    	try {
    		wallet = (Wallet)value;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return (wallet == null) ? "" : wallet.getCode();
    }
    
    //@Override
    public String convertToPresentation(Wallet value, ValueContext context) {
        return (value == null) ? "" : value.getCode();
    }
}
