package com.example.B;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class MyActivity extends Activity {
    ArrayAdapter<String> arrayAdapter;

    private void sendRequest(String request) {
        Intent startServiceIntent = new Intent(this, MainService.class);
        startServiceIntent.putExtra("REQUEST TYPE", request);
        startService(startServiceIntent);
    }

    private void addAddress(String address) {
        Intent startServiceIntent = new Intent(this, MainService.class);
        startServiceIntent.putExtra("REQUEST TYPE", "ADDRESS");
        startServiceIntent.putExtra("ADDRESS", address);
        arrayAdapter.add(address);
        startService(startServiceIntent);
    }

    private void removeAddress(String address) {
        Intent startServiceIntent = new Intent(this, MainService.class);
        startServiceIntent.putExtra("REQUEST TYPE", "ADDRESS REMOVE");
        startServiceIntent.putExtra("ADDRESS", address);
        arrayAdapter.remove(address);
        startService(startServiceIntent);
    }



    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sendRequest("JUST START");
        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.text_view, new ArrayList<String>());
        listView.setAdapter(arrayAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView) view;
                removeAddress(textView.getText().toString());
                return true;
            }
        });
    }

    public void addNewAddress(View v) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String address = editText.getText().toString();
        editText.setText("http://");
        addAddress(address);
    }

}
