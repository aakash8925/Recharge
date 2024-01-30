package com.example.recharge;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends Activity {
    private EditText numberEditText;
    private Spinner operatorSpinner, packSpinner;
    private Button saveButton, showtransactionsbtn, storeButton;
    private ArrayAdapter<CharSequence> airtelPackAdapter;
    private ArrayAdapter<CharSequence> jioPackAdapter;

    private RechargeDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberEditText = findViewById(R.id.numberEditText);
        operatorSpinner = findViewById(R.id.spinner_operator);
        packSpinner = findViewById(R.id.spinner_pack);
        saveButton = findViewById(R.id.saveButton);
        storeButton = findViewById(R.id.storeButton);
        showtransactionsbtn = findViewById(R.id.mytransactions);

        // Initialize the database helper
        databaseHelper = new RechargeDatabaseHelper(this);

        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 14) {
                    String trimmedNumber = s.toString().substring(0, 14);
                    numberEditText.setText(trimmedNumber);
                    numberEditText.setSelection(trimmedNumber.length());
                }
            }
        });

        ArrayAdapter<CharSequence> operatorAdapter = ArrayAdapter.createFromResource(this,
                R.array.operator_array, android.R.layout.simple_spinner_item);
        operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operatorSpinner.setAdapter(operatorAdapter);

        operatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOperator = parent.getItemAtPosition(position).toString();
                updatePackSpinner(selectedOperator);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecharge();
            }
        });
        showtransactionsbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this, ViewRechargeActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "An error occurred while opening ViewRechargeActivity.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StoreActivity.class);
                startActivity(intent);
            }
        });
    }
    private void updatePackSpinner(String selectedOperator) {
        if (selectedOperator.equals(getString(R.string.airtel_operator))) {
            airtelPackAdapter = ArrayAdapter.createFromResource(this,
                    R.array.airtel_pack_array, android.R.layout.simple_spinner_item);
            airtelPackAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            packSpinner.setAdapter(airtelPackAdapter);
        } else if (selectedOperator.equals(getString(R.string.jio_operator))) {
            jioPackAdapter = ArrayAdapter.createFromResource(this,
                    R.array.jio_pack_array, android.R.layout.simple_spinner_item);
            jioPackAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            packSpinner.setAdapter(jioPackAdapter);
        }
    }
    private void saveRecharge() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = sdf.format(new Date());
        String number = numberEditText.getText().toString();
        String pack = packSpinner.getSelectedItem().toString();
        String operator = operatorSpinner.getSelectedItem().toString().trim();
        float profitPercent;
        float profit;
        String mode = "cash";

        if (number.startsWith("+91 ")) {
            number = number.substring(4);
        } else {
            if (number.length() != 10) {
                Toast.makeText(MainActivity.this, "Please Enter 10 Digit Numbers", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        sendRechargeDetailsViaWhatsApp(number, operator, pack);

        if (operator.equalsIgnoreCase("Airtel")) {
            profitPercent = 3.0F;
        } else if (operator.equalsIgnoreCase("Jio")) {
            profitPercent = 4.0F;
        } else {
            profitPercent = 0.0F;
        }

        float packValue = Float.parseFloat(pack);
        profit = (packValue * profitPercent) / 100;

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RechargeContract.RechargeEntry.COLUMN_NUMBER, number);
        values.put(RechargeContract.RechargeEntry.COLUMN_OPERATOR, operator);
        values.put(RechargeContract.RechargeEntry.COLUMN_PACK, pack);
        values.put(RechargeContract.RechargeEntry.COLUMN_DATE, date);
        values.put(RechargeContract.RechargeEntry.COLUMN_PROFITPERCENT, profitPercent);
        values.put(RechargeContract.RechargeEntry.COLUMN_PROFIT, profit);
        values.put(RechargeContract.RechargeEntry.COLUMN_MODE, mode);

        try {
            long newRowId = db.insertOrThrow(RechargeContract.RechargeEntry.TABLE_NAME, null, values);

            if (newRowId == -1) {
                Toast.makeText(MainActivity.this, "Error saving recharge details", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Recharge details saved successfully", Toast.LENGTH_SHORT).show();
                numberEditText.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error saving recharge details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void sendRechargeDetailsViaWhatsApp(String number, String operator, String pack) {

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Number: ").append(number).append("\n");
        messageBuilder.append("Operator: ").append(operator).append("\n");
        messageBuilder.append("Pack: ").append(pack).append("\n");

        String message = messageBuilder.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setPackage("com.whatsapp");
        try {
            MainActivity.this.startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            // If WhatsApp is not installed, show a message
            Toast.makeText(MainActivity.this, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
        }
    }
}