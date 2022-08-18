package org.jpos.qi.services;

import org.jpos.ee.*;
import org.jpos.gl.Layer;
import org.jpos.qi.ViewConfig;
import java.util.List;

public abstract class JCardQIHelper extends QIHelper {

    public JCardQIHelper (Class<Card> clazz) {
        super(clazz);
    }

    public JCardQIHelper (Class clazz, ViewConfig viewConfig) {
        super(clazz, viewConfig);
    }

   
}
