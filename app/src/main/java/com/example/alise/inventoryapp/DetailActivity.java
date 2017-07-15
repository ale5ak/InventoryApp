package com.example.alise.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alise.inventoryapp.data.InventoryContract;
import com.example.alise.inventoryapp.data.MyAsyncQueryHandler;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private TextView mIdLabelTV, mIdValueTV;
    private EditText mTitleET, mPriceET, mMaterialET, mQuantityET;
    private int mQuantityInt;
    private Toast errorQuantityBiggerThan1;
    private MyAsyncQueryHandler mMyAsyncQueryHandler;
    private Uri mUri;
    private boolean mIsNewProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        errorQuantityBiggerThan1 = Toast.makeText(this, getString(R.string.error_quantity_bigger_than_0), Toast.LENGTH_LONG);

        Intent intentThatStartedThisActivity = getIntent();
        int id = intentThatStartedThisActivity.getIntExtra("id", -1);

        if (id != -1) {
            mIsNewProduct = false;
            mUri = InventoryContract.ProductEntry.PRODUCTS_URI.buildUpon().appendPath(String.valueOf(id)).build();
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            mIsNewProduct = true;
        }

        mIdLabelTV = (TextView) findViewById(R.id.tv_detail_id_label);
        mIdValueTV = (TextView) findViewById(R.id.tv_detail_id_value);
        mTitleET = (EditText) findViewById(R.id.et_detail_title);
        mPriceET = (EditText) findViewById(R.id.et_detail_price);
        mMaterialET = (EditText) findViewById(R.id.et_detail_material);
        mQuantityET = (EditText) findViewById(R.id.et_detail_quantity);

        mQuantityET.addTextChangedListener(new MyTextWatcher());

        String quantityString = mQuantityET.getText().toString();
        mQuantityInt = Integer.parseInt(quantityString);

        mMyAsyncQueryHandler = new MyAsyncQueryHandler(getContentResolver(), this);
    }

    public void reduceQuantity(View view) {
        if (mQuantityInt > 1) {
            String quantityFinal = String.valueOf(--mQuantityInt);
            mQuantityET.setText(quantityFinal);
        } else {
            errorQuantityBiggerThan1.show();
        }
    }

    public void addQuantity(View view) {
        String quantityFinal = String.valueOf(++mQuantityInt);
        mQuantityET.setText(quantityFinal);
    }

    public void orderFromTheSupplier(View view) {
        String[] values = getValues();
        if (values == null) return;

        String message = getString(R.string.order_text, values[0], values[2], values[3], values[1]);
        Intent intent = new Intent(Intent.ACTION_SENDTO);

        intent.setData(Uri.parse("mailto:" + "thebestsupplier@gmail.com")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_subject));
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void deleteProduct(View view) {
        if (!mIsNewProduct) mMyAsyncQueryHandler.startDelete(1, mTitleET.getText().toString(), mUri, null, null);
        finish();
    }

    public void saveProduct(View view) {
        String[] values = getValues();
        if (values == null) return;

        int price = Integer.parseInt(values[1]);
        int quantity = Integer.parseInt(values[3]);

        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_TITLE, values[0]);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_PRICE, price);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_MATERIAL, values[2]);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_QUANTITY, quantity);

        if (mIsNewProduct) {
            mMyAsyncQueryHandler.startInsert(0, values[0], InventoryContract.ProductEntry.PRODUCTS_URI, contentValues);
        } else {
            mMyAsyncQueryHandler.startUpdate(1, null, mUri, contentValues, null, null);
        }

        finish();
    }

    public String[] getValues() {
        String title = mTitleET.getText().toString().trim();
        String priceString = mPriceET.getText().toString().trim();
        String material = mMaterialET.getText().toString().trim();
        String quantityString = mQuantityET.getText().toString().trim();

        if (title.isEmpty() || priceString.isEmpty() || material.isEmpty() || quantityString.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_all_fields_must_be_filled), Toast.LENGTH_SHORT).show();
            return null;
        }

        title = title.toLowerCase();
        material = material.toLowerCase();

        return new String[]{
                title,
                priceString,
                material,
                quantityString
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToNext()) {
            String id = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry._ID));
            String title = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_TITLE));
            String price = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_PRICE));
            String material = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_MATERIAL));
            String quantity = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_QUANTITY));

            mIdLabelTV.setVisibility(View.VISIBLE);
            mIdValueTV.setText(id);
            mIdValueTV.setVisibility(View.VISIBLE);

            mTitleET.setText(title);
            mPriceET.setText(price);
            mMaterialET.setText(material);
            mQuantityET.setText(quantity);

            mQuantityInt = Integer.parseInt(quantity);
        }

        data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class MyTextWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String quantityString = editable.toString();
            if (quantityString.isEmpty()) {
                mQuantityInt = 1;
                return;
            }

            int quantity = Integer.parseInt(quantityString);
            if (quantity >= 1) {
                mQuantityInt = quantity;
            } else {
                mQuantityInt = 1;
                mQuantityET.setText(String.valueOf(mQuantityInt));
                errorQuantityBiggerThan1.show();
            }
        }
    }

}
