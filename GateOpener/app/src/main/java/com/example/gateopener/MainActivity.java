package com.example.gateopener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.text.PrecomputedText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BLT_CONNECT = 100;
    private final int REQUEST_ENABLE_BLT_SCAN = 101;
    private final int REQUEST_ENABLE_BLT_ACCESS_COARSE_LOCATION = 102;

    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ArrayList<BluetoothDevice> listBlt = null;
    RfcommManager rfcommManager = new RfcommManager(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_addUser = (Button) this.findViewById(R.id.button_addUser);
        Button button_viewUsers = (Button) this.findViewById(R.id.button_viewUsers);
        Button button_test = (Button) this.findViewById(R.id.button_test);
        Button button_bluetooth = (Button) this.findViewById(R.id.button_bluetooth);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        //progressBar1.setVisibility(View.INVISIBLE);
        dialogUpBltOnStart();



        button_addUser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent addUserIntent = new Intent(MainActivity.this, Activity_addUser.class);
                MainActivity.this.startActivity(addUserIntent);
            }
        });

        button_viewUsers.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent viewUsersIntent = new Intent(MainActivity.this, Activity_viewUsers.class);
                MainActivity.this.startActivity(viewUsersIntent);
            }
        });

        button_test.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    //rfcommManager.echo();
                    //rfcommManager.synchronize();
            }
        });

        button_bluetooth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                requestPermissionsBltScan();
            }
        });

    }


    private void enableBLT() {

        //BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            dialogNoBlt();
        }
        assert mBluetoothAdapter != null;
        if (!mBluetoothAdapter.isEnabled()) {

            if (ContextCompat.checkSelfPermission(
                    MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED) {

                Intent enableBltIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBltIntent, REQUEST_ENABLE_BLT_CONNECT);
            } else {
                requestPermissionsBltConnect();
            }
        }
    }

    private void checkGpsStatus() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            dialogNoGps();
        }
    }

    private void dialogNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your gps seems to be disabled, do you wan't to enable it ?")
                .setCancelable(false)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("oui", "no gps");
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void requestPermissionsBltConnect() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_CONNECT)) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialogTitle1)
                    .setMessage(R.string.dialogMessage3)
                    .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BLT_CONNECT);
                            }
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            dialogCancelBlt();
                        }
                    })
                    .create().show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BLT_CONNECT);
            }
        }
    }

    private void requestPermissionsBltScan() {
        Log.d("oui", "requestPermissionsBltScan1");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_SCAN)) {
            Log.d("oui", "requestPermissionsBltScan2");


            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialogTitle1)
                    .setMessage(R.string.dialogMessage6)
                    .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("oui", "requestPermissionsBltScan3");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_ENABLE_BLT_SCAN);
                            }
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            dialogCancelBlt();
                        }
                    })
                    .create().show();
        } else {
            Log.d("oui", "requestPermissionsBltScan4");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_ENABLE_BLT_SCAN);
            }
        }
    }

    private void requestPermissionsBltCoarseLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialogTitle1)
                    .setMessage(R.string.dialogMessage6)
                    .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_BLT_ACCESS_COARSE_LOCATION);
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            dialogCancelBlt();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_BLT_ACCESS_COARSE_LOCATION);
        }
    }

    public void bluetoothScanning() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsBltConnect();
        } else {

            boolean notPaired = true;

            listBlt = getArrayOfAlreadyPairedBluetoothDevices();

            for (int i = 0; i < listBlt.size(); i++) {
                BluetoothDevice device = listBlt.get(i);
                if (device.getName().equals("GateOpener")) {
                    Log.d("oui", "already bonded, connecting");
                    //rfcommManager.setDevice(device);
                    //rfcommManager.connect2();

                    Intent intent = new Intent(this, Activity_bluetooth.class);
                    intent.putExtra("device", device);
                    this.startActivity(intent);


                    notPaired = false;
                    break;
                }
            }
            if (notPaired) {
                checkGpsStatus();
                scanForDevice();
            }


        }
    }

    public ArrayList getArrayOfAlreadyPairedBluetoothDevices() {
        ArrayList<BluetoothDevice> arrayOfAlreadyPairedBluetoothDevices = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsBltConnect();
            Log.d("oui", "4");
        } else {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                arrayOfAlreadyPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : pairedDevices) {
                    arrayOfAlreadyPairedBluetoothDevices.add(device);
                }
            }
        }


        return arrayOfAlreadyPairedBluetoothDevices;
    } //Return the list of the already bounded devices

    public void scanForDevice() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsBltConnect();
        } else {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
        }

    } //Launch the startDiscovery()

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionsBltConnect();
                    Log.d("oui", "2");
                } else {
                    listBlt.add(device);
                    String deviceAddress = device.getAddress();
                    Log.d("oui", deviceAddress);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("oui", "ended");
                printDevices();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("oui", "started");
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.d("oui", "bonding");
            }
        }
    };

    public void printDevices() {
        boolean GateOpenedFound = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsBltConnect();
        } else {
            if (listBlt != null) {
                Log.d("oui", String.valueOf(listBlt.size()));
            } else {
                Log.d("oui", "No devices scanned");
            }
            for (int i = 0; i < listBlt.size(); i++) {
                BluetoothDevice device = listBlt.get(i);
                String name = device.getName();
                if (name != null) {
                    Log.d("oui", name);

                    if (name.equals("GateOpener")) {
                        GateOpenedFound = true;
                        createBond(device);
                    }
                } else {
                    Log.d("oui", device.getAddress());
                }
            }
            Log.d("oui", "end list");
            if(!GateOpenedFound){
                dialogGateOpenerNotFound();
            }
        }
    } // Print in log the devices founded by the scanForDevices(), and launch the createBond() if the GateOpener is found

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("oui", "onRequestPermissionsResult1");
        if (requestCode == REQUEST_ENABLE_BLT_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.toast1, Toast.LENGTH_SHORT).show();
                enableBLT();
            } else {
                Toast.makeText(this, R.string.toast2, Toast.LENGTH_SHORT).show();
                dialogCancelBlt();
            }
        } else if (requestCode == REQUEST_ENABLE_BLT_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, R.string.toast1, Toast.LENGTH_SHORT).show();
                requestPermissionsBltCoarseLocation();
            } else {
                Toast.makeText(this, R.string.toast2, Toast.LENGTH_SHORT).show();
                dialogCancelBlt();
            }
        } else if (requestCode == REQUEST_ENABLE_BLT_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, R.string.toast1, Toast.LENGTH_SHORT).show();
                //scanForDevice();
                bluetoothScanning();
            } else {
                Toast.makeText(this, R.string.toast2, Toast.LENGTH_SHORT).show();
                dialogCancelBlt();
            }
        }
    }

    private void dialogUpBltOnStart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        if (!mBluetoothAdapter.isEnabled()) {
            builder.setTitle(R.string.dialogTitle2);
            builder.setMessage(R.string.dialogMessage4);

            builder.setPositiveButton(R.string.button_upBluetooth, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    enableBLT();
                }

            });
            builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    dialogCancelBlt();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    } // Ask to activate the bluetooth when the main activity launches

    private void dialogNoBlt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(R.string.dialogMessage2);

        builder.setPositiveButton(R.string.button_closeApp, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                System.exit(0);
            }

        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    } //Explain why the user should use the bluetooth

    private void dialogCancelBlt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(R.string.dialogTitle3);
        builder.setMessage(R.string.dialogMessage5);

        builder.setPositiveButton(R.string.button_understand, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }

        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogGateOpenerNotFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(R.string.dialogTitle4);
        builder.setMessage(R.string.dialogMessage7);

        builder.setPositiveButton(R.string.button_understand, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }

        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void createBond(BluetoothDevice device) {
        Log.d("oui", "go");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
        }
        mBluetoothAdapter.cancelDiscovery();
        Log.d("oui", "trying to pair with " + device.getName());
        device.createBond();
    } //Bond with the GateOpener device found by the scanForDevice() and the printDevices()

    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }
}