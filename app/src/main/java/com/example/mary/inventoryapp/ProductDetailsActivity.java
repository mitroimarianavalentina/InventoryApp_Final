package com.example.mary.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mary.inventoryapp.data.StoreContract.TableEntry;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Class that allows the user to visualize the details of a product.
 */
public class ProductDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = ProductDetailsActivity.class.getSimpleName();

    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * variable for the current product Uri
     */
    private Uri currentProductUri;

    /**
     * variable for the current quantity
     */
    private int currentQuantity;

    /**
     * variable to track if the quantity changes
     */
    private boolean quantityChanged = false;

    /**
     * declare as global all the textViews
     */
    private TextView productNameTextView;
    private TextView productPriceTextView;
    private TextView productQuantityTextView;
    private TextView supplierNameTextView;
    private TextView supplierPhoneTextView;

    /**
     * declare as global all the Image buttons
     */
    private ImageButton incrementButton;
    private ImageButton decrementButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details_activity);
        productNameTextView = findViewById(R.id.product_name);
        productPriceTextView = findViewById(R.id.price_amount);
        productQuantityTextView = findViewById(R.id.quantity_amount);
        supplierNameTextView = findViewById(R.id.supplier_name);
        supplierPhoneTextView = findViewById(R.id.supplier_phone);
        incrementButton = findViewById(R.id.button_quantity_increase);
        decrementButton = findViewById(R.id.button_quantity_decrease);


        /**
         * declare the floating Action Button
         */
        FloatingActionButton floatingActionButton = findViewById(R.id.floating_action_button_id);

        // Get the intent that was used to launch this activity and extract the product Uri.
        Intent intent = getIntent();
        currentProductUri = intent.getData();

        // Initialize the loader to read the product data from the database and display the current values.
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, ProductDetailsActivity.this);

        // Setup the floating action button to open the Editor Activity.
        floatingActionButton.setOnClickListener(view -> launchEditorActivity());

        // Set Click Listeners for the Increment and Decrement buttons.
        incrementButton.setOnClickListener(view -> incrementQuantity());
        decrementButton.setOnClickListener(view -> decrementQuantity());
    }

    /**
     * Override onPause to save the quantity value to database when the user leaves the activity.
     */
    @Override
    protected void onPause() {
        super.onPause();
        updateQuantity();
    }

    /**
     * Override the onCreateOptionsMenu method in order to create the options menu for the Details Activity.
     * @param menu The menu to create.
     * @return Returns true if the menu was created successfully.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    /**
     * Override the onOptionsItemSelected method in order to detect which menu list_item was clicked and to execute the selected option.
     * @param item The list_item that was clicked in the menu.
     * @return Executes the action and returns the clicked list_item from the menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                launchEditorActivity();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Overrides the onCreateLoader method in order to define the projection that contains all columns from the products table.
     * @return Returns a Loader that will execute the query on a background thread.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                TableEntry._ID,
                TableEntry.COLUMN_PRODUCT_NAME,
                TableEntry.COLUMN_PRICE,
                TableEntry.COLUMN_QUANTITY,
                TableEntry.COLUMN_SUPPLIER_NAME,
                TableEntry.COLUMN_SUPPLIER_PHONE
        };

        return new CursorLoader(
                ProductDetailsActivity.this,
                currentProductUri,
                projection,
                null,
                null,
                null
        );
    }

    /**
     * Overrides the onLoadFinished method in order to read product data from
     * the database, and update the views on the screen with the existing values.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Check if the Cursor is null or it contains less than 1 row.
        if (cursor == null || cursor.getCount() < 1) return;

        // Proceed with moving to the first row of the cursor and read the data from it.
        // This should be the only row in the cursor.
        if (cursor.moveToFirst()) {
            // Find the columns that we're interested in.
            int productNameColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_SUPPLIER_PHONE);

            // Extract the values from the Cursor
            String productName = cursor.getString(productNameColumnIndex);
            float productPrice = cursor.getFloat(productPriceColumnIndex);
            int productQuantity = cursor.getInt(productQuantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the global values for Quantity.
            currentQuantity = productQuantity;

            // Update the views on the screen with the values from the database.
            productNameTextView.setText(productName);
            productPriceTextView.setText(formatPrice(productPrice));
            productQuantityTextView.setText(formatQuantity(productQuantity));
            supplierNameTextView.setText(supplierName);
            supplierPhoneTextView.setText(supplierPhone);
        }
    }

    /**
     * Overrides the onLoaderReset method in order to handle the case in which the Loader
     * is invalidated. If that happens, we need to clear out the data from the views
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameTextView.setText("");
        productPriceTextView.setText("");
        productQuantityTextView.setText("");
        supplierNameTextView.setText("");
        supplierPhoneTextView.setText("");
    }


//    private void findAllViews() {
//        productNameTextView = findViewById(R.id.product_name);
//        productPriceTextView = findViewById(R.id.price_amount);
//        productQuantityTextView = findViewById(R.id.quantity_amount);
//        supplierNameTextView = findViewById(R.id.supplier_name);
//        supplierPhoneTextView = findViewById(R.id.supplier_phone);
//        incrementButton = findViewById(R.id.button_quantity_increase);
//        decrementButton = findViewById(R.id.button_quantity_decrease);
//        floatingActionButton = findViewById(R.id.floating_action_button_id);
//    }

    /** Method to launch the Editor Activity with the current product Uri. */
    private void launchEditorActivity() {
        if (currentProductUri != null) {
            Intent editorIntent = new Intent(ProductDetailsActivity.this, EditorActivity.class);
            editorIntent.setData(currentProductUri);
            startActivity(editorIntent);
        }
        finish();
    }

//    /** Method to launch Phone Dialer app, to call the specified Supplier Phone number. */
//    private void launchCall(String phone) {
//        if (!phone.isEmpty()) {
//            Uri phoneNumber = Uri.parse("tel:" + phone);
//            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
//            dialIntent.setData(phoneNumber);
//            if (dialIntent.resolveActivity(getPackageManager()) != null) {
//                startActivity(dialIntent);
//            }
//        }
//    }

    /** Method to increment the current product quantity by one. */
    private void incrementQuantity() {
        if (currentQuantity != Integer.MAX_VALUE) {
            currentQuantity++;
            productQuantityTextView.setText(formatQuantity(currentQuantity));
            quantityChanged = true;
        }
    }

    /** Method to decrement the current product quantity by one. */
    private void decrementQuantity() {
        if (currentQuantity > 0) {
            currentQuantity--;
            productQuantityTextView.setText(formatQuantity(currentQuantity));
            quantityChanged = true;
        }
    }

    /** Method to update the product quantity and save the new value into the database. */
    private void updateQuantity() {
        if (quantityChanged) {
            ContentValues values = new ContentValues();
            values.put(TableEntry.COLUMN_QUANTITY, currentQuantity);
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0) showToastAndLog(ProductDetailsActivity.this, true, LOG_TAG, getString(R.string.update_msg_error));
            else showToastAndLog(ProductDetailsActivity.this, false, LOG_TAG, getString(R.string.update_msg_success));
        }
    }

    /** Method to delete the product from the database. */
    private void deleteProduct() {
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) showToastAndLog(ProductDetailsActivity.this, true, LOG_TAG, getString(R.string.product_deleted_error));
            else showToastAndLog(ProductDetailsActivity.this, false, LOG_TAG, getString(R.string.product_deleted_success));
        }
        finish();
    }

    /** Method to show a confirmation dialog for the delete action. */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsActivity.this);
        builder.setMessage(R.string.question_about_deleting);
        builder.setPositiveButton(R.string.delete, (dialogInterface, id) -> deleteProduct());
        builder.setNegativeButton(R.string.cancel, (dialogInterface, id) -> {
            if (dialogInterface != null) dialogInterface.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
