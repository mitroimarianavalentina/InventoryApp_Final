package com.example.mary.inventoryapp;


import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mary.inventoryapp.data.StoreContract.TableEntry;


/**
 * Displays a list of products that were entered and stored in the app.
 * This is the main entry point for the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int PRODUCT_LOADER = 0;

    /**
     * Cursor adapter used to populate the ListView with product data.
     */
    private ProductCursorAdapter mCursorAdapter;

    /**
     * Overrides the onCreate method to assemble and display the Catalog Activity.
     *
     * @param savedInstanceState The saved instance state Bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_activity);

        // Attach Intent to open the Editor Activity.
        FloatingActionButton floatingActionButton = findViewById(R.id.catalog_floating_action_button);
        floatingActionButton.setOnClickListener(view -> {
            Intent editorIntent = new Intent(CatalogActivity.this, EditorActivity.class);
            startActivity(editorIntent);
        });

        // Find the ListView which will be populated with product data.
        ListView catalogListView = findViewById(R.id.catalog_list);

        // Find and set empty state view on the ListView.
        View emptyStateView = findViewById(R.id.empty_view);
        catalogListView.setEmptyView(emptyStateView);

        // Setup an Adapter to create a list of items with each row of product data in the Cursor.
        // There is no product data yet (until the Loader finishes), so we pass in null for the Cursor.
        mCursorAdapter = new ProductCursorAdapter(CatalogActivity.this, null);
        catalogListView.setAdapter(mCursorAdapter);

        // Initialize the Loader.
        getLoaderManager().initLoader(PRODUCT_LOADER, null, CatalogActivity.this);
    }

    /**
     * Override the onCreateOptionsMenu method in order to
     * create the options menu for Catalog Activity.
     *
     * @param menu The menu to create.
     * @return Returns true if the menu was created successfully.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.catalog_menu, menu);
        return true;
    }

    /**
     * Override the onOptionsItemSelected method in order to
     * detect which menu list_item was clicked and to execute the selected action.
     *
     * @param item The list_item that was clicked in the menu.
     * @return Executes the action and returns the clicked list_item from the menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_add_dummy_data:
//                insertDummyData();
//                return true;
            case R.id.action_delete_all_data:
                deleteAllData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Overrides the onCreateLoader method in order to create the projection
     * that specifies the columns from the table that we care about.
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
                TableEntry.COLUMN_SUPPLIER_NAME
        };

        return new CursorLoader(
                CatalogActivity.this,
                TableEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    /**
     * Overrides the onLoadFinished method in order to update the
     * ProductCursorAdapter with this new Cursor containing updated product data.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    /**
     * Overrides the onLoaderReset method in order to
     * call this callback when the data needs to be deleted.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

//    /**
//     * Insert dummy (demo) product data into the database.
//     */
//    private void insertDummyData() {
//            // Extract dummy data and add it to ContentValues object.
//            ContentValues values = new ContentValues();
//            values.put(TableEntry.COLUMN_PRODUCT_NAME, "Laptop");
//            values.put(TableEntry.COLUMN_PRICE, 699.99);
//            values.put(TableEntry.COLUMN_QUANTITY, 10);
//            values.put(TableEntry.COLUMN_SUPPLIER_NAME, "HP");
//            values.put(TableEntry.COLUMN_SUPPLIER_PHONE, "+40766584689");
//
//        // Insert a new row for Toto into the provider using the ContentResolver.
//        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
//        // into the pets database table.
//        // Receive the new content URI that will allow us to access Toto's data in the future.
//        Uri newUri = getContentResolver().insert(TableEntry.CONTENT_URI, values);
//    }

    /**
     * Delete all products data from the database.
     */
    private void deleteAllData() {
        // Delete all products from the database.
        int rowsDeleted = getContentResolver().delete(TableEntry.CONTENT_URI, null, null);

        // Show Toast and Log the message.
        String deletionMsg = getString(R.string.all_products_deleted_success);
        if (rowsDeleted > 0) {
            Toast.makeText(this, deletionMsg, Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, deletionMsg);
        }
    }
}
