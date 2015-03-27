package com.storme;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by brett on 27/02/15.
 */
public class TestRecordStore extends StormeBaseHelper {

    private static final String DATABASE_NAME = "test_records";
    private static final String TABLE_PREFIX = "storme_example_";

    // public and non final for test purposes only. Normally this should be private and final
    public static int DATABASE_VERSION = 1;


    private static List<Class<? extends StormeModel>> DB_CLASSES = new ArrayList<Class<? extends StormeModel>>();

    static {
        DB_CLASSES.add(TestRecord.class);
    }

    public TestRecordStore(Context context)
    {
        super(context, DATABASE_NAME, DATABASE_VERSION, TABLE_PREFIX, DB_CLASSES);
    }

    public TestRecord findByStringField(String field) {
        List<TestRecord> list = find(TestRecord.class, "stringField = ? ", new String[]{String.valueOf(field)}, "dateField DESC", 0, 0);
        if(list != null && list.size() >= 1) {
            return list.get(0);
        }
        return null;
    }

    public TestRecord findById(long id) {
        return get(TestRecord.class, id);
    }

    public List<TestRecord> getAllOrderedByDate(boolean dir) {
        Date date = new Date();
        long start = getStartOfDay(date);
        long end = getEndOfDay(date);

        String dirStr = "ASC";
        if(!dir) {
            dirStr = "DESC";
        }

        List<TestRecord> list = find(TestRecord.class, "dateField >= ? and dateField <= ?", new String[] {String.valueOf(start), String.valueOf(end)}, "dateField " + dirStr, 0, 0);
        return new ArrayList<TestRecord>(list);
    }

    public void delete(TestRecord record) {
        delete(TestRecord.class, (TestRecord) record);
    }

    private long getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 23, 59, 59);
        return calendar.getTimeInMillis();
    }
}
