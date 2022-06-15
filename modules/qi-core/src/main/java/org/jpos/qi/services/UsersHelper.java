package org.jpos.qi.services;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import org.jpos.ee.*;
import org.jpos.qi.ViewConfig;
import java.util.List;

public class UsersHelper extends QIHelper {

    public UsersHelper (ViewConfig viewConfig) {
        super(User.class, viewConfig);
    }

    @Override
    public String getItemId(Object item) {
        return String.valueOf(((User)item).getId());
    }

    @Override
    public List<Validator> getValidators(String propertyId) {
        List<Validator> validators = super.getValidators(propertyId);
        if ("nick".equals(propertyId)) {
            validators.add(getNickTakenValidator());
        }
        return validators;
    }

    public List<Role> getRoles() {
        try {
            return DB.exec((db) -> {
                RoleManager mgr = new RoleManager(db);
                return mgr.getAll();
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }

    public Validator getNickTakenValidator() {
        return (Validator<String>) (value, context) -> {
            String oldNick = getOriginalEntity() != null ? ((User) getOriginalEntity()).getNick() : null;
            if (oldNick != null) {
                User u = getUserByNick((String) value, true);
                if (u == null || u.getId().equals(((User) getOriginalEntity()).getId())) {
                    return ValidationResult.ok();
                }
                return ValidationResult.error(getApp().getMessage("errorMessage.nickAlreadyExists", value));
            } else {
                if (getUserByNick(value, true) == null) {
                    return ValidationResult.ok();
                }
                return ValidationResult.error(getApp().getMessage("errorMessage.nickAlreadyExists", value));
            }
        };
    }

    public User getUserByNick (String nick, boolean includeDeleted) {
        try {
            return DB.exec((db) -> {
                UserManager mgr = new UserManager(db);
                return mgr.getUserByNick(nick,includeDeleted);
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }
}
