/**
 *  Copyright 2015 Brett Cherrington
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 **/
package com.storme;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by brett on 20/02/15.
 */
public class StormeModelFactory<T extends StormeModel> {

    private static final String TAG = StormeModelFactory.class.getSimpleName();

    private static final String DROP_TEMPLATE = "DROP TABLE IF EXISTS %s";
    private static final String TRUNCATE_TEMPLATE = "DELETE FROM %s";

    private Class<T> modelClass;
    ReflectionClassTableGenerator<T> reflectionClassTableGenerator;

    private int dbVersion;
    private String tableName;

    public StormeModelFactory(Class<T> modelClass, String tablePrefix, int dbVersion) {
        this.modelClass = modelClass;
        this.dbVersion = dbVersion;
        reflectionClassTableGenerator = new ReflectionClassTableGenerator<T>(modelClass, tablePrefix);
        tableName = reflectionClassTableGenerator.getTableName();
    }

    public void createTable(SQLiteDatabase db) {
        String createStatement = reflectionClassTableGenerator.getCreateStatement();
        db.execSQL(createStatement);
    }

    public T get(long id, SQLiteDatabase db)
    {
        Cursor cursor = db.query(tableName, getSelectColumns(), ReflectionClassTableGenerator.ID_COLUMN_NAME + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        try {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    return setupFromCursor(getNewInstance(), cursor);
                } else {
                    Log.e(TAG, "Failed to find DbModel object with id: " + id);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create DbModel object", e);
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public <T> int findCount(SQLiteDatabase db)
    {
        int count = 0;
        Cursor curCount= db.rawQuery("select count(*) from " + tableName, null);
        if(curCount != null) {
            curCount.moveToFirst();
            count= curCount.getInt(0);
            curCount.close();
        }
        return count;
    }

    public List<T> find(String where, String[] whereParams, String order, int page, int pagesize, SQLiteDatabase db)
    {
        Cursor cursor = null;
        if(page > 0 && pagesize > 0) {
            String limit = "";
            if(page > 1) {
                limit = ((page-1)*pagesize) + "," + pagesize;
            } else {
                limit = "" + pagesize;
            }
            cursor = db.query(tableName, getSelectColumns(), where, whereParams, null, null, order, limit);
        } else {
            cursor = db.query(tableName, getSelectColumns(), where, whereParams, null, null, order, null);
        }

        try {
            if (cursor != null) {
                List<T> orderedList = new ArrayList<T>();
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    try {
                        T copy = modelClass.newInstance();
                        setupFromCursor(copy, cursor);
                        orderedList.add(copy);
                        cursor.moveToNext();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to create DB object copy", e);
                    }
                }
                return orderedList;
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public void dropTable(SQLiteDatabase db)
    {
        String drop = String.format(DROP_TEMPLATE, tableName);
        db.execSQL(drop);
    }

    public int update(SQLiteDatabase db, T obj)
    {
        long now = System.currentTimeMillis();
        obj.setModifiedDate(now);

        ContentValues values = addModelValues(obj);
        return db.update(tableName, values,  ReflectionClassTableGenerator.ID_COLUMN_NAME + " = ?", new String[] { String.valueOf(obj.getId()) });
    }

    public void insert(SQLiteDatabase db, T obj)
    {
        long now = System.currentTimeMillis();
        obj.setModifiedDate(now);
        obj.setCreatedDate(now);
        obj.setDbVersion(dbVersion);

        ContentValues values = addModelValues(obj);

        long id = db.insert(tableName, null, values);
        if(id < 0) {
            Log.e(TAG, "Failed to insert object into DbModel: " + tableName + ":" + id);
        } else {
            obj.setId(id);
        }
    }

    public void delete(T obj, SQLiteDatabase db) {
        db.delete(tableName, ReflectionClassTableGenerator.ID_COLUMN_NAME + " = ?", new String[] { String.valueOf(obj.getId()) });
    }

    public void delete(String where, String[] whereParams, SQLiteDatabase db) {
        db.delete(tableName, where, whereParams);
    }

    public void deleteAll(SQLiteDatabase db) {
        db.execSQL(String.format(TRUNCATE_TEMPLATE, tableName));
    }

    private String[] getSelectColumns() {
        return new String[] {"*"};
    }

    private T getNewInstance() throws IllegalAccessException, InstantiationException {
        T inst = modelClass.newInstance();
        return inst;
    }

    private T setupFromCursor(T inst, Cursor cursor) {
        if (cursor != null && cursor.getColumnCount() > 0) {
            int count = 0;

            while (count < cursor.getColumnCount()) {
                String colName = cursor.getColumnName(count);
                if(colName.equals(ReflectionClassTableGenerator.ID_COLUMN_NAME)) {
                    inst.setId(cursor.getLong(count));
                } else {
                    FieldColumn fieldColumn = reflectionClassTableGenerator.getFieldForColumn(colName);
                    if (fieldColumn != null) {
                        fieldColumn.setValueFromCursor(inst, cursor, count);
                    } else {
                        Log.e(TAG, "Failed to find field for column in data from DB: " + colName);
                    }
                }
                count++;
            }
        } else {
            Log.e(TAG, "Empty cursor during setup of DbModel instance");
        }

        return inst;
    }

    private ContentValues addModelValues(T inst) {
        ContentValues values = new ContentValues();
        Collection<FieldColumn> fields = reflectionClassTableGenerator.getModelFields();

        for(FieldColumn f : fields) {
            try {
                f.addFieldValue(inst, values);
            } catch(Exception e) {
                Log.e(TAG, "Failed to get field value: " + f.getName(), e);
            }
        }
        return values;
    }
}
