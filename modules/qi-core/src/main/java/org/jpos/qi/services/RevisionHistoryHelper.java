package org.jpos.qi.services;

import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.Revision;
import org.jpos.qi.ViewConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RevisionHistoryHelper extends QIHelper {
    public RevisionHistoryHelper(ViewConfig viewConfig) {
        super(Revision.class, viewConfig);
    }

    @Override
    public String getItemId(Object item) {
        return null;
    }

//    public Object getEntityById (String id)  {
//        //most id are longs, if id is of other type this method must be overridden
//        try {
//            return  DB.exec(db ->  {
//                db.session().enableFetchProfile("eager");
//                return db.session().get(clazz, Long.parseLong(id));
//            });
//        } catch (Exception e) {
//            getApp().getLog().error(e);
//            return null;
//        }
//    }

    public Stream getAll(int offset, int limit, Map<String, Boolean> orders) throws Exception {
        List items = DB.exec(db -> {
            db.session().enableFetchProfile("eager");
            DBManager mgr = new DBManager(db, clazz);
            return mgr.getAll(offset,limit,orders);
        });
        return items.stream();
    }

    protected String getAuthorLink(String nickAndId, String currentRevision) {
        String[] data = nickAndId.split("\\(|\\)");
        String backRoute = "revision_history" + (!currentRevision.isEmpty() ? "." + currentRevision : "");
        return "<a href=\"#!/users/" + data[1] + "?back="+ backRoute +"\">" + nickAndId + "</a>";
    }
}
