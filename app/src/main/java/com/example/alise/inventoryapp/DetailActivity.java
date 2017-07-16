package com.example.alise.inventoryapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alise.inventoryapp.data.InventoryContract;
import com.example.alise.inventoryapp.data.MyAsyncQueryHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    static final int REQUEST_TAKE_PHOTO = 1;

    private ImageView mProductImageIV;
    private TextView mIdLabelTV, mIdValueTV;
    private EditText mTitleET, mPriceET, mMaterialET, mQuantityET;

    private int mQuantityInt;
    private Toast errorQuantityBiggerThan1;

    private MyAsyncQueryHandler mMyAsyncQueryHandler;
    private Uri mProductItemUri;

    //mInitialImageUri is the uri for the image when we open DetailActivity by clicking on the item on the recycler view
    // from MainActivity)

    //mImageUri is the uri for the last image that was just properly taken with the camera

    //mTempImageUri is the uri for the image that we just started writing to by trying to take a photo
    //(after the photo is correctly taken we copy its value to the mImageUri
    private Uri mInitialImageUri, mImageUri, mTempImageUri;

    //this helps us to determine what images we should delete and what images are being still used by the app
    private boolean mIsNewProduct;
    private boolean mDbWasChanged = false;
    private boolean mShouldDeleteImage = false;
    private boolean mShouldDeleteInitialImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        errorQuantityBiggerThan1 = Toast.makeText(this, getString(R.string.error_quantity_bigger_than_0), Toast.LENGTH_LONG);

        Intent intentThatStartedThisActivity = getIntent();
        int id = intentThatStartedThisActivity.getIntExtra("id", -1);

        if (id != -1) {
            mIsNewProduct = false;
            mProductItemUri = InventoryContract.ProductEntry.PRODUCTS_URI.buildUpon().appendPath(String.valueOf(id)).build();
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            mIsNewProduct = true;
            findViewById(R.id.bt_detail_delete).setVisibility(View.GONE);
        }

        mProductImageIV = (ImageView) findViewById(R.id.iv_detail_image);
        mIdLabelTV = (TextView) findViewById(R.id.tv_detail_id_label);
        mIdValueTV = (TextView) findViewById(R.id.tv_detail_id_value);
        mTitleET = (EditText) findViewById(R.id.et_detail_title);
        mPriceET = (EditText) findViewById(R.id.et_detail_price);
        mMaterialET = (EditText) findViewById(R.id.et_detail_material);
        mQuantityET = (EditText) findViewById(R.id.et_detail_quantity);

        mQuantityET.addTextChangedListener(new MyTextWatcher());

        mProductImageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shouldAskPermissions()) {
                    askPermissions();
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });

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
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getString(R.string.delete_confirmation_dialog_message));

        alert.setPositiveButton(getString(R.string.delete_confirmation_dialog_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mMyAsyncQueryHandler.startDelete(1, mTitleET.getText().toString(), mProductItemUri, null, null);
                mDbWasChanged = true;

                //we don't need to delete the initialImage, because provider will do that, so delete only image
                if (mImageUri != null) mShouldDeleteImage = true;
                finish();
            }
        });

        alert.setNegativeButton(getString(R.string.delete_confirmation_dialog_no), null);
        alert.show();
    }

    //here we check what images are still going to be used by the app and we delete the other images
    @Override
    protected void onDestroy() {
        //if the database was changed
        // -> the methods changed the values for mShouldDeleteImage and mShouldDeleteInitialImage
        // -> so act based on those values
        if (mDbWasChanged) {
            if (mShouldDeleteImage) new File(mImageUri.getPath()).delete();
            if (mShouldDeleteInitialImage) new File(mInitialImageUri.getPath()).delete();
        } else {
            //if it is new product and we are not saving it -> delete everything we can
            if (mIsNewProduct) {
                if (mImageUri != null) new File(mImageUri.getPath()).delete();
                if (mInitialImageUri != null) new File(mInitialImageUri.getPath()).delete();
            }
            //if it isn't new product and we are not saving it or removing it -> delete everything except the
            //picture for this product that we are already using in the database
            if (!mIsNewProduct) {
                if (mImageUri != null) new File(mImageUri.getPath()).delete();
            }
        }
        super.onDestroy();
    }

    public void saveProduct(View view) {
        String[] values = getValues();
        if (values == null) return;
        if (mImageUri == null && mInitialImageUri == null) {
            Toast.makeText(this, getString(R.string.error_no_image), Toast.LENGTH_LONG).show();
            return;
        }

        String imageUri = null;
        if (mImageUri == null) {
            imageUri = mInitialImageUri.toString();
        }

        if (mImageUri != null) {
            imageUri = mImageUri.toString();
            if (mInitialImageUri != null) mShouldDeleteInitialImage = true;
        }

        int price = Integer.parseInt(values[1]);
        int quantity = Integer.parseInt(values[3]);

        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_TITLE, values[0]);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_PRICE, price);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_MATERIAL, values[2]);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_QUANTITY, quantity);
        contentValues.put(InventoryContract.ProductEntry.COLUMN_NAME_IMAGE, imageUri);

        if (mIsNewProduct) {
            mMyAsyncQueryHandler.startInsert(0, values[0], InventoryContract.ProductEntry.PRODUCTS_URI, contentValues);
        } else {
            mMyAsyncQueryHandler.startUpdate(1, null, mProductItemUri, contentValues, null, null);
        }

        mDbWasChanged = true;
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
        return new CursorLoader(this, mProductItemUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToNext()) {
            String id = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry._ID));
            String title = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_TITLE));
            String price = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_PRICE));
            String material = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_MATERIAL));
            String quantity = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_QUANTITY));
            String imageUri = data.getString(data.getColumnIndexOrThrow(InventoryContract.ProductEntry.COLUMN_NAME_IMAGE));

            mIdLabelTV.setVisibility(View.VISIBLE);
            mIdValueTV.setText(id);
            mIdValueTV.setVisibility(View.VISIBLE);

            mTitleET.setText(title);
            mPriceET.setText(price);
            mMaterialET.setText(material);
            mQuantityET.setText(quantity);

            mQuantityInt = Integer.parseInt(quantity);

            mInitialImageUri = Uri.parse(imageUri);
            mProductImageIV.setImageURI(mInitialImageUri);
        }

        data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    //this method is called after the picture from the camera is taken
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            //if we are replacing the image we can delete the old image
            if (mImageUri != null) {
                new File(mImageUri.getPath()).delete();
            }

            mImageUri = mTempImageUri;
            mProductImageIV.setImageURI(mImageUri);
        } else {
            //if something went wrong delete the empty file
            new File(mTempImageUri.getPath()).delete();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String path = "/InventoryApp/Pictures";
        File dir = new File(baseDir + path);
        dir.mkdirs();

        File image = File.createTempFile(
                imageFileName,   //prefix
                ".jpg",          //suffix
                dir              //directory
        );

        // Save a file: uri
        mTempImageUri = Uri.fromFile(image);
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("inventoryapp", "Error with creating the file: " + ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.alise.inventoryapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        ActivityCompat.requestPermissions(DetailActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    dispatchTakePictureIntent();

                } else {
                    // Permission denied.
                    Toast.makeText(DetailActivity.this, getString(R.string.permission_external_storage_write), Toast.LENGTH_LONG).show();
                }

                break;
            }
        }
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

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
