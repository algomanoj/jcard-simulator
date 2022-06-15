package org.jpos.qi.util;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.State;

public class StateManager extends DBManager<State> {
    private String countryCode;
    
    public StateManager(DB db) {
        super(db, State.class);
    }
    public StateManager(DB db, String countryCode) {
        super(db, State.class);
        this.countryCode = countryCode;
    }
    public State getStateByName(String name) {
        return super.getItemByParam("name",name,true);
    }
    public State getStateById(Long id) {
        return super.getItemByParam("id",id,true);
    }

    @Override
    protected Predicate[] buildFilters(Root<State> root) {
        if (countryCode != null) {
            Predicate equalsBrand = db.session().getCriteriaBuilder().equal(root.get("countryCode"), countryCode);
            return new Predicate[] { equalsBrand };
        }
        return null;
    }
}
