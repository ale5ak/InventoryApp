package com.example.alise.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Aleš Pros on 13.07.2017.
 */

public final class InventoryContract {
    public static final String AUTHORITY = "com.example.alise.inventoryapp"; //authority == provider name
    public static final String PATH_PRODUCTS = "products";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private InventoryContract() {
    }

    /* Inner class that defines the table contents */
    public static class ProductEntry implements BaseColumns {
        public static final Uri PRODUCTS_URI = Uri.withAppendedPath(BASE_URI, PATH_PRODUCTS);
        public static final String TABLE_NAME = "products";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_MATERIAL = "material";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_IMAGE = "image";

    }
}