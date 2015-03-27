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

import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brett on 20/02/15.
 */
public class ReflectionClassTableGenerator<T> {

    private static final String TAG = ReflectionClassTableGenerator.class.getSimpleName();

    public static final String ID_COLUMN_NAME = "id";

    private static final String TABLE_TEMPLATE = "CREATE TABLE IF NOT EXISTS %s (" +
            "\n" + ID_COLUMN_NAME + " INTEGER PRIMARY KEY" +
            "%s" +
            "\n);";


    private static Map<Class<?>, Class<? extends FieldColumn>> SUPPORTED_TYPE_COLUMNS = new HashMap<Class<?>, Class<? extends FieldColumn>>();
    static {
        SUPPORTED_TYPE_COLUMNS.put(Boolean.class, BooleanFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(boolean.class, BooleanFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(Long.class, LongFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(long.class, LongFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(Integer.class, IntegerFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(int.class, IntegerFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(String.class, StringFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(Date.class, DateFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(Float.class, FloatFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(float.class, FloatFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(Double.class, DoubleFieldColumn.class);
        SUPPORTED_TYPE_COLUMNS.put(double.class, DoubleFieldColumn.class);
    }

    private String tablePrefix;
    private Class<T> modelClass;
    private Map<String, FieldColumn> modelColumnNameFieldMap;
    private List<Field> modelFields;

    public ReflectionClassTableGenerator(Class<T> modelClass, String tablePrefix) {
        this.modelClass = modelClass;
        this.tablePrefix = tablePrefix;
    }

    public String getTableName() {
        return tableNameForClass(modelClass.getName());
    }

    public String tableNameForClass(String name) {
        return tablePrefix + name.replace(".", "_").replace("$", "_");
    }

    public String getCreateStatement() {

        StringBuilder columnsStatement = new StringBuilder();

        if(modelColumnNameFieldMap == null) {
            setupMaps();
        }

        for (FieldColumn fieldColumn : modelColumnNameFieldMap.values()) {
            columnsStatement.append(",\n")
                    .append(fieldColumn.getName())
                    .append(" ")
                    .append(fieldColumn.getColumnType());
        }
        return String.format(TABLE_TEMPLATE, getTableName(), columnsStatement.toString());
    }

    public FieldColumn getFieldForColumn(String columnName) {
        if(modelColumnNameFieldMap == null) {
            setupMaps();
        }
        return modelColumnNameFieldMap.get(columnName);
    }

    public Collection<FieldColumn> getModelFields() {
        if(modelColumnNameFieldMap == null) {
            setupMaps();
        }
        return modelColumnNameFieldMap.values();
    }

    private void setupMaps() {
        modelColumnNameFieldMap = new HashMap<String, FieldColumn>();
        modelFields = new ArrayList<Field>();

        List<Field> fields = new ArrayList<Field>();
        getAllFields(fields, modelClass);
        for (Field f : fields) {
            f.setAccessible(true);
            if (!f.isAnnotationPresent(StormeFieldIgnore.class)) {
                try {
                    FieldColumn column = columnFromField(f);
                    if (column != null) {
                        modelColumnNameFieldMap.put(column.getName(), column);
                        modelFields.add(f);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Unable to setup field mapping for field: " + f.getName());
                }
            }
        }
    }

    private void getAllFields(List<Field> fields, Class<?> type) {
        if (type.getSuperclass() != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            getAllFields(fields, type.getSuperclass());
        }
    }

    private FieldColumn columnFromField(Field field) throws IllegalAccessException, InstantiationException {

        if(SUPPORTED_TYPE_COLUMNS.containsKey(field.getType())) {
            Class<? extends FieldColumn> columnClass = SUPPORTED_TYPE_COLUMNS.get(field.getType());
            FieldColumn column = columnClass.newInstance();
            column.setName(field.getName());
            column.setField(field);
            return column;
        }

        return null;
    }
}


