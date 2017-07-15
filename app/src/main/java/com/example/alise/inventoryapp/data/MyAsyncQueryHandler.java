package com.example.alise.inventoryapp.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.example.alise.inventoryapp.R;

import java.lang.reflect.Array;

/**
 * Created by Aleš Pros on 14.07.2017.
 */

public class MyAsyncQueryHandler extends AsyncQueryHandler {
    private Context mContext;

    public MyAsyncQueryHandler(ContentResolver cr, Context context) {
        super(cr);
        mContext = context;
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        //super.onInsertComplete(token, cookie, uri);
        Toast.makeText(mContext, mContext.getString(R.string.row_added, cookie), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        //super.onUpdateComplete(token, cookie, result);
        if (result > 0) {
            if (token == 0) {
                Toast.makeText(mContext, mContext.getString(R.string.row_updated_sale, cookie), Toast.LENGTH_SHORT).show();
            } else if (token == 1) {
                Toast.makeText(mContext, mContext.getString(R.string.row_updated_edit), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        //super.onDeleteComplete(token, cookie, result);
        if (result > 0) {
            if (token == 0) {
                Toast.makeText(mContext, mContext.getString(R.string.row_deleted, cookie), Toast.LENGTH_SHORT).show();
            } else if (token == 1) {
                Toast.makeText(mContext, mContext.getString(R.string.product_deleted, cookie), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
