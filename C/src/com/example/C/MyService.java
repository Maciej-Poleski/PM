package com.example.C;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.*;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {

    private final MyBinder myBinder = new MyBinder();
    private MyActivity activity = null;
    private Timer timer = null;

    public void setActivity(MyActivity activity) {
        this.activity = activity;
    }

    public IBinder onBind(Intent intent) {
        timer = new Timer("observer,true");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        timer.cancel();
        activity = null;
        return false;
    }

    public void start() {
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    InputStream is= new URL("http://grzegorz.gutowski.staff.tcs.uj.edu.pl/board/state/").openStream();
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(is));
                    final StringBuilder result=new StringBuilder();
                    for(;;)
                    {
                        int c=bufferedReader.read();
                        if(c==-1)
                            break;
                        result.append((char)c);
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.setMessage(result.toString());
                        }
                    });
                } catch (IOException e) {
                    // Spróbujemy następnym razem
                    e.printStackTrace();
                }
            }
        }, 0, 1500);
    }

    class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }
}
