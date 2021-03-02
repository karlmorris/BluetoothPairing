package com.example.tuf80213.bluetoothexample;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {

    ArrayList<BluetoothDevice> devices;
    Context context;

    public DeviceListAdapter (ArrayList devices, Context context) {
        this.devices = devices;
        this.context = context;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView deviceName = new TextView(context);
        deviceName.setPadding(0,16,0,16);
        deviceName.setText(devices.get(position).getName() + " - "
        + devices.get(position).getAddress());
        return deviceName;
    }
}
