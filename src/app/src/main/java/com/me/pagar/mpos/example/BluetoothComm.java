package com.me.pagar.mpos.example;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

interface BluetoothEvent {
    void startDiscovery();
    void finishDiscovery(List<BluetoothEntity> bluetoothList);
}

public class BluetoothComm {
    public final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter = null;
    private List<BluetoothEntity> bluetoothList;
    private Activity activity;
    private boolean pinpadWasFound = false;

    BluetoothEvent bluetoothEvent;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    BluetoothEntity btEntity = new BluetoothEntity(device);
                    bluetoothList.add(btEntity);
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                bluetoothEvent.startDiscovery();
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                bluetoothEvent.finishDiscovery(bluetoothList);
            }

        }
    };

    public BluetoothComm(Activity activity) throws NullPointerException {
        this.activity = activity;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothList = new ArrayList<BluetoothEntity>();
        this.pinpadWasFound = false;
        if (this.bluetoothAdapter != null) {
            if (!this.bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            throw new NullPointerException("BluetoothComm");
        }
        ActivityCompat.requestPermissions(this.activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                BluetoothEntity btEntity = new BluetoothEntity(device);
                this.bluetoothList.add(btEntity);
                if (btEntity.getDeviceName().contains("PAX-6J97")) {
                    Log.d("PINPADExample", btEntity.getDeviceName());
                    this.pinpadWasFound = true;
                }
            }
        }

    }

    public boolean pinpadWasDicovered() {
        return this.pinpadWasFound;
    }

    public List<BluetoothEntity> getBluetoothList() {
        return bluetoothList;
    }

    public void discoveryDevices() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.activity.registerReceiver(this.receiver, filter);
        this.bluetoothAdapter.startDiscovery();
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        this.activity.registerReceiver(this.receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.activity.registerReceiver(this.receiver, filter);
    }


    public void stopDiscoveryDevices() {
        this.activity.unregisterReceiver(this.receiver);
    }

    public void addListener(BluetoothEvent bluetoothEvent) {
        this.bluetoothEvent = bluetoothEvent;
    }
}
