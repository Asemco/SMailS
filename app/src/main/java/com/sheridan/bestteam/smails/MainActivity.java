package com.sheridan.bestteam.smails;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private Intent mServiceIntent;
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
        setContentView(R.layout.activity_main);
        eTxtEmail = ((EditText) findViewById(R.id.eTxtEmail));
        togBtnReply = ((ToggleButton) findViewById(R.id.togBtnReply));
        togBtnEnable = ((ToggleButton) findViewById(R.id.togBtnEnable));
        sharedPreferences = getPreferences(MODE_PRIVATE);
        LoadPreferences();
        togBtnReply.setOnCheckedChangeListener(togBtnReplyOnCheckedChangeListener);
        togBtnEnable.setOnCheckedChangeListener(togBtnEnableOnCheckedChangeListener);
        if (togBtnEnable.isEnabled())
            startSMSService();
    }


    // Thanks to Marshmallow, we must request permissions before trying to use 'em.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_CODE_FOR_RECEIVE_SMS)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                togBtnEnable.setEnabled(true);
            else {
                togBtnEnable.setEnabled(false);
                Toast.makeText(MainActivity.this, "All Permissions are required for this app to function.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == Constants.REQUEST_CODE_FOR_RECEIVE_INTERNET)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                togBtnEnable.setEnabled(true);
            else {
                togBtnEnable.setEnabled(false);
                Toast.makeText(MainActivity.this, "All Permissions are required for this app to function.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == Constants.REQUEST_CODE_FOR_ACCESS_INTERNET_STATE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                togBtnEnable.setEnabled(true);
            else
                togBtnEnable.setEnabled(false);
            Toast.makeText(MainActivity.this, "All Permissions are required for this app to function.", Toast.LENGTH_SHORT).show();
        }
    }

    CompoundButton.OnCheckedChangeListener togBtnReplyOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            SavePreferences();
        }
    };

    CompoundButton.OnCheckedChangeListener togBtnEnableOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            SavePreferences();
            PermissionManager.checkPermissions(MainActivity.this, Manifest.permission.RECEIVE_SMS, Constants.REQUEST_CODE_FOR_RECEIVE_SMS);
            if (b) {
                String userEmail = eTxtEmail.getText().toString();
                if (!userEmail.isEmpty())
                    if (userEmail.contains("@") && userEmail.contains("."))
                        startSMSService();
                    else {
                        Toast.makeText(MainActivity.this, "Please enter a valid E-Mail address.", Toast.LENGTH_SHORT).show();
                        togBtnEnable.setChecked(false);
                    }
                else {
                    Toast.makeText(MainActivity.this, "The E-Mail field is empty.", Toast.LENGTH_SHORT).show();
                    togBtnEnable.setChecked(false);
                }
            }
            else
            {
                stopSMSService();
            }
        }
    };

    private void stopSMSService() {

    }

    private void startSMSService() {
        if (sharedPreferences.getBoolean("ENABLE", false)) {
            if (!eTxtEmail.getText().toString().isEmpty())
                Toast.makeText(this, "Good to Go!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Make sure you fill in your E-Mail address!", Toast.LENGTH_SHORT).show();
        }
    }

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
        boolean enableService = sharedPreferences.getBoolean("ENABLE", false);
        boolean enableReply = sharedPreferences.getBoolean("REPLY", false);
        togBtnEnable.setChecked(enableService);
        togBtnReply.setChecked(enableReply);
        eTxtEmail.setText(userEmail);
    }


}
