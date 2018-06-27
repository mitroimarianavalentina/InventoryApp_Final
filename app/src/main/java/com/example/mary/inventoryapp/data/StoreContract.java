package com.example.mary.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public class StoreContract {

    private StoreContract() {
    }

    /**
     * we need to create the structure of the URI
     * content + content authority + type of data
     */

    /**
     * Declare the content authority, this is used to know witch content provider to be asked for the info we are interested in
     */
    public static  final String CONTENT_AUTHORITY = "com.example.mary.inventoryapp";

    /**
     * create the Uri, by parsing the URI, this means that we add the content to the content authority
     * and the String resulted from the concatenation we transform it into a URi with the help of the "parse" method
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * declare the path for the table
     */
    public static final String PATH_TABLE = "products";

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents a single product.
     */
    public static abstract class TableEntry implements BaseColumns {

        /**
         * creating the final structure of teh URI by appending the path : the name of the table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TABLE);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TABLE;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TABLE;

        /** The name of the table */
        public static final String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME = "ProductName";

        /**
         * Price of the product.
         *
         * Type: FLOAT
         */
        public static final String COLUMN_PRICE = "price";

        /**
         * Quantity of the product.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_QUANTITY = "quantity";

        /**
         * Supplier Name
         *
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_NAME = "SupplierName";

        /**
         * Supplier Phone
         *
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_PHONE = "SupplierPhoneNumber";

    }

}
