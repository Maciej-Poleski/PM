package com.example.B;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;

/**
 * User: Maciej Poleski
 * Date: 23.03.13
 * Time: 18:52
 */
public class MainService extends Service {
    List<String> addressList = Collections.synchronizedList(new ArrayList<String>());
    Map<String, byte[]> sites = Collections.synchronizedMap(new HashMap<String, byte[]>());

    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Bardzo leniwie. Nie wznamia urządzenia. Następny CHECK względem poprzedniego.
     */
    private void setupNextGlobalCheck() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent globalCheckIntent = new Intent(this, MainService.class);
        globalCheckIntent.putExtra("REQUEST TYPE", "GLOBAL CHECK");
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000 * 5, PendingIntent.getService(this, 0, globalCheckIntent, 0));
    }

    private void performGlobalCheck() {
        setupNextGlobalCheck();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> frozenAddressList;
                synchronized (addressList) {
                    frozenAddressList = new ArrayList<>(addressList);
                }
                for (String address : frozenAddressList) {
                    AndroidHttpClient httpClient = null;
                    InputStream is = null;
                    long count = 0;
                    try {
                        URL url = new URL(address);
                        URLConnection connection = url.openConnection();
                        is = new BufferedInputStream(connection.getInputStream());
                        // httpClient = AndroidHttpClient.newInstance("MainService");
                        //  HttpResponse response = httpClient.execute(new HttpGet(address));
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        try {
                            is = new DigestInputStream(is, md);
                            byte[] content = new byte[1000];
                            while (is.read(content) != -1) {
                            }
                        } finally {
                            is.close();
                        }
                        byte[] digest = md.digest();
                        if (sites.containsKey(address)) {
                            byte[] t1 = sites.get(address);
                            byte[] t2 = digest;
                            boolean result = Arrays.equals(t1, t2);
                            if (!result) {
                                // Odpalić przeglądarke
                                sites.put(address, digest);

                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainService.this)
                                        .setSmallIcon(123)
                                        .setContentTitle("Web page changed")
                                        .setContentText(address + " changed");
                                Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(MainService.this, 0, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
                                mBuilder.setContentIntent(resultPendingIntent);
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotificationManager.notify(address.hashCode(), mBuilder.build());
                                System.out.println("notification sent");
                            }
                        } else {
                            sites.put(address, digest);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // To nic - spróbujemy następnym razem (albo nie URI)
                    } finally {
                        if (httpClient != null) {
                            httpClient.close();
                        }
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String requestType = intent.getStringExtra("REQUEST TYPE");
        switch (requestType) {
            case "JUST START":
                performGlobalCheck();
                break;
            case "ADDRESS":
                addressList.add(intent.getStringExtra("ADDRESS"));
                break;
            case "ADDRESS REMOVE":
                addressList.remove(intent.getStringExtra("ADDRESS"));
                break;
            case "GLOBAL CHECK":
                performGlobalCheck();
                break;
            default:
                System.out.println("Dziwny request: " + requestType);
                break;
        }
        return START_STICKY;
    }
}
