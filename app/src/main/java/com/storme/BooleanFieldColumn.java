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

/**
 * Created by brett on 24/02/15.
 */
public class BooleanFieldColumn extends FieldColumn {

    public BooleanFieldColumn() { super(Boolean.class); }

    @Override
    public String getColumnType() { return "INTEGER"; }

    @Override
    public void setValueFromCursor(Object inst, Cursor cursor, int position) {
        int val = cursor.getInt(position);
        boolean setVal = false;
        if(val > 0) {
            setVal = true;
        }
        setValue(inst, setVal);
    }

    @Override
    public void addFieldValue(Object inst, ContentValues values) throws IllegalAccessException {
        field.setAccessible(true);
        boolean val = field.getBoolean(inst);
        if (val) {
            values.put(name, 1);
        } else {
            values.put(name, 0);
        }
    }
}
