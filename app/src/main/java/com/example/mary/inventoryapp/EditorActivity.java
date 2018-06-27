package com.example.mary.inventoryapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mary.inventoryapp.data.StoreContract.TableEntry;
/**
 * Activity class that allows the user to create a new product, or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Current Product Uri
     */
    private Uri currentProductUri;

    /**
     * Variable to keep track if we can save the product or not
     */
    private boolean canSave = true;

    /**
     * Variable to keep track if the product has been edited or not
     */
    private boolean hasBeenEdited = false;

    /**
     * declare as global all the EditText
     */
    private EditText productNameEditText;
    private EditText productPriceEditText;
    private EditText productQuantityEditText;
    private EditText supplierNameEditText;
    private EditText supplierPhoneEditText;
    private FloatingActionButton floatingActionButton;

    // Touch listener to detect when the user modified the view.
    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener mTouchListener = (view, motionEvent) -> {
        hasBeenEdited = true;
        return false;
    };

    /**
     * Overrides the onCreate method to assemble and display the Editor Activity.
     *
     * @param savedInstanceState The saved instance state Bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity);

        // Find the views.
        findAllViews();

        // Get the intent that was used to launch this activity and extract the product Uri.
        Intent intent = getIntent();
        currentProductUri = intent.getData();

        // If the intent doesn't contain a product URI, then we create a new product.
        if (currentProductUri == null) {
            setTitle(R.string.add_product);

            // Invalidate the options menu, hide "Delete" option menu, if it doesn't exist a product we can not delete it.
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_product);
            // If it's an existing product, initialize the Loader to read the product data from database.
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, EditorActivity.this);
        }

        // Save or update the product and exit the Editor Activity.
        floatingActionButton.setOnClickListener(view -> {
            if (isExistingProduct()) updateProduct();
            else saveProduct();
        });

        // Set touch listeners for all EditText fields.
        setTouchListeners();
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu list_item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Override the onCreateOptionsMenu method in order to
     * create the options menu for the Editor Activity.
     *
     * @param menu The menu to create.
     * @return Returns true if the menu was created successfully.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nemu_editor, menu);
        return true;
    }

    /**
     * Override the onOptionsItemSelected method in order to
     * detect which menu list_item was clicked and to execute the selected option.
     *
     * @param item The list_item that was clicked in the menu.
     * @return Executes the action and returns the clicked list_item from the menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (isExistingProduct()) updateProduct();
                else saveProduct();
                return true;
            case R.id.action_delete:
                if (isExistingProduct()) showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!hasBeenEdited) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        (dialogInterface, id) -> NavUtils.navigateUpFromSameTask(EditorActivity.this);
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Overrides the onCreateLoader method in order to define
     * the projection that contains all columns from the products table.
     *
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
                EditorActivity.this,
                currentProductUri,
                projection,
                null,
                null,
                null
        );
    }

    /**
     * Overrides the onLoadFinished method in order to read product data from
     * the database, and update the fields on the screen with the existing values.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the Cursor is null or there it contains less than 1 row.
        if (cursor == null || cursor.getCount() < 1) return;

        // Proceed with moving to the first row of the cursor and reading data from it.
        // This should be the only row in the cursor.
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in.
            int nameProductColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_PRODUCT_NAME);
            int priceProductColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_PRICE);
            int quantityProductColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(TableEntry.COLUMN_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index.
            String productName = cursor.getString(nameProductColumnIndex);
            float productPrice = cursor.getFloat(priceProductColumnIndex);
            int productQuantity = cursor.getInt(quantityProductColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database.
            productNameEditText.setText(productName);
            productPriceEditText.setText(Float.toString(productPrice));
            productQuantityEditText.setText(Integer.toString(productQuantity));
            supplierNameEditText.setText(supplierName);
            supplierPhoneEditText.setText(supplierPhone);
        }
    }

    /**
     * Overrides the onLoaderReset method in order to handle the case in which the Loader
     * is invalidated. If that happens, we need to clear out the data from the fields.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameEditText.setText("");
        productPriceEditText.setText("");
        productQuantityEditText.setText("");
        supplierNameEditText.setText("");
        supplierPhoneEditText.setText("");
    }

    /**
     * This method is called when the back button is pressed.
     * If the product hasn't changed, we continue with handling back button press,
     * otherwise if there are unsaved changes, we setup a dialog to warn the user.
     */
    @Override
    public void onBackPressed() {
        if (!hasBeenEdited) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = (dialogInterface, id) -> finish();
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Method to find all necessary views that we need to populate with product data.
     */
    private void findAllViews() {
        productNameEditText = findViewById(R.id.product_name);
        productPriceEditText = findViewById(R.id.product_price);
        productQuantityEditText = findViewById(R.id.product_quantity);
        supplierNameEditText = findViewById(R.id.supplier_name);
        supplierPhoneEditText = findViewById(R.id.supplier_phone);
        floatingActionButton = findViewById(R.id.save_fab);
    }

    /**
     * Method to attach OnTouchListeners to all EditText inputs fields, so we can determine
     * if the user has touched or modified the field. This will let us know if there are
     * unsaved changes or not, if the user tries to leave the editor without saving.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListeners() {
        productNameEditText.setOnTouchListener(mTouchListener);
        productPriceEditText.setOnTouchListener(mTouchListener);
        productQuantityEditText.setOnTouchListener(mTouchListener);
        supplierNameEditText.setOnTouchListener(mTouchListener);
        supplierPhoneEditText.setOnTouchListener(mTouchListener);
    }

    /**
     * Method to save the data into the database as a new product.
     */
    private void saveProduct() {
        ContentValues values = getValidatedData();
        if (canSave) {
            Uri newProductUri = getContentResolver().insert(TableEntry.CONTENT_URI, values);
            if (newProductUri == null) {
                showToastAndLog(EditorActivity.this, true, LOG_TAG, getString(R.string.product_save_error));
            } else {
                String saveSuccessMsg = getString(R.string.product_save_success) + String.valueOf(ContentUris.parseId(newProductUri));
                showToastAndLog(EditorActivity.this, false, LOG_TAG, saveSuccessMsg);
            }
            finish();
        } else {
            showToastAndLog(EditorActivity.this, true, LOG_TAG, getString(R.string.error_saving_empty_fields));
        }
    }

    /**
     * Method to update the existing product details.
     */
    private void updateProduct() {
        ContentValues values = getValidatedData();
        if (canSave) {
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0)
                showToastAndLog(EditorActivity.this, true, LOG_TAG, getString(R.string.update_msg_error));
            else
                showToastAndLog(EditorActivity.this, false, LOG_TAG, getString(R.string.update_msg_success));
            finish();
        } else {
            showToastAndLog(EditorActivity.this, true, LOG_TAG, getString(R.string.error_saving_empty_fields));
        }
    }

    /**
     * Method to delete the product from the database.
     */
    private void deleteProduct() {
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0)
                showToastAndLog(EditorActivity.this, true, LOG_TAG, getString(R.string.product_deleted_error));
            else
                showToastAndLog(EditorActivity.this, false, LOG_TAG, getString(R.string.product_deleted_success));
        }
        finish();
    }

    /**
     * Method to extract the text entered by the user into the EditText fields.
     *
     * @return Returns a new Product object to pass to other methods.
     */
    private Product getEditTextData() {
        // Get the text from EditText fields.
        String productNameString = productNameEditText.getText().toString().trim();
        String productPriceString = productPriceEditText.getText().toString().trim();
        String productQuantityString = productQuantityEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString().trim();
        String supplierPhoneString = supplierPhoneEditText.getText().toString().trim();

        // Convert the text to appropriate types.
        final String EMPTY_STR = "0";
        if (productPriceString.isEmpty()) productPriceString = EMPTY_STR;
        if (productQuantityString.isEmpty()) productQuantityString = EMPTY_STR;
        float productPriceFloat = Float.parseFloat(productPriceString);
        int productQuantityInt = Integer.parseInt(productQuantityString);

        // Return a new Product with the text entered/modified by the user.
        return new Product(
                productNameString,
                productPriceFloat,
                productQuantityInt,
                supplierNameString,
                supplierPhoneString
        );
    }

    /**
     * Method to get and validate data from the EditText fields.
     *
     * @return Return the validated ContentValues that can be saved to the database.
     */
    private ContentValues getValidatedData() {
        // Get data from the EditText fields.
        Product product = getEditTextData();

        // Extract data from the Product object.
        String productName = product.getProductName();
        float productPrice = product.getProductPrice();
        int productQuantity = product.getProductQuantity();
        String supplierName = product.getSupplierName();
        String supplierPhone = product.getSupplierPhone();

        // Create the ContentValues object.
        // Column names are the keys, product attributes are the values.
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(productName)) {
            values.put(TableEntry.COLUMN_PRODUCT_NAME, productName);
        } else {
            canSave = false;
        }

        if ((productPrice != 0) && (productPrice < Float.MAX_VALUE)) {
            values.put(TableEntry.COLUMN_PRICE, productPrice);
        } else {
            canSave = false;
        }
        if ((productQuantity != 0) && (productQuantity < Integer.MAX_VALUE)) {
            values.put(TableEntry.COLUMN_QUANTITY, productQuantity);
        } else {
            canSave = false;
        }
        if (!TextUtils.isEmpty(supplierName)) {
            values.put(TableEntry.COLUMN_SUPPLIER_NAME, supplierName);
        } else {
            canSave = false;
        }
        if (!TextUtils.isEmpty(supplierPhone)) {
            values.put(TableEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
        } else {
            canSave = false;
        }

        // Return validated values.
        return values;
    }

    /**
     * Method to check if this is an existing product.
     *
     * @return Returns true, if we have a product Uri.
     */
    private boolean isExistingProduct() {
        return (currentProductUri != null);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost if they leave the editor.
     *
     * @param clickListener This is the click listener for what to do when the user confirms
     *                      they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener clickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.question_about_discarding_editing);
        builder.setPositiveButton(R.string.discard, clickListener);
        builder.setNegativeButton(R.string.keep_editing, (dialogInterface, id) -> {
            // User clicked "Keep Editing", so dismiss the dialog and continue editing product.
            if (dialogInterface != null) dialogInterface.dismiss();
        });

        // Create and show the AlertDialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Show a dialog that warns the user that he's about to delete the product.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
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
}
