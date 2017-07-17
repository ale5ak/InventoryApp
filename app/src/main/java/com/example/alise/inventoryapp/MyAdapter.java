package com.example.alise.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alise.inventoryapp.data.InventoryContract;
import com.example.alise.inventoryapp.data.MyAsyncQueryHandler;

/**
 * Created by Aleš Pros on 13.07.2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private Cursor mDataset;
    private Context mContext;
    private MyAsyncQueryHandler mMyAsyncQueryHandler;
    private int mTitleColumn, mPriceColumn, mQuantityColumn, mIdColumn;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context) {
        mContext = context;
        mMyAsyncQueryHandler = new MyAsyncQueryHandler(mContext.getContentResolver(), mContext);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        mDataset.moveToPosition(position);

        final String title = mDataset.getString(mTitleColumn);
        final int price = mDataset.getInt(mPriceColumn);
        final int quantity = mDataset.getInt(mQuantityColumn);
        final int id = mDataset.getInt(mIdColumn);

        holder.mItemTitleTV.setText(title.substring(0, 1).toUpperCase() + title.substring(1));
        holder.mItemPriceTV.setText(holder.itemView.getResources().getString(R.string.price, price));
        holder.mItemtQuantityTV.setText(holder.itemView.getResources().getString(R.string.quantity, quantity));

        holder.mSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Uri uri = InventoryContract.ProductEntry.PRODUCTS_URI.buildUpon().appendPath(String.valueOf(id)).build();
                if (quantity > 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_QUANTITY, quantity - 1);
                    mMyAsyncQueryHandler.startUpdate(0, title, uri, contentValues, null, null);
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.error_quantity_negative), Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("id", id);
                mContext.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset == null) {
            return 0;
        }
        return mDataset.getCount();
    }

    public void swap(Cursor data) {
        if (mDataset != null) mDataset.close();
        mDataset = data;
        mDataset.setNotificationUri(mContext.getContentResolver(), InventoryContract.ProductEntry.PRODUCTS_URI);
        mTitleColumn = mDataset.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_TITLE);
        mPriceColumn = mDataset.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_PRICE);
        mQuantityColumn = mDataset.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_QUANTITY);
        mIdColumn = mDataset.getColumnIndexOrThrow(InventoryContract.ProductEntry._ID);
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mItemTitleTV, mItemPriceTV, mItemtQuantityTV;
        public Button mSaleButton;

        public ViewHolder(View v) {
            super(v);
            mItemTitleTV = v.findViewById(R.id.tv_item_title);
            mItemPriceTV = v.findViewById(R.id.tv_item_price);
            mItemtQuantityTV = v.findViewById(R.id.tv_item_quantity);
            mSaleButton = v.findViewById(R.id.bt_sale);
        }
    }
}