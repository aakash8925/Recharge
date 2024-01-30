package com.example.recharge;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ViewRechargeActivity extends AppCompatActivity {
    private EditText fromDateEditText, toDateEditText,numberEditText;
    private Button showButton, importButton,exportbtn, deletebtn;
    private List<RechargeDetails> rechargeList;
    private SimpleDateFormat dateFormat;
    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;
    private static final int REQUEST_CODE_SAVE_FILE = 2;

    private String selectedOperator = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recharge);

        showButton = findViewById(R.id.showButton);
        importButton = findViewById(R.id.importButton);
        exportbtn = findViewById(R.id.exportbtn);
        deletebtn = findViewById(R.id.deletebtn);
        fromDateEditText = findViewById(R.id.fromDateEditText);
        toDateEditText = findViewById(R.id.toDateEditText);
        numberEditText = findViewById(R.id.numberEditText);
        rechargeList = new ArrayList<>();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        fromDateEditText.setText(currentDate);
        toDateEditText.setText(currentDate);
        fromDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(fromDateEditText);
            }
        });
        toDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(toDateEditText);
            }
        });
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showAllRechargeDetails(); }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndPickFile();
            }
        });
        exportbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { exportToExcel(v); }
        });
        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { deleteAllData(v); }
        });
    }
    private void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ViewRechargeActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        String formattedDate = dateFormat.format(selectedDate.getTime());
                        editText.setText(formattedDate);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }
    private void showAllRechargeDetails() {
        String fromDate = fromDateEditText.getText().toString();
        String toDate = toDateEditText.getText().toString();
        String mobileNumber = numberEditText.getText().toString();

        rechargeList = fetchRechargeDetailsFromDatabase(fromDate, toDate, mobileNumber);
        String jsonData = convertRechargeListToJson(rechargeList);
        showRechargeDetails(jsonData);
    }
    private String convertRechargeListToJson(List<RechargeDetails> rechargeList) {
        JSONArray jsonArray = new JSONArray();
        for (RechargeDetails recharge : rechargeList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("mobileNumber", recharge.getMobileNumber());
                jsonObject.put("operator", recharge.getOperator());
                jsonObject.put("pack", recharge.getPack());
                jsonObject.put("date", recharge.getDate());
                jsonObject.put("profitPercent", recharge.getProfitpercent());
                jsonObject.put("profit", recharge.getProfit());
                jsonObject.put("mode", recharge.getMode());

                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }
    private void showRechargeDetails(String jsonData) {
        // Parse JSON data and filter if necessary
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        LinearLayout rechargeDetailsLayout = findViewById(R.id.rechargeDetailsLayout);
        rechargeDetailsLayout.removeAllViews();

        if (jsonArray.length() == 0) {
            // If no data found, display a message
            TextView noDataTextView = new TextView(this);
            noDataTextView.setText("No data found");
            rechargeDetailsLayout.addView(noDataTextView);
            return;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String jsonString = String.format("Mobile Number: %s\nOperator: %s\nPack: %s\nDate: %s",
                        jsonObject.getString("mobileNumber"),
                        jsonObject.getString("operator"),
                        jsonObject.getString("pack"),
                        jsonObject.getString("date"));

                EditText editText = new EditText(this);
                editText.setText(jsonString);
                editText.setFocusable(false);
                editText.setClickable(true);
                rechargeDetailsLayout.addView(editText);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void showFilterMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.airtel_option:
                        selectedOperator = "Airtel";
                        break;
                    case R.id.jio_option:
                        selectedOperator = "Jio";
                        break;
                }
                // Show filtered recharge details
                showFilteredRechargeDetails(selectedOperator);
                return true;
            }
        });
        popupMenu.show();
    }
    private void showFilteredRechargeDetails(String selectedOperator) {
        String jsonData = convertRechargeListToJson(rechargeList);
        showRechargeDetailsByOperator(jsonData, selectedOperator);
    }
    private void showRechargeDetailsByOperator(String jsonData, String selectedOperator) {
        JSONArray filteredArray = new JSONArray();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("operator").equalsIgnoreCase(selectedOperator)) {
                    filteredArray.put(jsonObject);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showRechargeDetails(filteredArray.toString());
    }
    private List<RechargeDetails> fetchRechargeDetailsFromDatabase(String fromDate, String toDate, String mobileNumber) {
        List<RechargeDetails> rechargeList = new ArrayList<>();

        RechargeDatabaseHelper databaseHelper = new RechargeDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT " +
                RechargeContract.RechargeEntry.COLUMN_NUMBER + ", " +
                RechargeContract.RechargeEntry.COLUMN_OPERATOR + ", " +
                RechargeContract.RechargeEntry.COLUMN_PACK + ", " +
                RechargeContract.RechargeEntry.COLUMN_DATE + ", " +
                RechargeContract.RechargeEntry.COLUMN_PROFITPERCENT + ", " +
                RechargeContract.RechargeEntry.COLUMN_PROFIT +", " +
                RechargeContract.RechargeEntry.COLUMN_MODE +
                " FROM " + RechargeContract.RechargeEntry.TABLE_NAME;

        // Apply filters based on fromDate, toDate, and mobileNumber
        List<String> whereClauses = new ArrayList<>();
        if (!fromDate.isEmpty()) {
            whereClauses.add(RechargeContract.RechargeEntry.COLUMN_DATE + " >= '" + fromDate + "'");
        }
        if (!toDate.isEmpty()) {
            whereClauses.add(RechargeContract.RechargeEntry.COLUMN_DATE + " <= '" + toDate + "'");
        }
        if (!mobileNumber.isEmpty()) {
            whereClauses.add(RechargeContract.RechargeEntry.COLUMN_NUMBER + " = '" + mobileNumber + "'");
        }
        if (!whereClauses.isEmpty()) {
            query += " WHERE " + String.join(" AND ", whereClauses);
        }

        // Execute the query
        Cursor cursor = db.rawQuery(query, null);

        // Process the results
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String number = cursor.getString(cursor.getColumnIndexOrThrow(RechargeContract.RechargeEntry.COLUMN_NUMBER));
                String operator = cursor.getString(cursor.getColumnIndexOrThrow(RechargeContract.RechargeEntry.COLUMN_OPERATOR));
                String pack = cursor.getString(cursor.getColumnIndexOrThrow(RechargeContract.RechargeEntry.COLUMN_PACK));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(RechargeContract.RechargeEntry.COLUMN_DATE));
                Float profitpercent = Float.valueOf(String.valueOf(cursor.getFloat(cursor.getColumnIndexOrThrow(RechargeContract.RechargeEntry.COLUMN_DATE))));
                Float profit = Float.valueOf(String.valueOf(cursor.getFloat(cursor.getColumnIndexOrThrow(RechargeContract.RechargeEntry.COLUMN_DATE))));
                String mode = cursor.getString(cursor.getColumnIndexOrThrow(RechargeContract.RechargeEntry.COLUMN_DATE));

                // Create a new RechargeDetails object with the retrieved data
                RechargeDetails rechargeDetails = new RechargeDetails(number, operator, pack, date, profitpercent, profit, mode);
                rechargeList.add(rechargeDetails);
            } while (cursor.moveToNext());

            cursor.close();
        }
        db.close();
        return rechargeList;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            importRechargeDetails(fileUri);
        } else if (requestCode == REQUEST_CODE_SAVE_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                String jsonData = convertRechargeListToJson(rechargeList);
                saveExcelFile(uri,jsonData);
            }
        }
    }
    private void checkPermissionsAndPickFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            openFilePicker();
        } else {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                openFilePicker();
            } else {
                // Permission denied
                Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.ms-excel");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }
    private void importRechargeDetails(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            RechargeDatabaseHelper databaseHelper = new RechargeDatabaseHelper(this);
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            db.beginTransaction();
            try {
                Iterator<Row> rowIterator = sheet.iterator();
                rowIterator.next();

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    String mobileNumber = String.valueOf((long) row.getCell(0).getNumericCellValue());
                    String operator = row.getCell(1).getStringCellValue();

                    // Retrieve the pack value and convert it to float
                    Float pack;
                    Cell packCell = row.getCell(2);
                    if (packCell != null && packCell.getCellType() == CellType.NUMERIC) {
                        pack = (float) packCell.getNumericCellValue();
                    } else {
                        pack = 0.0f; // Default value or handle the error accordingly
                    }

                    Date date = row.getCell(3).getDateCellValue();
                    String formattedDate = sdf.format(date);

                    // Calculate profit and mode based on operator
                    Float profitPercent;
                    if (operator.equalsIgnoreCase("Airtel")) {
                        profitPercent = 3.0F;
                    } else if (operator.equalsIgnoreCase("Jio")) {
                        profitPercent = 4.0F;
                    } else {
                        profitPercent = 0.0F;
                    }
                    Float profit = (float) (pack * profitPercent / 100.0);
                    String mode;
                    if (operator.equalsIgnoreCase("Airtel") || operator.equalsIgnoreCase("Jio")) {
                        mode = "cash";
                    } else {
                        mode = "";
                    }

                    // Insert data into the database
                    ContentValues values = new ContentValues();
                    values.put(RechargeContract.RechargeEntry.COLUMN_NUMBER, mobileNumber);
                    values.put(RechargeContract.RechargeEntry.COLUMN_OPERATOR, operator);
                    values.put(RechargeContract.RechargeEntry.COLUMN_PACK, pack);
                    values.put(RechargeContract.RechargeEntry.COLUMN_DATE, formattedDate);
                    values.put(RechargeContract.RechargeEntry.COLUMN_PROFITPERCENT, profitPercent);
                    values.put(RechargeContract.RechargeEntry.COLUMN_PROFIT, profit);
                    values.put(RechargeContract.RechargeEntry.COLUMN_MODE, mode);

                    // Insert the values into the database
                    db.insert(RechargeContract.RechargeEntry.TABLE_NAME, null, values);
                }

                db.setTransactionSuccessful();
                // Show a success message
                Toast.makeText(this, "Recharge details imported and saved successfully!", Toast.LENGTH_SHORT).show();
            } finally {
                db.endTransaction();
                db.close();
                workbook.close();
            }
        } catch (EncryptedDocumentException | InvalidFormatException e) {
            e.printStackTrace();
            // Show an error message for invalid format or encrypted file
            Toast.makeText(this, "Invalid Excel file format or encrypted file", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show an error message for IO exception
            Toast.makeText(this, "Error importing and saving recharge details: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            // Show a generic error message for other exceptions
            Toast.makeText(this, "Error importing and saving recharge details: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void exportToExcel(View view) {
        String fileName = "RechargeData.xlsx";

        // Create a new Intent for file picker
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        // Start the activity for result
        startActivityForResult(intent, REQUEST_CODE_SAVE_FILE);
    }
    private void saveExcelFile(Uri uri, String jsonData) {
        try {
            // Export details to Excel
            try {
                // Create a new workbook
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Recharge Details");

                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Mobile Number");
                headerRow.createCell(1).setCellValue("Operator");
                headerRow.createCell(2).setCellValue("Pack");
                headerRow.createCell(3).setCellValue("Date");
                headerRow.createCell(4).setCellValue("Profit Percent");
                headerRow.createCell(5).setCellValue("Profit");

                // Style for header cells
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                // Apply the header style to all cells in the header row
                for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                    headerRow.getCell(i).setCellStyle(headerStyle);
                }

                // Parse JSON data and write to Excel
                JSONArray jsonArray = new JSONArray(jsonData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(jsonObject.getString("mobileNumber"));
                    row.createCell(1).setCellValue(jsonObject.getString("operator"));
                    row.createCell(2).setCellValue(jsonObject.getString("pack"));
                    row.createCell(3).setCellValue(jsonObject.getString("date"));
                    row.createCell(4).setCellValue(jsonObject.getString("profitPercent"));
                    row.createCell(5).setCellValue(jsonObject.getString("profit"));
                }

                // Save the workbook to the specified URI
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                workbook.write(outputStream);
                outputStream.close();
                workbook.close();

                // Show a success message
                Toast.makeText(this, "Recharge details exported to Excel", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                // Show an error message
                Toast.makeText(this, "Error exporting recharge details to Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show a generic error message for other exceptions
            Toast.makeText(this, "Error exporting recharge details to Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void deleteAllData(View view) {

        RechargeDatabaseHelper databaseHelper = new RechargeDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        // Delete all rows from the table
        db.delete(RechargeContract.RechargeEntry.TABLE_NAME, null, null);
        // Close the database
        db.close();
        LinearLayout rechargeDetailsLayout = findViewById(R.id.rechargeDetailsLayout);
        rechargeDetailsLayout.removeAllViews();
        Toast.makeText(this, "All data deleted", Toast.LENGTH_SHORT).show();
    }

}