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

import java.util.Date;

/**
 * Created by brett on 24/02/15.
 */

public class DateFieldColumn extends FieldColumn {

    public DateFieldColumn() { super(Date.class); }

    @Override
    public String getColumnType() {
        return "INTEGER";
    }

    @Override
    public void setValueFromCursor(Object inst, Cursor cursor, int position) {
        long dateVal = cursor.getLong(position);
        Date setVal = new Date(dateVal);
        setValue(inst, setVal);
    }

    @Override
    public void addFieldValue(Object inst, ContentValues values) throws IllegalAccessException {
        Date d = (Date)field.get(inst);
        if(d != null) {
            values.put(name, d.getTime());
        } else {
            values.put(name, 0);
        }
    }
}
