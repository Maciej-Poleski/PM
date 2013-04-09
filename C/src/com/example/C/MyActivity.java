package com.example.C;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyActivity extends Activity {

    private MyService service = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.MyBinder binder = (MyService.MyBinder) iBinder;
            service = binder.getService();
            service.setActivity(MyActivity.this);
            service.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };
    private List<Path> myPaths = Collections.synchronizedList(new ArrayList<Path>());

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        MyView view = (MyView) findViewById(R.id.view);
        view.setActivity(this);

        Intent intent = new Intent(this, MyService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    public void newBoardAvailable(final Board board) {
        adjustListOfMyPaths(board);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                repopulateGui(board);
            }
        });
    }

    public void addNewPath(Path path) {
        myPaths.add(path);
        service.sendPath(path,null,null);
    }

    /**
     * Musi być wywołane z wątku GUI
     *
     * @param board
     */
    private void repopulateGui(Board board) {
        MyView view = (MyView) findViewById(R.id.view);
        view.setBoard(board);
        view.addMyPaths(myPaths);
        view.invalidate();
    }

    public void adjustListOfMyPaths(Board board) {
        for (Path path : board.paths) {
            synchronized (myPaths) {
                for (int i = 0; i < myPaths.size(); ++i)
                    if (myPaths.get(i).almostEquals(path)) {
                        myPaths.remove(i);
                        break;
                    }
            }
        }
    }
}
