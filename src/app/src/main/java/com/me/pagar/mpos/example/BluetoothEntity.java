package com.me.pagar.mpos.example;

import android.bluetooth.BluetoothDevice;

public class BluetoothEntity {

    private BluetoothDevice device;
    private String deviceName;
    private String deviceHardwareAddress;


    public BluetoothEntity(BluetoothDevice device) {
        this.device = device;
        this.deviceName = device.getName();
        this.deviceHardwareAddress = device.getAddress(); // MAC address
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMacAddress() {
        return deviceHardwareAddress;
    }

    public void setDeviceMacAddress(String macAddress) {
        this.deviceHardwareAddress = macAddress;
    }

}

