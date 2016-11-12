package com.sheridan.bestteam.smails;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

@SuppressLint("Registered")
public class SMSEmailService extends IntentService  {

    public SMSEmailService() {
        super("SMSEmailService");
    }

    // Here we send our email out to the user.
    @Override
    protected void onHandleIntent(Intent intent) {

        /*String textNumber = intent.getStringExtra("textNumber");
        String textBody = intent.getStringExtra("textBody");*/
        Log.d(Constants.SMSEMAILSERVICE_LOG_TAG, "We've received an " + intent.getAction() + " intent.");
    }
}
