package com.example.client;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
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
            GCMRegistrar.unregister(this);
            GCMRegistrar.register(this, "488364184817");
        }

        setContentView(R.layout.main);

        if(getIntent().getExtras()!=null)
        {
            TextView textView= (TextView) findViewById(R.id.text);
            String name=getIntent().getStringExtra("name");
            String message=getIntent().getStringExtra("message");
            textView.setText("Wiadomość od "+name+":\n"+message);
        }
        else
        {
            TextView textView= (TextView) findViewById(R.id.text);
            textView.setText("W tej aktywności nie ma nic ciekawego");
        }
    }
}
