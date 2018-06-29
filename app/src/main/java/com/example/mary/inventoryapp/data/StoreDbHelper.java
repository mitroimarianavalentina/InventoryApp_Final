package com.example.mary.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mary.inventoryapp.data.StoreContract.TableEntry;
/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class StoreDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 4;

    /**
     * Constructs a new instance of {@link StoreDbHelper}.
     *
     * @param context of the app
     */
    public StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PPRODUCTS_TABLE =
                "CREATE TABLE " + TableEntry.TABLE_NAME + " (" +
                    TableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TableEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                    TableEntry.COLUMN_PRICE + " REAL NOT NULL DEFAULT 0," +
                    TableEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                    TableEntry.COLUMN_SUPPLIER_NAME + " TEXT," +
                    TableEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL)";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PPRODUCTS_TABLE);
    }

    /**
     * Override for the onUpgrade method.
     * This method is used for the cases in which the database schema changes.
     * This method is also used to delete all the data from the current table.
     *
     * This is called when the database needs to be upgraded.
     *
     * @param db         The SQLite database to upgrade.
     * @param oldVersion The version of the old database.
     * @param newVersion The version of the new database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Here we drop the current table and create a new one.
        //db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }

    /**
     * Override for the onDowngrade method.
     * This method is used for the cases in which the database schema changes back to an older version.
     *
     * @param db         The SQLite database to downgrade.
     * @param oldVersion The version of the old database.
     * @param newVersion The version of the new database.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Here we just call the onUpgrade method above because we do the same thing.
        onUpgrade(db, oldVersion, newVersion);
    }

}
