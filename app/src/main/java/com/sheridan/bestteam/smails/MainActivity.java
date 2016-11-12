package com.sheridan.bestteam.smails;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private EditText eTxtEmail;
    private ToggleButton togBtnReply;
    private ToggleButton togBtnEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionManager.checkPermissions(this, Manifest.permission.RECEIVE_SMS, Constants.REQUEST_CODE_FOR_RECEIVE_SMS);
        PermissionManager.checkPermissions(this, Manifest.permission.INTERNET, Constants.REQUEST_CODE_FOR_RECEIVE_INTERNET);
        PermissionManager.checkPermissions(this, Manifest.permission.ACCESS_NETWORK_STATE, Constants.REQUEST_CODE_FOR_ACCESS_INTERNET_STATE);
        PermissionManager.checkPermissions(this, Manifest.permission.READ_CONTACTS, Constants.REQUEST_CODE_FOR_ACCESS_READ_CONTACTS);
        setContentView(R.layout.activity_main);
        eTxtEmail = ((EditText) findViewById(R.id.eTxtEmail));
        togBtnReply = ((ToggleButton) findViewById(R.id.togBtnReply));
        togBtnEnable = ((ToggleButton) findViewById(R.id.togBtnEnable));
        sharedPreferences = getPreferences(MODE_PRIVATE);
        LoadPreferences(); // Fills buttons and fields with their saved values.
        togBtnReply.setOnCheckedChangeListener(togBtnReplyOnCheckedChangeListener);
        togBtnEnable.setOnCheckedChangeListener(togBtnEnableOnCheckedChangeListener);
    }


    // Thanks to Marshmallow, we must request permissions before trying to use 'em.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_CODE_FOR_RECEIVE_SMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                togBtnEnable.setEnabled(true);
            else {
                Toast.makeText(MainActivity.this, "SMS is required for this app to work.", Toast.LENGTH_SHORT).show();
                togBtnEnable.setEnabled(false);
            }
        }
        else if (requestCode == Constants.REQUEST_CODE_FOR_RECEIVE_INTERNET) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                togBtnEnable.setEnabled(true);
            else {
                Toast.makeText(MainActivity.this, "Internet and SMS are required for this app to work.", Toast.LENGTH_SHORT).show();
                togBtnEnable.setEnabled(false);
            }
        }
        else if (requestCode == Constants.REQUEST_CODE_FOR_ACCESS_INTERNET_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                togBtnEnable.setEnabled(true);
            else {
                Toast.makeText(MainActivity.this, "Internet and SMS are required for this app to work.", Toast.LENGTH_SHORT).show();
                togBtnEnable.setEnabled(false);
            }
        }
        else if (requestCode == Constants.REQUEST_CODE_FOR_ACCESS_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(MainActivity.this, "Contact names will now be included in the E-Mail Subject!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "Contacts are not required for this App to work.", Toast.LENGTH_SHORT).show();
        }
    }

    private final CompoundButton.OnCheckedChangeListener togBtnReplyOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            SavePreferences();
        }
    };

    private final CompoundButton.OnCheckedChangeListener togBtnEnableOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            PermissionManager.checkPermissions(MainActivity.this, Manifest.permission.RECEIVE_SMS, Constants.REQUEST_CODE_FOR_RECEIVE_SMS);
            SavePreferences();
            if (compoundButton.isChecked()) {
                String userEmail = eTxtEmail.getText().toString();
                if (!userEmail.isEmpty())
                    if (userEmail.contains("@") && userEmail.contains("."))
                        Toast.makeText(MainActivity.this, "Good to Go!", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(MainActivity.this, "Please enter a valid E-Mail address.", Toast.LENGTH_SHORT).show();
                        togBtnEnable.setChecked(false);
                    }
                else {
                    Toast.makeText(MainActivity.this, "The E-Mail field is empty.", Toast.LENGTH_SHORT).show();
                    togBtnEnable.setChecked(false);
                }
            }
        }
    };

    private void SavePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String userEmail = eTxtEmail.getText().toString();
        boolean enableService = togBtnEnable.isChecked();
        boolean enableReply = togBtnReply.isChecked();
        editor.putBoolean("ENABLE", enableService);
        editor.putBoolean("REPLY", enableReply);
        editor.putString("EMAIL", userEmail);
        editor.apply();
    }

    // Fills in the fields with values saved in the SharedPreferences.
    private void LoadPreferences() {
        String userEmail = sharedPreferences.getString("EMAIL", "");
        boolean enableReceiver = sharedPreferences.getBoolean("ENABLE", false);
        boolean enableReply = sharedPreferences.getBoolean("REPLY", false);
        togBtnEnable.setChecked(enableReceiver);
        togBtnReply.setChecked(enableReply);
        eTxtEmail.setText(userEmail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadPreferences();
    }
}
