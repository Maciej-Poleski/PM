package com.example.client;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.google.android.gcm.GCMBaseIntentService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Maciej Poleski
 * Date: 22.05.13
 * Time: 15:16
 */
public class GCMIntentService extends GCMBaseIntentService {
    public GCMIntentService() {
        super("488364184817");
    }

    private static void sendRegIdToServer(String regId) {
        System.err.println("sending regId: " + regId);

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://maciej.poleski.student.tcs.uj.edu.pl/gcm3/register/");

// Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("regId", regId));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

//Execute and get the response.
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            entity.writeTo(System.err);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        String name = intent.getStringExtra("name");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(GCMIntentService.this)
                .setSmallIcon(R.drawable.monitor)
                .setContentTitle("Wiadomość z chmury")
                .setContentText(message);
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setClass(this, MyActivity.class);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("name", name);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(GCMIntentService.this, 0, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((name + message).hashCode(), mBuilder.build());
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        System.err.println("Recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    @Override
    protected void onError(Context context, String s) {
        System.err.println("An error occured: " + s);
    }

    @Override
    protected void onRegistered(Context context, String s) {
        sendRegIdToServer(s);
    }

    @Override
    protected void onUnregistered(Context context, String s) {
        // unsupported - do nothing
    }
}
