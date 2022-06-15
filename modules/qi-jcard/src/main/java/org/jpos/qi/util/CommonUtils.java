package org.jpos.qi.util;

import java.util.Collection;
import java.util.Map;

public class CommonUtils {
	
	//Collection
	public static boolean isEmpty( Collection<?> collection) {
		return (collection == null || collection.isEmpty());
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}

	//String 
	public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
