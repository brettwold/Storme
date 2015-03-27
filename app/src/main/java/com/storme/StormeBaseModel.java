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

/**
 * Created by brett on 23/02/15.
 */
public abstract class StormeBaseModel implements StormeModel {

    @StormeFieldIgnore
    private long id;

    private long createdDate;

    private long modifiedDate;

    private int dbVersion;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getCreatedDate() {
        return createdDate;
    }

    @Override
    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public long getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public int getDbVersion() {
        return dbVersion;
    }

    @Override
    public void setDbVersion(int version) {
        this.dbVersion = version;
    }

}
