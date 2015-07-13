package com.storme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by brett on 13/07/15.
 */
public class SQLiteInstance extends SQLiteOpenHelper {

    private static SQLiteInstance instance;

    private String DB_LOCK = "dblock";

    private SQLiteDatabase mDb;
    private StormeBaseHelper stormeBaseHelper;

    public static synchronized SQLiteInstance getInstance(Context context, String dbName, int dbVersion, StormeBaseHelper stormeBaseHelper) {

        if (instance == null) {
            instance = new SQLiteInstance(context.getApplicationContext(), dbName, dbVersion);
        }
        return instance;
    }

    private SQLiteInstance(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        mDb = database;
        stormeBaseHelper.handleCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        stormeBaseHelper.handleUpgrade(database);
    }

    public SQLiteDatabase getDatabase() {
        return mDb;
    }

    public void openDB()
    {
        synchronized (DB_LOCK) {
            if (mDb == null) {
                mDb = getWritableDatabase();
                onCreate(mDb);
            }
        }
    }

    public void close() {
        synchronized (DB_LOCK) {
            if (mDb != null) {
                mDb.close();
                mDb = null;
            }
        }
    }
}
