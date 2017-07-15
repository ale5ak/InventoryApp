package com.example.alise.inventoryapp;

import com.example.alise.inventoryapp.data.InventoryContract.ProductEntry;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.alise.inventoryapp.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private InventoryDbHelper mDbHelper;
    private MyAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mDbHelper = new InventoryDbHelper(this);
        //insertDummyData();
        //queryData();

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    public void openDetailActivity(View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }

    public void insertDummyData() {
        insertData("title001", 1, 100);
        insertData("title002", 2, 200);
        insertData("title003", 3, 300);
        insertData("title004", 4, 400);
        insertData("title005", 5, 500);
        insertData("title006", 6, 600);
        insertData("title007", 7, 700);
        insertData("title008", 8, 800);
        insertData("title009", 9, 900);
        insertData("title0010", 10, 1000);
    }

    public long insertData(String title, int price, int quantity) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME_TITLE, title);
        values.put(ProductEntry.COLUMN_NAME_PRICE, price);
        values.put(ProductEntry.COLUMN_NAME_QUANTITY, quantity);

        // Insert the new row, returning the primary key value of the new row
        return db.insert(ProductEntry.TABLE_NAME, null, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME_TITLE,
                ProductEntry.COLUMN_NAME_PRICE,
                ProductEntry.COLUMN_NAME_QUANTITY
        };

        return new CursorLoader(this, ProductEntry.PRODUCTS_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swap(data);
        if (data.getCount() == 0) {
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.empty_view).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
