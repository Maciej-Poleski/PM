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

import java.io.*;
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
    private final static List<String> addressList = Collections.synchronizedList(new ArrayList<String>());
    private final Map<String, byte[]> sites = Collections.synchronizedMap(new HashMap<String, byte[]>());

    public static List<String> getAddressList() {
        return addressList;
    }

    private void saveState() {
        try {
            FileOutputStream fileOutputStream = openFileOutput("state", MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(addressList);
            objectOutputStream.writeObject(sites);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException ignored) {
        }
    }

    private void loadState() {
        try {
            FileInputStream fileInputStream = openFileInput("state");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            List<String> addressList = (List<String>) objectInputStream.readObject();
            Map<String, byte[]> sites = (Map<String, byte[]>) objectInputStream.readObject();
            synchronized (MainService.addressList) {
                MainService.addressList.clear();
                MainService.addressList.addAll(addressList);
            }
            synchronized (this.sites) {
                this.sites.clear();
                this.sites.putAll(sites);
            }
            objectInputStream.close();
            fileInputStream.close();
            MyActivity activity2 = MyActivity.getInstance();
            if (activity2 != null) {
                activity2.getArrayAdapter().notifyDataSetChanged();
            }
        } catch (IOException ignored) {
        } catch (ClassNotFoundException ignored) {
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Bardzo leniwie. Nie wznawia urządzenia. Następny CHECK względem poprzedniego.
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
                    frozenAddressList = new ArrayList<String>(addressList);
                }
                for (String address : frozenAddressList) {
                    AndroidHttpClient httpClient = null;
                    InputStream is = null;
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
                            boolean result = Arrays.equals(t1, digest);
                            if (!result) {
                                // Odpalić przeglądarke
                                sites.put(address, digest);

                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainService.this)
                                        .setSmallIcon(R.drawable.monitor)
                                        .setContentTitle("Web page changed")
                                        .setContentText(address + " changed");
                                Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(MainService.this, 0, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
                                mBuilder.setContentIntent(resultPendingIntent);
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotificationManager.notify(address.hashCode(), mBuilder.build());
                            }
                        } else {
                            sites.put(address, digest);
                            saveState();
                        }
                    } catch (Exception e) {
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
    public void onCreate() {
        super.onCreate();
        loadState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String requestType = intent.getStringExtra("REQUEST TYPE");
        if (requestType.equals("JUST START")) {
            performGlobalCheck();

        } else if (requestType.equals("ADDRESS")) {
            addressList.add(intent.getStringExtra("ADDRESS"));
            saveState();
            MyActivity activity = MyActivity.getInstance();
            if (activity != null)
                activity.getArrayAdapter().notifyDataSetChanged();

        } else if (requestType.equals("ADDRESS REMOVE")) {
            addressList.remove(intent.getStringExtra("ADDRESS"));
            saveState();
            MyActivity activity2 = MyActivity.getInstance();
            if (activity2 != null)
                activity2.getArrayAdapter().notifyDataSetChanged();

        } else if (requestType.equals("GLOBAL CHECK")) {
            performGlobalCheck();

        }
        return START_STICKY;
    }
}
