package com.me.pagar.mpos.example;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.me.pagar.mpos.example.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BluetoothEvent {
    private String PINPAD_LOG_TAG = "PINPADExample";
    private BluetoothComm btComm;
    private BluetoothEvent bluetoothEvent;
    private Activity activity;

    private void startPagarMeAPI(List<BluetoothEntity> bluetoothList, int amount, String paymentMethod, Boolean updateTable) {
        for (BluetoothEntity bluetoothEntity : bluetoothList) {

            if (bluetoothEntity.getDeviceName().contains("PAX-")) {
                PagarMeSDKInterface pagarMeSDKInterface = new PagarMeSDKInterface("SUA_API_KEY", "SUA_ENCRYPTION_KEY");

                 ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
                 binding.setTransactionViewModel(pagarMeSDKInterface.transactionViewModel);

                try {
                    pagarMeSDKInterface.init(bluetoothEntity.getDevice(), getApplicationContext(), amount, paymentMethod, updateTable);
                } catch (IOException e) {
                    Log.e("PINPAD_EXAMPLE_PAGARME", "Error in pagarMeSDKInterface.init() - " + e.getMessage());
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.bluetoothEvent = this;
        this.activity = this;

        Button btnCreateTransaction = findViewById(R.id.btnCreate);
        btnCreateTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    btComm = new BluetoothComm(activity);
                    if (!btComm.pinpadWasDicovered()) {
                        btComm.addListener(bluetoothEvent);
                        btComm.discoveryDevices();
                    } else {
                        startPagarMeAPI(btComm.getBluetoothList(), 1000, "credit", true);
                    }
                } catch (NullPointerException e) {
                    Log.e(PINPAD_LOG_TAG, "Device does not support bluetooth");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void startDiscovery() {
        Log.d(PINPAD_LOG_TAG, "startDiscovery()");
    }

    @Override
    public void finishDiscovery(List<BluetoothEntity> bluetoothList) {
        Log.d(PINPAD_LOG_TAG, "finishDiscovery()");
        startPagarMeAPI(bluetoothList, 1000, "credit", true);
    }
}
