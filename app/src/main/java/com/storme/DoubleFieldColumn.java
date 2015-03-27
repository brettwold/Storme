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

public class DoubleFieldColumn extends FieldColumn {

    public DoubleFieldColumn() { super(Double.class); }

    @Override
    public String getColumnType() {
        return "REAL";
    }

    @Override
    public void setValueFromCursor(Object inst, Cursor cursor, int position) {
        setValue(inst, cursor.getDouble(position));
    }

    @Override
    public void addFieldValue(Object inst, ContentValues values) throws IllegalAccessException {
        values.put(name, field.getDouble(inst));
    }
}
