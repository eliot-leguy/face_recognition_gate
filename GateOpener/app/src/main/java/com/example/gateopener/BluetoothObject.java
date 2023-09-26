package com.example.gateopener;

import android.os.ParcelUuid;

public class BluetoothObject {

    private String name;
    private String address;
    private ParcelUuid[] uuids;
    private int type;
    private int bondState;

    public BluetoothObject(String name, String address){
        this.name = name;
        this.address = address;
    }

    public void setBluetooth_state(int bondState) {
        this.bondState = bondState;
    }

    public int getBluetooth_state() {
        return this.bondState;
    }

    public void setBluetooth_type(int type) {
        this.type = type;
    }
    public int getBluetooth_type() {
        return this.type;
    }

    public void set_Bluetooth_uuid(ParcelUuid[] uuids) {
        this.uuids = uuids;
    }
    public ParcelUuid[] getBluetooth_uuid() {
        return this.uuids;
    }

    public void setBluetooth_name(String name) {
        this.name = name;
    }
    public String getBluetooth_name() {
        return this.name;
    }

    public void setBluetooth_address(String address) {
        this.address = address;
    }
    public String getBluetooth_address() {
        return this.address;
    }
}
