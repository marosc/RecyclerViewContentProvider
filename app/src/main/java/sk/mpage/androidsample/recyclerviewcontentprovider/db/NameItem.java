/*
 * Copyright (C) 2015 Maros Cavojsky, (mpage.sk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sk.mpage.androidsample.recyclerviewcontentprovider.db;

import android.content.ContentValues;

public class NameItem {
    public long id;
    public String name;
    public int gender;

    public final static int MALE = 0;
    public final static int FEMALE = 1;

    public NameItem(long id, String name, int gender) {
        this.id = id;
        this.name = name;
        this.gender = gender;
    }

    public NameItem(String name, int gender) {
        this.name = name;
        this.gender = gender;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(ItemContentProvider.NAMES.NAME, name);
        values.put(ItemContentProvider.NAMES.GENDER, gender);
        return values;
    }
}
