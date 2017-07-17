package com.example.alise.inventoryapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.alise.inventoryapp.data.InventoryContract.ProductEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private MyAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
