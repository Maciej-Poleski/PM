package com.example.client;

import android.content.Context;
import android.content.Intent;
import com.google.android.gcm.GCMBaseIntentService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
        } catch (ClientProtocolException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        System.err.println("Received some message");
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
