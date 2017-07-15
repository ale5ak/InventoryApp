package com.example.alise.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.alise.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * Created by Aleš Pros on 13.07.2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "Inventory.db";

    private static final String SQL_CREATE_ENTRIES =

            "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +

                    ProductEntry._ID +                      " INTEGER PRIMARY KEY," +
                    ProductEntry.COLUMN_NAME_TITLE +        " TEXT NOT NULL," +
                    ProductEntry.COLUMN_NAME_PRICE +        " INT NOT NULL," +
                    ProductEntry.COLUMN_NAME_MATERIAL +     " TEXT NOT NULL," +
                    ProductEntry.COLUMN_NAME_QUANTITY +     " INT NOT NULL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}