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
public interface StormeModel {

    public long getId();

    public void setId(long id);

    public long getCreatedDate();

    public void setCreatedDate(long createdDate);

    public long getModifiedDate();

    public void setModifiedDate(long modifiedDate);

    public int getDbVersion();

    public void setDbVersion(int version);

}
