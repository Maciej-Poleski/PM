package com.example.client;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gcm.GCMRegistrar;

public class MyActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, "488364184817");
        } else {
            System.err.println("Already registered: "+regId);
            GCMRegistrar.unregister(this);
            GCMRegistrar.register(this, "488364184817");
        }
    }
}
