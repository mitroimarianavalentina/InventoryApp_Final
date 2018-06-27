package com.example.mary.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mary.inventoryapp.data.StoreContract.TableEntry;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Adapter class for the ListView that uses a Cursor of product data as its data source.
 * This adapter knows how to create list items for each row of product data in the Cursor.
 */
public class ProductCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    /**
     * Constructor for a new ProductCursorAdapter.
     *
     * @param context The context from which this class is instantiated.
     * @param cursor  The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Makes a new blank list list_item view. No data is set (or bound) to the views yet.
     *
     * @param context The app context.
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct position.
     * @param parent  The parent to which the new view is attached to.
     * @return The newly created list list_item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate the list_item layout.
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        // Create a new ViewHolder to hold all child views.
        ViewHolder viewHolder = new ViewHolder(view);
        // Set the ViewHolder as a tag to the newly inflated layout.
        view.setTag(viewHolder);
        // Return the inflated layout.
        return view;
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list list_item layout. For example, the name for the current product can be set on the name TextView
     * in the list list_item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct row.
            */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get the ViewHolder from the tag set in the newView method.
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Find the columns of product attributes that we're interested in.
        int idColumnIndex = cursor.getColumnIndex(TableEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_QUANTITY);
        int supplierNameColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_SUPPLIER_NAME);

        // Read the attributes from the Cursor for the current product.
        long productID = cursor.getLong(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        float productPrice = cursor.getFloat(priceColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        String productSupplierName = cursor.getString(supplierNameColumnIndex);

        // Form the content URI that represents this specific product.
        Uri currentProductUri = ContentUris.withAppendedId(TableEntry.CONTENT_URI, productID);

        // If the product supplier is null or empty, set default values to display.
        // This field can be empty.
        if (TextUtils.isEmpty(productSupplierName))
            productSupplierName = context.getString(R.string.supplier_unknown);

        // Set Intent to open the current Product in the ProductDetailsActivity.
        viewHolder.itemCard.setOnClickListener(item -> {
            Intent detailsIntent = new Intent(context, ProductDetailsActivity.class);
            detailsIntent.setData(currentProductUri);
            context.startActivity(detailsIntent);
        });

        // Update the TextViews with the attributes for the current product.
        viewHolder.productNameTextView.setText(productName);
        viewHolder.productPriceTextView.setText(formatPrice(productPrice));
        viewHolder.productQuantityTextView.setText(formatQuantity(productQuantity));
        viewHolder.supplierNameTextView.setText(productSupplierName);

        // Set Click Listener for the Sale button.
        viewHolder.saleButton.setOnClickListener(button -> {
            int changedQuantity = productQuantity;
            if (changedQuantity > 0) {
                // Subtract one and display the quantity again.
                changedQuantity--;
                viewHolder.productQuantityTextView.setText(formatQuantity(changedQuantity));

                // Update the database with the new quantity value.
                ContentValues values = new ContentValues();
                values.put(TableEntry.COLUMN_QUANTITY, changedQuantity);
                int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
                if (rowsAffected == 0)
                    showToastAndLog(context, true, LOG_TAG, context.getString(R.string.stock_msg_error));
                else
                    showToastAndLog(context, false, LOG_TAG, context.getString(R.string.stock_msg_success));
            }
        });
    }

    /**
     * Class that catches all the child views necessary to build each list_item.
     */
    private static class ViewHolder {
        private RelativeLayout itemCard;
        private TextView productNameTextView;
        private TextView productPriceTextView;
        private TextView productQuantityTextView;
        private TextView supplierNameTextView;
        private Button saleButton;

        /**
         * Public constructor so we can find all the views necessary.
         *
         * @param view The parent view from which to find the children views.
         */
        private ViewHolder(View view) {
            itemCard = view.findViewById(R.id.list_item);
            productNameTextView = view.findViewById(R.id.item_product_name);
            productPriceTextView = view.findViewById(R.id.item_product_price);
            productQuantityTextView = view.findViewById(R.id.item_product_quantity);
            supplierNameTextView = view.findViewById(R.id.item_supplier_name);
            saleButton = view.findViewById(R.id.item_sale_btn);
        }
    }

    /**
     * Method that displays short Toast and logs messages to the console.
     * @param context The context from which this method is called.
     * @param error Flag to show if it's an error or not.
     * @param tag The tag title for the Logs.
     * @param message The message to show in the Toast and Log to the console.
     */
    public static void showToastAndLog(Context context, boolean error, String tag, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        if (error) Log.e(tag, message);
        else Log.i(tag, message);
    }

    /**
     * Method to format the price
     * It allows only two decimal digits
     * @param price The raw price received from the database.
     * @return Returns the formatted price.
     */
    public static String formatPrice(float price) {
        Locale locale = Locale.getDefault();
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        numberFormat.setCurrency(Currency.getInstance(locale));
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(price);
    }

    /**
     * Method to format the quantity, according to the user's locale.
     * Please don't use this method to insert the quantity into the database!
     * @param rawQuantity The raw quantity received from the database.
     * @return Returns the formatted quantity.
     */
    public static String formatQuantity(float rawQuantity) {
        Locale locale = Locale.getDefault();
        NumberFormat numberFormat = NumberFormat.getIntegerInstance(locale);
        return numberFormat.format(rawQuantity);
    }
}
