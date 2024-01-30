package com.example.recharge;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class StoreActivity extends AppCompatActivity {

    private LinearLayout dateWiseTotalLayout, monthWiseTotalLayout, yearWiseTotalLayout;
    private RechargeDatabaseHelper databaseHelper;

    // Get the current year
    Calendar calendar = Calendar.getInstance();
    int currentYear = calendar.get(Calendar.YEAR);

    int startYear = currentYear - 4;
    int endYear = currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_screen);

        dateWiseTotalLayout = findViewById(R.id.dateWiseTotalLayout);
        monthWiseTotalLayout = findViewById(R.id.monthWiseTotalLayout);
        yearWiseTotalLayout = findViewById(R.id.yearWiseTotalLayout);
        databaseHelper = new RechargeDatabaseHelper(this);

        // Display date-wise, month-wise, and year-wise total amounts
        displayTotalAmounts();
    }

    private void displayTotalAmounts() {
        // Fetch stored details from the database
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        performYearWiseCalculations(db);

        performMonthWiseCalculations(db);

        performDateWiseCalculations(db);

        db.close();
    }
    private void performYearWiseCalculations(SQLiteDatabase db) {
        // Loop through each year
        for (int year = startYear; year <= endYear; year++) {
            // Query to calculate total amount for the year
            Cursor yearCursor = db.rawQuery(
                    "SELECT SUM(" + RechargeContract.RechargeEntry.COLUMN_PACK + ") " +
                            "FROM " + RechargeContract.RechargeEntry.TABLE_NAME +
                            " WHERE strftime('%Y', " + RechargeContract.RechargeEntry.COLUMN_DATE + ") = ?",
                    new String[]{String.valueOf(year)});

            float yearTotalAmount = 0;

            if (yearCursor != null && yearCursor.moveToFirst()) {
                yearTotalAmount = yearCursor.getFloat(0);
                yearCursor.close();
            }

            // Create TextView to display the year and its total amount
            TextView yearTextView = new TextView(this);
            yearTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            yearTextView.setText("Year " + year + " Total: " + yearTotalAmount);
            yearTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            yearTextView.setTypeface(null, Typeface.BOLD);

            // Add the TextView to the year-wise total layout
            yearWiseTotalLayout.addView(yearTextView);
        }
    }
    private void performMonthWiseCalculations(SQLiteDatabase db) {
        // Loop through each year
        for (int year = startYear; year <= endYear; year++) {
            // Create a LinearLayout to hold month-wise totals for the year
            LinearLayout yearMonthLayout = new LinearLayout(this);
            yearMonthLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            yearMonthLayout.setOrientation(LinearLayout.VERTICAL);

            // Loop through each month and calculate the total amount for each month in the specified year
            for (int month = 1; month <= 12; month++) {
                // Query the database to calculate the total amount for the specified month and year
                Cursor monthCursor = db.rawQuery(
                        "SELECT SUM(" + RechargeContract.RechargeEntry.COLUMN_PACK + ") " +
                                "FROM " + RechargeContract.RechargeEntry.TABLE_NAME +
                                " WHERE strftime('%Y', " + RechargeContract.RechargeEntry.COLUMN_DATE + ") = ?" +
                                " AND strftime('%m', " + RechargeContract.RechargeEntry.COLUMN_DATE + ") = ?",
                        new String[]{String.valueOf(year), String.format("%02d", month)}); // Ensure month format is two digits

                float monthTotalAmount = 0;

                // Extract and display the total amount for the month
                if (monthCursor != null && monthCursor.moveToFirst()) {
                    monthTotalAmount = monthCursor.getFloat(0);
                    monthCursor.close();
                }

                // Create TextView to display the month and its total amount
                TextView monthTextView = new TextView(this);
                monthTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                monthTextView.setText(getMonthName(month) + " " + year + " Total: " + monthTotalAmount);
                monthTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                monthTextView.setTypeface(null, Typeface.BOLD);

                // Add the TextView to the LinearLayout for the year
                yearMonthLayout.addView(monthTextView);
            }

            // Add the LinearLayout for the year to the main month-wise total layout
            monthWiseTotalLayout.addView(yearMonthLayout);
        }
    }
    private void performDateWiseCalculations(SQLiteDatabase db) {
        // Loop through each year
        for (int year = startYear; year <= endYear; year++) {
            // Loop through each month and calculate the total amount for each month in the specified year
            for (int month = 1; month <= 12; month++) {
                // Query to calculate total amount for each date in the month
                Cursor dateCursor = db.rawQuery(
                        "SELECT " + RechargeContract.RechargeEntry.COLUMN_DATE + ", SUM(" + RechargeContract.RechargeEntry.COLUMN_PACK + ") " +
                                "FROM " + RechargeContract.RechargeEntry.TABLE_NAME +
                                " WHERE strftime('%Y', " + RechargeContract.RechargeEntry.COLUMN_DATE + ") = ?" +
                                " AND strftime('%m', " + RechargeContract.RechargeEntry.COLUMN_DATE + ") = ?" +
                                " GROUP BY " + RechargeContract.RechargeEntry.COLUMN_DATE,
                        new String[]{String.valueOf(year), String.format("%02d", month)}); // Ensure month format is two digits

                // Display date-wise total amounts for each month
                if (dateCursor != null && dateCursor.moveToFirst()) {
                    // Create a LinearLayout to hold date-wise totals for the month
                    LinearLayout monthDateLayout = new LinearLayout(this);
                    monthDateLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    monthDateLayout.setOrientation(LinearLayout.VERTICAL);

                    // Loop through each date and display its total amount
                    do {
                        String date = dateCursor.getString(0);
                        Float totalAmount = dateCursor.getFloat(1);
                        addTextViewToLayout(monthDateLayout, date + ": " + totalAmount);
                    } while (dateCursor.moveToNext());

                    // Add the LinearLayout for the month to the main date-wise total layout
                    dateWiseTotalLayout.addView(monthDateLayout);

                    dateCursor.close();
                }
            }
        }
    }
    private void addTextViewToLayout(LinearLayout layout, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(textView);
    }

    private String getMonthName(int month) {
        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return "Invalid month";
        }
    }
}