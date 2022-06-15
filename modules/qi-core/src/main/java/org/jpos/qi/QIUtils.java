package org.jpos.qi;

import org.apache.commons.lang3.StringUtils;

public class QIUtils {
    public static String getCaptionFromId (String id) {
        return getCaptionFromId(id, true);
    }
    
    public static String getCaptionFromId (String id, boolean capitalize) {
        //try to get caption from messages file
        String fieldPrefix="field.";
        String columnPrefix="column.";
        String caption = QI.getQI().getMessage(id);
        if (caption.equals(id)) {
            //try to get caption without prefix
            id = id.startsWith(fieldPrefix) ? id.substring(fieldPrefix.length()) : (id.startsWith(columnPrefix) ? id.substring(columnPrefix.length()) : id);
            caption = QI.getQI().getMessage(id);
            if (caption.equals(id)) {
                //parse existing id to a readable format
                String parsed = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(id), ' ');
                caption = parsed;
            }
        }
        return capitalize ? StringUtils.capitalize(caption) : caption;
    }
}
