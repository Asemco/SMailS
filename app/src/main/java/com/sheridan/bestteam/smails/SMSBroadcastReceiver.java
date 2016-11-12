package com.sheridan.bestteam.smails;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Crasy on 2016-11-10.
 */

public class SMSBroadcastReceiver extends BroadcastReceiver {


    SmsMessage[] smsMessage;
    String message;
    JSONObject smsData;

    @Override
    public void onReceive(Context context, Intent intent) {

        /*Log.d(Constants.SMSBROADCASTRECEIVER_LOG_TAG, "We've received an " + intent.getAction() + " intent.");*/ // For Debugging
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        // A Notification Builder is used to make our Notification.
        NotificationCompat.Builder notifc = new NotificationCompat.Builder(context);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (sharedPreferences.getBoolean("ENABLE", false)) {
            // Retrieve the SmsMessage array from the intent.
            smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            // A boolean is used to check if the E-Mail was sent correctly.
            // There's probably a better way through the Async Method, but this is easier.
            message = "";
            for (SmsMessage sms : smsMessage) {
                message = message + sms.getMessageBody(); // Create a single string for the SMS Message.
            }

            smsData = new JSONObject();
            try {
                // Store the pertinent information as a JSONObject
                smsData.put("name", smsMessage[0].getOriginatingAddress() + "@yourphone.com");
                smsData.put("subject", "You've received a Text from " + smsMessage[0].getOriginatingAddress() + "! - SMailS");
                smsData.put("body", message);
                smsData.put("to", sharedPreferences.getString("EMAIL", ""));
            /*Log.d(Constants.SMSBROADCASTRECEIVER_LOG_TAG, sharedPreferences.getString("EMAIL", "") + " OK?");*/ // For Debugging

                // If there's no Email set in the MainActivity, we can't send an E-Mail and
                // Notify the user as to why with a Notification.
                if (smsData.getString("to").isEmpty()) {
                    notifc.setCategory(Notification.CATEGORY_MESSAGE);
                    notifc.setSmallIcon(R.drawable.peco);
                    notifc.setContentTitle("Please set an E-Mail address in the SMailS app!");
                    notifc.setContentText("Tap here to open the SMailS app.");
                    Intent mainActivity = new Intent(context, MainActivity.class);
                    PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                            mainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
                    notifc.setContentIntent(resultPendingIntent);
                    synchronized (notifc) {
                        manager.notify(1, notifc.build());
                    }
                    return;
                }
            } catch (Exception e) {
                //If we run into an error making this JSONObject, we have bic problems.
                e.printStackTrace();

            /*Log.d(Constants.SMSBROADCASTRECEIVER_LOG_TAG, "Error making JSON Objects. Stopping.");*/ // For Debugging

                notifc.setCategory(Notification.CATEGORY_ERROR);
                notifc.setSmallIcon(R.drawable.peco);
                notifc.setContentTitle("An error occurred with the SMailS app.  Please try restarting!");
                notifc.setContentText("Also, try setting your E-Mail again.");
                Intent mainActivity = new Intent(context, MainActivity.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                        mainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
                notifc.setContentIntent(resultPendingIntent);
                synchronized (notifc) {
                    manager.notify(1, notifc.build());
                }
                return;
            }

            // We check that we can connect to the internet, before attempting to send a notification out.
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // Attempts to send the E-Mail through an Async method
                DownloadWebPageTask task = new DownloadWebPageTask();
                try {
                    String result = "";
                    result = task.execute("https://script.google.com/macros/s/AKfycbwsEvRTeLIaQIqSZ8yyUiclV-W5VYPprErxo-YkxDld6RafFntF/exec").get();
                    if (!result.isEmpty()) {
                        notifc.setCategory(Notification.CATEGORY_MESSAGE);
                        notifc.setSmallIcon(R.drawable.peco);
                        notifc.setContentTitle("SMailS E-mailed Successfully!");
                        notifc.setContentText("From " + smsMessage[0].getOriginatingAddress() + " to " + sharedPreferences.getString("EMAIL", ""));
                        smsMessage = null;
                        message = null;
                        smsData = null;
                        synchronized (notifc) {
                            manager.notify(1, notifc.build());
                        }
                    } else {
                        notifc.setCategory(Notification.CATEGORY_ERROR);
                        notifc.setSmallIcon(R.drawable.peco);
                        notifc.setContentTitle("Failed to send an E-Mail!!");
                        notifc.setContentText("Problems connecting to the E-Mail server.");
                        synchronized (notifc) {
                            manager.notify(1, notifc.build());
                        }
                    }
                } catch (Exception e) {
                /*Log.d(Constants.SMSBROADCASTRECEIVER_LOG_TAG, e.getMessage());*/ // For Debugging
                    notifc.setCategory(Notification.CATEGORY_ERROR);
                    notifc.setSmallIcon(R.drawable.peco);
                    notifc.setContentTitle("Failed to send an E-Mail!!");
                    notifc.setContentText("Was Wi-fi/Data toggled off recently?");
                    synchronized (notifc) {
                        manager.notify(1, notifc.build());
                    }
                }
            } else // If we cannot connect to the internet, we inform the user with a Notification.
            {
                notifc.setCategory(Notification.CATEGORY_ERROR);
                notifc.setSmallIcon(R.drawable.peco);
                notifc.setContentTitle("SMailS cannot connect to the internet!");
                notifc.setContentText("Is Wi-Fi/Data enabled?");
                synchronized (notifc) {
                    manager.notify(1, notifc.build());
                }
            }
        }
    }

    // Returns the To E-Mail address if successful.
    private String getSMSSendResult(String emailUrl) {
        String response = "";
        try {
            URL url = new URL(emailUrl);
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                // Open the output stream to allow us to add POST data
                httpURLConnection.setDoOutput(true);
                // Open the input stream to receive the response data.
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // We get access to the response through an outputStream.
                OutputStream os = httpURLConnection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os);
                // Write our JSONObject as a string to the Request.
                writer.write(smsData.toString());
                /*Log.d(Constants.SMSBROADCASTRECEIVER_LOG_TAG, smsData.toString());*/ // For Debugging

                // We flush the writer and close it, and the OutputStream, before we forget.
                writer.flush();
                writer.close();
                os.close();
                // We get the responseCode and check that it's 200.
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Each line in the response is read sequentially through the BufferedReader
                    // and added to the local response variable. - For Debugging
                    /*String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    // We then close the buffered reader
                    br.close();*/
                    /*Log.d(Constants.SMSBROADCASTRECEIVER_LOG_TAG, "Successful Send!?\n" + response);*/ // For Debuggings

                    // We then set all variables to null to save space between SMSes.
                    response = smsData.getString("to");
                    return response;
                } else {
                    Log.d(Constants.SMSBROADCASTRECEIVER_LOG_TAG, "Response Code was: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*Log.d(Constants.SMSBROADCASTRECEIVER_LOG_TAG, "Failed along the way");*/ // For Debugging
        // If we fail along the way, we still set all variables to null to save the space.
        smsMessage = null;
        message = null;
        smsData = null;
        response = "";
        return response;
    }

    // Runs the web page request Async.  Required so Android doesn't complain/CRASH.
    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                return getSMSSendResult(strings[0]);
            } catch (Exception e) {
                return "";
            }
        }
    }
}



