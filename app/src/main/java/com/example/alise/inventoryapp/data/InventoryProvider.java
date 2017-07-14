package com.example.alise.inventoryapp.data;

import com.example.alise.inventoryapp.data.InventoryContract.ProductEntry;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Aleš Pros on 13.07.2017.
 */

public class InventoryProvider extends ContentProvider {

    /*
    * Defines a handle to the database helper object. The MainDatabaseHelper class is defined
    * in a following snippet.
    */
    private InventoryDbHelper mOpenHelper;

    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize. For this snippet, only the calls for table 3 are shown.
         */

        /*
         * Sets the integer value for multiple rows in table 3 to 1. Notice that no wildcard is used
         * in the path
         */
        sUriMatcher.addURI(InventoryContract.AUTHORITY, InventoryContract.PATH_PRODUCTS, 1);

        /*
         * Sets the code for a single row to 2. In this case, the "#" wildcard is
         * used. "content://com.example.app.provider/table3/3" matches, but
         * "content://com.example.app.provider/table3 doesn't.
         */
        sUriMatcher.addURI(InventoryContract.AUTHORITY, InventoryContract.PATH_PRODUCTS + "/#", 2);
    }

    @Override
    public boolean onCreate() {
        /*
         * Creates a new helper object. This method always returns quickly.
         * Notice that the database itself isn't created or opened
         * until SQLiteOpenHelper.getWritableDatabase is called
         */
        mOpenHelper = new InventoryDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        switch (sUriMatcher.match(uri)) {
            // If the incoming URI was for all of table products
            case 1:
                return db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

            // If the incoming URI was for a single row
            case 2:
                /*
                 * Because this URI was for a single row, the _ID value part is
                 * present. Get the last path segment from the URI; this is the _ID value.
                 * Then, append the value to the WHERE clause for the query
                 */
                selection = ProductEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                return db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

            default:
                // If the URI is not recognized, you should do some error handling here.
                throw new Error("Unknown uri");
        }


    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        //Check if the same item isn't already in the table
        String selection =
                ProductEntry.COLUMN_NAME_TITLE + "=? AND " +
                ProductEntry.COLUMN_NAME_PRICE + "=? AND " +
                ProductEntry.COLUMN_NAME_MATERIAL + "=?";

        String[] selectionArgs = new String[]{
                contentValues.getAsString(ProductEntry.COLUMN_NAME_TITLE),
                contentValues.getAsString(ProductEntry.COLUMN_NAME_PRICE),
                contentValues.getAsString(ProductEntry.COLUMN_NAME_MATERIAL),
        };

        Cursor cursor = db.query(ProductEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        long newRowId;

        //if there is the same item in the table update the quantity of that item instead of inserting a new row
        if (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID));

            int quantityInDb = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_QUANTITY));
            int quantityToAdd = Integer.parseInt(contentValues.getAsString(ProductEntry.COLUMN_NAME_QUANTITY));

            ContentValues contentValuesForUpdate = new ContentValues();
            contentValuesForUpdate.put(ProductEntry.COLUMN_NAME_QUANTITY, quantityInDb + quantityToAdd);

            Uri newUri = InventoryContract.ProductEntry.PRODUCTS_URI.buildUpon().appendPath(String.valueOf(id)).build();
            update(newUri, contentValuesForUpdate, null, null);

            newRowId = id;
        } else {
            newRowId = db.insert(ProductEntry.TABLE_NAME, null, contentValues);

            if (newRowId != -1) {
                getContext().getContentResolver().notifyChange(uri, null);
            } else {
                return null;
            }
        }

        return ProductEntry.PRODUCTS_URI.buildUpon().appendPath(String.valueOf(newRowId)).build();
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        selection = ProductEntry._ID + "=?";
        selectionArgs = new String[]{uri.getLastPathSegment()};

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int rows = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
        if (rows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        selection = ProductEntry._ID + "=?";
        selectionArgs = new String[]{uri.getLastPathSegment()};

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int rows = db.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }
}
