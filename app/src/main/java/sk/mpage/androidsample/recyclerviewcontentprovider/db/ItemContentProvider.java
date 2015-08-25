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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.HashMap;


public class ItemContentProvider extends ContentProvider {

    public static final String AUTHORITY = "sk.mpage.androidsample.recyclerviewcontentprovider.provider";
    public static final String PATH = "names";
    public static final String URL = "content://" + AUTHORITY + "/" + PATH;
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final class NAMES {
        public static final String _ID = BaseColumns._ID;
        public static final String NAME = "name";
        public static final String GENDER = "gender";
    }

    private static HashMap<String, String> NAMES_PROJECTION_MAP;

    private static final int NAMES_DIR = 1;
    private static final int NAMES_ITEM = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH, NAMES_DIR);//for more than specific name
        uriMatcher.addURI(AUTHORITY, PATH + "/#", NAMES_ITEM);//for specific name
    }

    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "PEOPLE_NAMES";
    private static final String TABLE_NAME = "names";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_SQL =
            "CREATE TABLE " + TABLE_NAME
                    + "(" + NAMES._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAMES.NAME + " TEXT NOT NULL,"
                    + NAMES.GENDER + " INTEGER NULL);";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        return db == null ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case NAMES_DIR:
                queryBuilder.setProjectionMap(NAMES_PROJECTION_MAP);
                break;
            case NAMES_ITEM:
                queryBuilder.appendWhere(NAMES._ID + "=" + uri.getPathSegments().get(1));//TODO: getLastPathSegment()??
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = NAMES.NAME;
        }

        Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case NAMES_DIR:
                return "vnd.android.cursor.dir/vnd.example.names";
            case NAMES_ITEM:
                return "vnd.android.cursor.item/vnd.example.names";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long rowID = db.insert(TABLE_NAME, null, contentValues);

        if (rowID > 0) {
            Uri id_uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(id_uri, null);
            return id_uri;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case NAMES_DIR:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case NAMES_ITEM:
                long id = Long.valueOf(uri.getPathSegments().get(1));//TODO: getLastPathSegment() ??

                count = db.delete(TABLE_NAME, NAMES._ID + " = " + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "")
                        , selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case NAMES_DIR:
                count = db.update(TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case NAMES_ITEM:
                long id = Long.valueOf(uri.getPathSegments().get(1));//TODO: getLastPathSegment() ??

                count = db.update(TABLE_NAME, contentValues, NAMES._ID + " = " + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "")
                        , selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
