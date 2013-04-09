package com.example.C;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Xml;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {

    private final MyBinder myBinder = new MyBinder();
    private final Object activityLock = new Object();
    private MyActivity activity = null;
    private Timer timer = null;
    private Board oldBoard = null;

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
        synchronized (activityLock) {
            activity = null;
        }
        return false;
    }

    public void start() {
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    InputStream is = new URL("http://grzegorz.gutowski.staff.tcs.uj.edu.pl/board/state/").openStream();
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(is, null);
                        parser.nextTag();

                        Board board = Board.readFromXml(parser);
                        if (!board.equals(oldBoard)) {
                            oldBoard = board;
                            synchronized (activityLock) {
                                if (activity != null)
                                    activity.newBoardAvailable(board);
                            }
                        }

                    } catch (XmlPullParserException e) {
                        // Czasem się zdarza, gdy strumień jest pusty
                    } finally {
                        is.close();
                    }
                } catch (IOException e) {
                    // Spróbujemy następnym razem
                }
            }
        }, 0, 1000);
    }

    public void sendPath(final Path path, final Runnable successCallback, final Runnable failCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String xml = path.getXml(MyService.this);
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://grzegorz.gutowski.staff.tcs.uj.edu.pl/board/newpath/");

                    httppost.setEntity(new StringEntity(xml));

                    HttpResponse response = httpclient.execute(httppost);
                    if (successCallback != null)
                        successCallback.run();
                } catch (Exception e) {
                    if (failCallback != null)
                        failCallback.run();
                }
            }
        }).start();

    }

    class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }
}
