package com.example.tuf80213.bluetoothexample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class DiscoverActivity extends AppCompatActivity {

    ListView listView;
    BluetoothAdapter bta;
    ArrayList<BluetoothDevice> devices;
    ProgressBar progressBar;
    DeviceListAdapter adapter;
    boolean isBonding;

    private final int REQUEST_PERMISSION = 111;

    // Show progressbar when discovery begins
    BroadcastReceiver discoverStartedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.VISIBLE);
        }
    };


    // Update list when each new device is found
    BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                devices.add(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                adapter.notifyDataSetChanged();
            }
        }
    };


    // Disable progressbar when discovery ends if pairing not in progress
    // If no devices were found, exit activity
    BroadcastReceiver discoverEndedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isBonding)
                progressBar.setVisibility(View.INVISIBLE);
            if (devices.size() < 1) {
                Toast.makeText(DiscoverActivity.this, "No Devices Found", Toast.LENGTH_SHORT).show();
                DiscoverActivity.this.setResult(RESULT_CANCELED);
                DiscoverActivity.this.finish();
            }
        }
    };


    // Close activity when bonded to device
    BroadcastReceiver bondStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);

                if (state == BluetoothDevice.BOND_BONDED) {
                    progressBar.setVisibility(View.INVISIBLE);
                    DiscoverActivity.this.setResult(RESULT_OK);
                    DiscoverActivity.this.finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        bta = BluetoothAdapter.getDefaultAdapter();

        devices = new ArrayList<>();
        listView = findViewById(R.id.listView);
        adapter = new DeviceListAdapter(devices, this);
        progressBar = findViewById(R.id.progressBar);

        // Register receivers
        registerReceiver(discoverStartedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(discoverEndedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(bondStateChangedReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        if (bta != null) {
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {

                // Attempt to pair with device
                BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
                if (bta.isDiscovering())
                    bta.cancelDiscovery();
                if (device.createBond()) {
                    isBonding = true;
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

            // Begin discovery process, or request necessary permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                startDiscovery();
            else
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);


        } else {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void startDiscovery() {
        if (!bta.startDiscovery()) {
            Toast.makeText(this, "Unable to scan for Bluetooth devices", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startDiscovery();
            else {
                Toast.makeText(this, "You must grant location permission in order to pair devices", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(discoverStartedReceiver);
        unregisterReceiver(discoverEndedReceiver);
        unregisterReceiver(deviceFoundReceiver);
        unregisterReceiver(bondStateChangedReceiver);
    }
}
