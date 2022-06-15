package org.jpos.qi.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.jpos.qi.QI;

import com.vaadin.flow.component.UI;

/**
 * Created by spr on 10/22/15.
 */
public class DateRange {
    public static String LAST_HOUR  = "daterange.lasthour";
    public static String TODAY      = "daterange.today";
    public static String YESTERDAY  = "daterange.yesterday";
    public static String THIS_WEEK  = "daterange.thisweek";
    public static String LAST_WEEK  = "daterange.lastweek";
    public static String THIS_MONTH = "daterange.thismonth";
    public static String LAST_MONTH = "daterange.lastmonth";
    public static String THIS_YEAR  = "daterange.thisyear";
    public static String ALL_TIME   = "daterange.alltime";
    public static String[] ranges   = new String[] {
            LAST_HOUR, TODAY, YESTERDAY, THIS_WEEK, LAST_WEEK, THIS_MONTH, LAST_MONTH, THIS_YEAR, ALL_TIME
    };
    private String range;
    private Date start;
    private Date end;
    public Map<String, Date> startDate = createStartDateMap();
    public Map<String, Date> endDate = createEndDateMap();

    public DateRange() {
        super();
    }

    public DateRange(String range) {
        this();
        if (startDate.containsKey(range) && endDate.containsKey(range)) {
            setStart(startDate.get(range));
            setEnd(endDate.get(range));
            setRange(range);
        }
    }

    private static Map<String, Date> createStartDateMap () {
        DateTime dt = DateTime.now().millisOfDay().withMinimumValue();
        Map<String, Date> map = new HashMap<>();
        map.put(LAST_HOUR,  DateTime.now().minusHours(1).toDate());
        map.put(TODAY,      dt.toDate());
        map.put(YESTERDAY,  dt.minusDays(1).toDate());
        map.put(THIS_WEEK,  dt.dayOfWeek().withMinimumValue().toDate());
        map.put(LAST_WEEK,  dt.dayOfWeek().withMinimumValue().minusWeeks(1).toDate());
        map.put(THIS_MONTH, dt.dayOfMonth().withMinimumValue().toDate());
        map.put(LAST_MONTH, dt.dayOfMonth().withMinimumValue().minusMonths(1).toDate());
        map.put(THIS_YEAR,  dt.dayOfYear().withMinimumValue().toDate());
        map.put(ALL_TIME,   null);
        return map;
    }

    private static Map<String, Date> createEndDateMap () {
        DateTime dt = DateTime.now().millisOfDay().withMaximumValue();
        Map<String, Date> map = new HashMap<>();
        map.put(LAST_HOUR,  DateTime.now().toDate());
        map.put(TODAY,      dt.toDate());
        map.put(YESTERDAY,  dt.minusDays(1).toDate());
        map.put(THIS_WEEK,  dt.dayOfWeek().withMaximumValue().toDate());
        map.put(LAST_WEEK,  dt.dayOfWeek().withMaximumValue().minusWeeks(1).toDate());
        map.put(THIS_MONTH, dt.dayOfMonth().withMaximumValue().toDate());
        map.put(LAST_MONTH, dt.dayOfMonth().withMaximumValue().minusMonths(1).toDate());
        map.put(THIS_YEAR,  dt.dayOfYear().withMaximumValue().toDate());
        map.put(ALL_TIME,   null);
        return map;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String toString() {
        if (getRange() != null)
            return getRange();
        DateTime from = new DateTime(getStart());
        DateTime to = new DateTime(getEnd());
        return from.toString() + " - " + to.toString();

    }
}
