package com.storme;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by brett on 26/03/15.
 */
@Config(emulateSdk = 18, manifest = "app/src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class StormeHelperTest {

    TestRecordStore store;

    @Before
    public void beforeTests() {
        ShadowLog.stream = System.out;
        store = new TestRecordStore(Robolectric.application);
    }

    @After
    public void after() {
        store.close();
    }

    @Test
    public void can_setup_helper() {
        assertNotNull(store);
    }

    @Test
    public void adding_a_blank_record_is_saved() {
        TestRecord record = new TestRecord();

        List<TestRecord> records = store.getAll(TestRecord.class, null, 0, 0);
        assertNotNull(records);
        int count = records.size();

        store.save(TestRecord.class, record);
        records = store.getAll(TestRecord.class, null, 0, 0);
        assertThat(records.size(), is(count + 1));
        assertThat(record.getCreatedDate() > 0, is(true));
        assertThat(record.getModifiedDate() > 0, is(true));
        assertThat(record.getDbVersion(), is(TestRecordStore.DATABASE_VERSION));
    }

    @Test
    public void adding_a_populated_record_is_saved() {
        String key = "hdjshasuighubfjbswfjhe";
        TestRecord record = getPopulatedRecord(key, 0);

        List<TestRecord> records = store.getAll(TestRecord.class, null, 0, 0);
        assertNotNull(records);
        int count = records.size();

        TestRecord saved = store.save(TestRecord.class, record);
        records = store.getAll(TestRecord.class, null, 0, 0);
        assertThat(records.size(), is(count+1));

        assertPopulatedRecord(saved, key, 0);
    }

    @Test
    public void deleting_a_record_is_performed() {
        TestRecord record = new TestRecord();

        List<TestRecord> records = store.getAll(TestRecord.class, null, 0, 0);
        assertNotNull(records);
        int count = records.size();

        TestRecord stored = store.save(TestRecord.class, record);
        records = store.getAll(TestRecord.class, null, 0, 0);
        assertThat(records.size(), is(count+1));

        store.delete(stored);
        records = store.getAll(TestRecord.class, null, 0, 0);
        assertThat(records.size(), is(count));
    }

    @Test
    public void deleting_with_where_is_performed() {
        TestRecord record = new TestRecord();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        try {
            store.delete(null, "dateField = ?", new String[]{String.valueOf(yesterday.getTime())});
            fail("Delete with no model should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.delete(TestRecord.class, null, new String[]{String.valueOf(yesterday.getTime())});
            fail("Delete with no where should fail");
        } catch (IllegalArgumentException e) {}

        record.setDateField(yesterday);
        store.save(TestRecord.class, record);
        record = new TestRecord();
        record.setDateField(yesterday);
        store.save(TestRecord.class, record);
        record = new TestRecord();
        record.setDateField(new Date());
        store.save(TestRecord.class, record);
        record = new TestRecord();
        record.setDateField(new Date());
        store.save(TestRecord.class, record);
        record = new TestRecord();
        record.setDateField(new Date());
        store.save(TestRecord.class, record);

        List<TestRecord> records = store.getAll(TestRecord.class, null, 0, 0);
        assertThat(records.size(), is(5));

        store.delete(TestRecord.class, "dateField = ?", new String[]{String.valueOf(yesterday.getTime())});

        records = store.getAll(TestRecord.class, null, 0, 0);
        assertThat(records.size(), is(3));
    }

    @Test
    public void deleting_all_records_is_performed() {

        List<TestRecord> records = store.getAll(TestRecord.class, null, 0, 0);
        assertNotNull(records);
        int count = records.size();

        TestRecord record = new TestRecord();
        store.save(TestRecord.class, record);
        record = new TestRecord();
        store.save(TestRecord.class, record);
        record = new TestRecord();
        store.save(TestRecord.class, record);
        record = new TestRecord();
        store.save(TestRecord.class, record);
        record = new TestRecord();
        store.save(TestRecord.class, record);

        records = store.getAll(TestRecord.class, null, 0, 0);
        assertThat(records.size(), is(count+5));

        store.deleteAll(TestRecord.class);
        records = store.getAll(TestRecord.class, null, 0, 0);
        assertThat(records.size(), is(0));
    }

    @Test
    public void can_update_record() {

        TestRecord record = new TestRecord();
        store.save(TestRecord.class, record);
        List<TestRecord> records = store.getAll(TestRecord.class, null, 0, 0);
        assertNotNull(records);
        int count = records.size();

        long id = record.getId();

        record.setStringField("hi");
        store.save(TestRecord.class, record);

        records = store.getAll(TestRecord.class, null, 0, 0);
        assertNotNull(records);
        assertThat(records.size(), is(count));

        TestRecord result = store.get(TestRecord.class, id);
        assertThat(result.getStringField(), is("hi"));
    }

    @Test
    public void check_delete_with_no_id_fails() {
        TestRecord record = new TestRecord();

        assertThat(record.getId(), is(0L));
        try {
            store.delete(record);
            fail("Attempt to delete a record with no id should fail");
        } catch(IllegalArgumentException e) {}
    }

    @Test
    public void can_find_a_record_by_a_field() {
        String key = "ahjjdshjdhjs123";
        TestRecord record = getPopulatedRecord(key, 0);

        assertThat(record.getStringField(), is(key));
        assertThat(record.getId(), is(0L));
        store.save(TestRecord.class, record);
        long id = record.getId();
        assertThat(id > 0L, is(true));

        TestRecord result = store.findByStringField(key);
        assertNotNull(result);
        assertThat(result.getId(), is(id));
    }

    @Test
    public void can_find_a_record_by_id() {
        String key = "ahjjdshjdhjs123";
        TestRecord record = getPopulatedRecord(key, 0);

        assertThat(record.getStringField(), is(key));
        assertThat(record.getId(), is(0L));
        store.save(TestRecord.class, record);
        long id = record.getId();
        assertThat(id > 0L, is(true));

        TestRecord result = store.findById(record.getId());
        assertNotNull(result);
        assertThat(result.getId(), is(id));
        assertThat(result.getStringField(), is(key));
    }

    @Test
    public void can_get_ordered_results() {
        store.deleteAll(TestRecord.class);

        TestRecord record1 = getPopulatedRecord("1", 0);
        TestRecord record2 = getPopulatedRecord("2", 1);
        TestRecord record3 = getPopulatedRecord("3", 2);
        TestRecord record4 = getPopulatedRecord("4", 3);
        TestRecord record5 = getPopulatedRecord("5", 4);

        store.save(TestRecord.class, record1);
        store.save(TestRecord.class, record2);
        store.save(TestRecord.class, record3);
        store.save(TestRecord.class, record4);
        store.save(TestRecord.class, record5);

        List<TestRecord> results = store.getAllOrderedByDate(true);
        for(int i = 0; i < results.size(); i++) {
            assertThat(results.get(i).getStringField(), is(String.valueOf(i + 1)));
        }

        results = store.getAllOrderedByDate(false);
        for(int i = 0; i < results.size(); i++) {
            assertThat(results.get(i).getStringField(), is(String.valueOf(5-i)));
        }

    }

    @Test
    public void count_works_as_expected() {
        store.deleteAll(TestRecord.class);

        for(int i = 0; i < 10; i++) {
            TestRecord record = getPopulatedRecord(String.valueOf(i + 1), i);
            store.save(TestRecord.class, record);
        }

        assertThat(store.findCount(TestRecord.class), is(10));
    }

    @Test
    public void paging_works_as_expected() {
        store.deleteAll(TestRecord.class);

        for(int i = 0; i < 10; i++) {
            TestRecord record = getPopulatedRecord(String.valueOf(i + 1), i);
            store.save(TestRecord.class, record);
        }
        assertThat(store.findCount(TestRecord.class), is(10));

        List<TestRecord> page1 = store.find(TestRecord.class, null, null, "dateField ASC", 1, 2);
        assertNotNull(page1);
        assertThat(page1.size(), is(2));
        assertThat(page1.get(0).getStringField(), is("1"));
        assertThat(page1.get(1).getStringField(), is("2"));

        List<TestRecord> page2 = store.find(TestRecord.class, null, null, "dateField ASC", 2, 2);
        assertNotNull(page2);
        assertThat(page2.size(), is(2));
        assertThat(page2.get(0).getStringField(), is("3"));
        assertThat(page2.get(1).getStringField(), is("4"));

        List<TestRecord> page5 = store.find(TestRecord.class, null, null, "dateField ASC", 5, 2);
        assertNotNull(page5);
        assertThat(page5.size(), is(2));
        assertThat(page5.get(0).getStringField(), is("9"));
        assertThat(page5.get(1).getStringField(), is("10"));

    }

    @Test
    public void check_valid_failure() {

        TestRecord record = store.get(TestRecord.class, 54885);
        assertNull(record);
    }

    @Test
    public void check_upgrade() {

        for(int i = 0; i < 10; i++) {
            TestRecord record = getPopulatedRecord(String.valueOf(i + 1), i);
            store.save(TestRecord.class, record);
        }
        assertThat(store.findCount(TestRecord.class), is(10));

        // cause an upgrade
        store.DATABASE_VERSION = 2;
        store = new TestRecordStore(Robolectric.application);

        // we are just dropping the tables for now
        assertThat(store.findCount(TestRecord.class), is(0));
    }

    @Test
    public void check_non_model_class_is_handled() {
        try {
            FakeRecord record = store.get(FakeRecord.class, 1);
            fail("Attempting to get an unknown model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            FakeRecord record = new FakeRecord();
            FakeRecord result = store.save(FakeRecord.class, record);
            fail("Attempting to save an unknown model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.getAll(FakeRecord.class, null, 1, 1);
            fail("Attempting to get an unknown model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.find(FakeRecord.class, null, null, null, 1, 1);
            fail("Attempting to find an unknown model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.findCount(FakeRecord.class);
            fail("Attempting to count an unknown model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            FakeRecord record = new FakeRecord();
            record.setId(10);
            store.delete(FakeRecord.class, record);
            fail("Attempting to delete an unknown model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.deleteAll(FakeRecord.class);
            fail("Attempting to delete all unknown model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.delete(FakeRecord.class, "cheese = ?", new String[] {"fromage"});
            fail("Attempting to delete all unknown model type should fail");
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void check_invalid_params() {
        try {
            TestRecord record = store.get(TestRecord.class, 0);
            fail("Attempting to get an model of zero id type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.get(null, 1);
            fail("Attempting to get a null model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.getAll(null, null, 1, 1);
            fail("Attempting to getAll of a null model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            TestRecord record = new TestRecord();
            TestRecord result = store.save(null, record);
            fail("Attempting to save an null model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            TestRecord record = new TestRecord();
            TestRecord result = store.save(TestRecord.class, null);
            fail("Attempting to save an null model obj should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.find(null, null, null, null, 1, 1);
            fail("Attempting to find a null model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.findCount(null);
            fail("Attempting to count a null model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            TestRecord record = new TestRecord();
            store.delete(null, record);
            fail("Attempting to delete a null model type should fail");
        } catch (IllegalArgumentException e) {}

        try {
            store.deleteAll(null);
            fail("Attempting to delete all null model type should fail");
        } catch (IllegalArgumentException e) {}
    }

    private TestRecord getPopulatedRecord(String stringFieldVal, int dateOffset) {
        TestRecord record = new TestRecord();
        record.setStringField(stringFieldVal);
        record.setBooleanField(true);

        Calendar cal = getCalendarDate(dateOffset);
        record.setDateField(cal.getTime());

        record.setIntegerField(1234);
        record.setLongField(123456789L);
        record.setDoubleField(56.78);
        record.setFloatField(876.11f);
        return record;
    }

    private Calendar getCalendarDate(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1999);
        cal.set(Calendar.MONTH, 3);
        cal.set(Calendar.DAY_OF_MONTH, 10 + offset);
        cal.set(Calendar.HOUR, 12);
        cal.set(Calendar.MINUTE, 35);
        cal.set(Calendar.SECOND, 45);
        cal.set(Calendar.MILLISECOND, 7654);
        return cal;
    }

    private void assertPopulatedRecord(TestRecord savedRecord, String stringFieldVal, int dateOffset) {
        assertThat(savedRecord.getStringField(), is(stringFieldVal));
        assertThat(savedRecord.getDateField(), equalTo(getCalendarDate(dateOffset).getTime()));
        assertThat(savedRecord.isBooleanField(), is(true));
        assertThat(savedRecord.getIntegerField(), is(1234));
        assertThat(savedRecord.getLongField(), is(123456789L));
        assertThat(savedRecord.getDoubleField(), is(56.78));
        assertThat(savedRecord.getFloatField(), is(876.11f));
    }


}
