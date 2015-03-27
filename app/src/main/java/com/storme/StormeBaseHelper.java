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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brett on 24/02/15.
 */
public abstract class StormeBaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase mDb;
    private Map<Class<? extends StormeModel>, StormeModelFactory> classToFactoryMap;

    public StormeBaseHelper(Context context, String dbName, int dbVersion, String tablePrefix, List<Class<? extends StormeModel>> models) {
        super(context, dbName, null, dbVersion);
        classToFactoryMap = new HashMap<Class<? extends StormeModel>, StormeModelFactory>();
        for(Class<? extends StormeModel> model : models) {
            StormeModelFactory factory = new StormeModelFactory(model, tablePrefix, dbVersion);
            classToFactoryMap.put(model, factory);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        mDb = database;
        if(database != null && database.isOpen()) {
            for(StormeModelFactory factory : classToFactoryMap.values()) {
                factory.createTable(database);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // for now just drop everything
        dropTables(database);
    }

    private void dropTables(SQLiteDatabase database) {
        if(database != null && database.isOpen()) {
            for(StormeModelFactory factory : classToFactoryMap.values()) {
                factory.dropTable(database);
            }
        }
    }

    protected <E extends StormeModel> E save(Class<E> modelClass, E record) {
        if(modelClass == null) {
            throw new IllegalArgumentException("Model class cannot be null");
        }
        if(record == null) {
            throw new IllegalArgumentException("Attempt to save null model object");
        }

        openDB();
        StormeModelFactory factory = classToFactoryMap.get(modelClass);
        if(factory != null) {
            if(record.getId() > 0) {
                factory.update(mDb, record);
            } else {
                factory.insert(mDb, record);
            }
            return record;
        }
        throw new IllegalArgumentException("Unknown model type passed to save method: " + modelClass.getName());
    }

    protected <E extends StormeModel> E get(Class<E> modelClass, long id) {
        if(modelClass == null) {
            throw new IllegalArgumentException("Model class cannot be null");
        }
        if(id <= 0) {
            throw new IllegalArgumentException("Attempt to get a record with zero or negative id");
        }
        openDB();
        StormeModelFactory factory = classToFactoryMap.get(modelClass);
        if(factory != null) {
            return (E)factory.get(id, mDb);
        }
        throw new IllegalArgumentException("Unknown model type passed to get method: " + modelClass.getName());
    }

    protected <E extends StormeModel> List<E> getAll(Class<E> modelClass, String order, int page, int pagesize) {
        if(modelClass == null) {
            throw new IllegalArgumentException("Model class cannot be null");
        }

        openDB();
        StormeModelFactory factory = classToFactoryMap.get(modelClass);
        if(factory != null) {
            List<E> list = factory.find(null, null, order, page, pagesize, mDb);
            return new ArrayList<E>(list);
        }
        throw new IllegalArgumentException("Unknown model type passed to getAll method: " + modelClass.getName());
    }

    protected <E extends StormeModel> List<E> find(Class<E> modelClass, String where, String[] whereParams, String order, int page, int pagesize) {
        if(modelClass == null) {
            throw new IllegalArgumentException("Model class cannot be null");
        }

        openDB();
        StormeModelFactory factory = classToFactoryMap.get(modelClass);
        if(factory != null) {
            return factory.find(where, whereParams, order, page, pagesize, mDb);
        }
        throw new IllegalArgumentException("Unknown model type passed to find method: " + modelClass.getName());
    }

    protected <E extends StormeModel> int findCount(Class<E> modelClass) {
        if(modelClass == null) {
            throw new IllegalArgumentException("Model class cannot be null");
        }
        openDB();
        StormeModelFactory factory = classToFactoryMap.get(modelClass);
        if(factory != null) {
            return factory.findCount(mDb);
        }
        throw new IllegalArgumentException("Unknown model type passed to findCount method: " + modelClass.getName());
    }

    protected <E extends StormeModel> void delete(Class<E> modelClass, E record) {
        if(modelClass == null) {
            throw new IllegalArgumentException("Model class cannot be null");
        }
        if(record.getId() <= 0) {
            throw new IllegalArgumentException("Attempt to delete a record with no id");
        }
        openDB();
        StormeModelFactory factory = classToFactoryMap.get(modelClass);
        if(factory != null) {
            factory.delete(record, mDb);
            return;
        }
        throw new IllegalArgumentException("Unknown model type passed to delete method: " + modelClass.getName());
    }

    protected <E extends StormeModel> void deleteAll(Class<E> modelClass) {
        if(modelClass == null) {
            throw new IllegalArgumentException("Model class cannot be null");
        }
        openDB();
        StormeModelFactory factory = classToFactoryMap.get(modelClass);
        if(factory != null) {
            factory.deleteAll(mDb);
            return;
        }
        throw new IllegalArgumentException("Unknown model type passed to delete method: " + modelClass.getName());
    }

    private void openDB()
    {
        if(mDb == null)
        {
            mDb = getWritableDatabase();
            onCreate(mDb);
        }
    }
}
