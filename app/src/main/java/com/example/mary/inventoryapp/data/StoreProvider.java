package com.example.mary.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.mary.inventoryapp.data.StoreContract.TableEntry;

public class StoreProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = StoreProvider.class.getSimpleName();

    /**
     * Database helper object
     */
    private StoreDbHelper mStoreDbHelper;

    /** URI matcher code for the content URI for the products table */
    private static final int PRODUCTS = 100; // Full Table

    /** URI matcher code for the content URI for a single product in the products table */
    private static final int PRODUCT_ID = 101; // Single Item

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer that is run the first time anything is called from this class.
    static {
        /**
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize. All paths added to the UriMatcher have a corresponding code to retur
         * when a match is found.
         * The content URI of the form "content://com.example.android.products/products" will map to the
         * integer code {@link #PRODUCTS}. This URI is used to provide access to MULTIPLE rows of the products table.
         */
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_TABLE, PRODUCTS);

        /**
         * The content URI of the form "content://com.example.android.products/products/#" will map to the
         * integer code {@link #PRODUCT_ID}. This URI is used to provide access to ONE single row of the products table.
         * In this case, the "#" wildcard is used where "#" can be substituted for an integer.
         * For example, "content://com.example.android.products/products/3" matches, but
         * "content://com.example.android.products/products" (without a number at the end) doesn't match.
         */
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_TABLE + "/#", PRODUCT_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mStoreDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mStoreDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code.
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(TableEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                /**
                 * For the PRODUCT_ID code, extract out the ID from the URI.
                 * For an example URI such as "content://com.example.android.products/products/3",
                 * the selection will be "_id=?" and the selection argument will be a
                 * String array containing the actual ID of 3 in this case.
                 * For every "?" in the selection, we need to have an element in the selection
                 * arguments that will fill in the "?". Since we have 1 question mark in the
                 * selection, we have 1 String in the selection arguments' String array.
                 */
                selection = TableEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                /**
                 * This will perform a query on the products table where the _id equals 3 to return a
                 * Cursor containing that row of the table.
                 */
                cursor = database.query(TableEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        /**
         * Set notification URI on the Cursor,
         * so we know what content URI the Cursor was created for.
         * If the data at this URI changes, then we know we need to update the Cursor.
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor.
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        // Figure out if the URI matcher can match the URI to a specific code.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Validate the data before sending it to the database.
                ContentValues validatedValues = checkData(contentValues);

                // Gets the database in write mode.
                SQLiteDatabase database = mStoreDbHelper.getWritableDatabase();

                // Insert a new row for our product in the database, returning the ID of that new row.
                long id = database.insert(TableEntry.TABLE_NAME, null, validatedValues);

                // If the ID is -1, then the insertion failed. Log an error and return null.
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                    return null;
                }

                // Notify all listeners that the data has changed for the product content URI.
                getContext().getContentResolver().notifyChange(uri, null);

                // Return the new URI with the ID (of the newly inserted row) appended at the end.
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                /**
                 * For the PRODUCT_ID code, extract out the ID from the URI,
                 * so we know which row to update. Selection will be "_id=?" and selection
                 * arguments will be a String array containing the actual ID.
                 */
                selection = TableEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database.
        SQLiteDatabase database = mStoreDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted.
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args.
                rowsDeleted = database.delete(TableEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI.
                selection = TableEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(TableEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at given URI has changed.
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return TableEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return TableEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Method used to validate the data before entering it to the database.
     *
     * @param values The ContentValues to put into the database.
     * @return Returns the validated ContentValues.
     */
    private ContentValues checkData(ContentValues values) {
        // If there's no values, then we don't need to proceed with validation.
        if ((values != null) && (values.size() > 0)) {

            // Check that the product name is not be null or empty.
            if (values.containsKey(TableEntry.COLUMN_PRODUCT_NAME)) {
                String productName = values.getAsString(TableEntry.COLUMN_PRODUCT_NAME);
                if (TextUtils.isEmpty(productName)) {
                    throw new IllegalArgumentException("The product requires a name.");
                }
            }

            // Check that the product price doesn't have a negative value or beyond the Float max value.
            if (values.containsKey(TableEntry.COLUMN_PRICE)) {
                Float productPrice = values.getAsFloat(TableEntry.COLUMN_PRICE);
                if ((productPrice != null) && (productPrice < 0 || productPrice > Float.MAX_VALUE)) {
                    throw new IllegalArgumentException("The product requires a valid price.");
                }
            }

            // Check that the product quantity doesn't have a negative value or beyond the Integer max value.
            if (values.containsKey(TableEntry.COLUMN_QUANTITY)) {
                Integer productQuantity = values.getAsInteger(TableEntry.COLUMN_QUANTITY);
                if ((productQuantity != null) && (productQuantity < 0 || productQuantity > Integer.MAX_VALUE)) {
                    throw new IllegalArgumentException("The product requires a valid quantity.");
                }
            }

            // Check that the supplier phone is not be null or empty.
            if (values.containsKey(TableEntry.COLUMN_SUPPLIER_PHONE)) {
                String productSupplierPhone = values.getAsString(TableEntry.COLUMN_SUPPLIER_PHONE);
                if (TextUtils.isEmpty(productSupplierPhone)) {
                    throw new IllegalArgumentException("The product requires a phone number for the supplier.");
                }
            }
        }

        // If the values are OK, we return them for further processing.
        return values;
    }

    /**
     * Method to update the products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     *
     * @return Returns the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Validate the data before sending it to the database.
        ContentValues checkedValues = checkData(values);

        // Get writable database to update the data.
        SQLiteDatabase database = mStoreDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(TableEntry.TABLE_NAME, checkedValues, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed.
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated.
        return rowsUpdated;
    }
}
