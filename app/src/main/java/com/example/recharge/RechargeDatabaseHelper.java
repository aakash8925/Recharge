package com.example.recharge;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RechargeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recharge.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "recharges";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RechargeContract.RechargeEntry.TABLE_NAME + " (" +
                    RechargeContract.RechargeEntry._ID + " INTEGER PRIMARY KEY," +
                    RechargeContract.RechargeEntry.COLUMN_NUMBER + " TEXT," +
                    RechargeContract.RechargeEntry.COLUMN_OPERATOR + " TEXT," +
                    RechargeContract.RechargeEntry.COLUMN_PACK + " INTEGER," +
                    RechargeContract.RechargeEntry.COLUMN_DATE + " TEXT," +
                    RechargeContract.RechargeEntry.COLUMN_PROFITPERCENT + " FLOAT," +
                    RechargeContract.RechargeEntry.COLUMN_PROFIT + " FLOAT," +
                    RechargeContract.RechargeEntry.COLUMN_MODE + " TEXT)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RechargeContract.RechargeEntry.TABLE_NAME;

    public RechargeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}

