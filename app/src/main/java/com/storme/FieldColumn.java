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
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by brett on 24/02/15.
 */
public abstract class FieldColumn<T> {

    protected static final String TAG = FieldColumn.class.getSimpleName();

    protected Class<T> fieldType;
    protected String name;
    protected Field field;

    public FieldColumn(Class<T> fieldType) {
        this.fieldType = fieldType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public abstract String getColumnType();

    public abstract void setValueFromCursor(Object inst, Cursor cursor, int position);

    protected void setValue(Object inst, Object val) {
        try {
            field.set(inst, val);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Failed to set value into DbModel instance", e);
        }
    }

    public abstract void addFieldValue(Object inst, ContentValues values) throws IllegalAccessException;
}
