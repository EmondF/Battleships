package com.example.gabmi.battleship;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Connexion extends AppCompatActivity {

    public static UUID uuid;
    public static boolean player1;

    private Button getPairedBtn;
    private Button connectPairedBtn;
    private Spinner pairedSpinner;
    private Button searchOtherBtn;
    private Button connectOtherBtn;
    private Spinner otherSpinner;
    private TextView informationsTextView;
    private Button makeDiscoverableBtn;

    public Set<BluetoothDevice> pairedDevices;
    public Set<BluetoothDevice> otherDevices;
    public ArrayAdapter<String> pairedAdapter = null;
    public ArrayAdapter<String> otherAdapter = null;

    public static BluetoothSocket btSocket = null;
    public static BluetoothServerSocket btServerSocket = null;
    public static BluetoothAdapter myBtAdapter = null;
    public static OutputStream btOutputStream;
    public static InputStream btInputStream;

    public IntentFilter filter_found;
    public IntentFilter filter_discoveryFinished;
    public IntentFilter filter_disconnected;

    private Thread listenThread;
    private Handler mHandler;

    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);
        Log.i("Tag", "Connexion - onCreate()");

        pairedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, R.id.tv_spinner);
        pairedAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        otherAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, R.id.tv_spinner);
        otherAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        pairedDevices = new HashSet<BluetoothDevice>();
        otherDevices = new HashSet<BluetoothDevice>();

        //Bluetooth
        myBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBtAdapter == null) {
            //Bluetooth not supported by device
            finish();
        }
        else if (!myBtAdapter.isEnabled()) {
            //Bluetooth is disabled
            myBtAdapter.enable();
        }

        uuid = UUID.fromString("68436eb5-faf2-461e-b30e-fb37f5664835"); //random

        filter_found = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter_discoveryFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter_disconnected = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        //Widgets
        getPairedBtn = (Button)findViewById(R.id.button_get_paired);
        getPairedBtn.setOnClickListener(getPairedBtnListener);

        searchOtherBtn = (Button)findViewById(R.id.button_search_other);
        searchOtherBtn.setOnClickListener(searchOtherBtnListener);

        pairedSpinner = (Spinner)findViewById(R.id.spinner_paired);
        pairedSpinner.setAdapter(pairedAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            pairedSpinner.setDropDownVerticalOffset(30);
        }

        otherSpinner = (Spinner)findViewById(R.id.spinner_other);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            otherSpinner.setDropDownVerticalOffset(30);
        }
        otherSpinner.setAdapter(otherAdapter);

        connectPairedBtn = (Button)findViewById(R.id.button_connectPaired);
        connectPairedBtn.setOnClickListener(connectBtnListener);

        connectOtherBtn = (Button)findViewById(R.id.button_connectOther);
        connectOtherBtn.setOnClickListener(connectBtnListener);

        informationsTextView = (TextView)findViewById(R.id.tv_lookingNearbyDevices);

        makeDiscoverableBtn = (Button) findViewById(R.id.button_make_discoverable);
        makeDiscoverableBtn.setOnClickListener(makeDiscoverableBtnListener);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("Tag", "Connexion - onStart()");

        registerReceiver(mReceiverActionFound, filter_found);
        registerReceiver(mReceiverActionDiscoveryFinished, filter_discoveryFinished);

        GetPairedDevices(); //Updates known devices spinner

        try {
            btInputStream.close();
            btInputStream = null;
        } catch (IOException e) {
            Log.e("Tag", "InputStream's close() method failed");
        } catch (NullPointerException e) {
            Log.e("Tag", "tried to access InputStream while it was null");
        }

        try {
            btOutputStream.close();
            btOutputStream = null;
        } catch (IOException e) {
            Log.e("Tag", "OutputStream's close() method failed");
        } catch (NullPointerException e) {
            Log.e("Tag", "tried to access OutputStream while it was null");
        }
        try {
            btSocket.close();
            btSocket = null;
        } catch (IOException e) {
            Log.e("Tag", "Socket's close() method failed");
        } catch (NullPointerException e) {
            Log.e("Tag", "tried to access btSocket while it was null");
        }

        try {
            unregisterReceiver(mReceiverActionAclDisconnected);
        } catch(IllegalArgumentException e) {
            Log.e("Tag", "Unregistering receivermReceiverActionAclDisconnected failed");
        }

        //Starts listen thread
        mHandler = new Handler();
        connected = false;

        listenThread = new Thread(listenThreadAction);
        listenThread.start();

        otherSpinner.setVisibility(View.INVISIBLE);
        connectOtherBtn.setVisibility(View.INVISIBLE);
        informationsTextView.setVisibility(View.INVISIBLE);


    }

    private Runnable listenThreadAction = new Runnable() {
        @Override
        public void run()  {
            //Reinitialisation des streams et du socket
            try {
                btInputStream.close();
                btInputStream = null;
            } catch (IOException e) {
                Log.e("Tag", "InputStream's close() method failed");
            } catch (NullPointerException e) {
                Log.e("Tag", "tried to access InputStream while it was null");
            }

            try {
                btOutputStream.close();
                btOutputStream = null;
            } catch (IOException e) {
                Log.e("Tag", "OutputStream's close() method failed");
            } catch (NullPointerException e) {
                Log.e("Tag", "tried to access OutputStream while it was null");
            }

            try {
                btSocket.close();
                btSocket = null;
            } catch (IOException e) {
                Log.e("Tag", "Socket's close() method failed");
            } catch (NullPointerException e) {
                Log.e("Tag", "tried to access btSocket while it was null");
            }

            //Listen
            try {
                btServerSocket = myBtAdapter.listenUsingInsecureRfcommWithServiceRecord("BattleShip", uuid);
            } catch (IOException e) {
                Log.e("Tag", "Socket's listen() method failed");
            }

            connected = false;
            while (!connected) {
                try {
                    Log.i("Tag", "Listening");
                    btSocket = btServerSocket.accept(); //Blocking method
                } catch (IOException e) {
                    Log.e("Tag", "Socket's accept() method failed");
                    break;
                }
                if (btSocket != null) {
                    try {
                        btServerSocket.close();
                    } catch (IOException e) {
                        Log.e("Tag", "Socket's close() method failed");
                    }
                    try {
                        btOutputStream = btSocket.getOutputStream();
                    } catch (IOException e) {
                        Log.e("Tag", "socket's getOutputStream() method failed");
                    }
                    try {
                        btInputStream = btSocket.getInputStream();
                    } catch (IOException e) {
                        Log.e("Tag", "socket's getInputStream() method failed");
                    }
                    connected = true;


                    if(!Connexion.this.isFinishing())
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alertdialog = new AlertDialog.Builder(Connexion.this)
                                        .setTitle("Un adversaire vous défie !")
                                        .setMessage(btSocket.getRemoteDevice().getName() + " tente de se connecter.\nAcceptez-vous ?")
                                        .setIcon(R.drawable.app_logo)
                                        .setPositiveButton(R.string.Yes,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        try {
                                                            btOutputStream.write("y".getBytes());
                                                            //Start activity "Ship placement" as player 2
                                                            Log.i("Tag", " Other activity started 2");
                                                            registerReceiver(mReceiverActionAclDisconnected, filter_disconnected);
                                                            player1 = false;
                                                            Intent intent = new Intent(getApplicationContext(), ShipPlacement.class);
                                                            startActivity(intent);

                                                        }
                                                        catch (IOException e) {
                                                            Log.e("Tag", "Writing failed");
                                                        }
                                                    }
                                                })
                                        .setNegativeButton(R.string.No,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        try {
                                                            btOutputStream.write("n".getBytes());
                                                            listenThread = new Thread(listenThreadAction);
                                                            listenThread.start();
                                                        }
                                                        catch (IOException e) {
                                                            Log.e("Tag", "Writing failed");
                                                        }
                                                    }
                                                })

                                        .show();
                                alertdialog.setCanceledOnTouchOutside(false);
                            }
                        });

                    }

                   }
            }
            Log.i("Tag", "Listening Thread ended");
        }
    };

    private View.OnClickListener getPairedBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GetPairedDevices();
        }
    };

    private View.OnClickListener searchOtherBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SearchOtherDevices();
        }
    };

    private View.OnClickListener connectBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean success = false;
            if (v.getId() == R.id.button_connectPaired) {
                success =  Connect(true);
            }
            else if (v.getId() == R.id.button_connectOther) {
                success =  Connect(false);
            }
            if (success) {
                if (btServerSocket != null) {
                    try {btServerSocket.close();} catch (IOException e) { e.printStackTrace();}
                }
                try {
                    btOutputStream = btSocket.getOutputStream();
                } catch (IOException e) {
                    Log.e("Tag", "socket's getOutputStream() method failed");
                }
                try {
                    btInputStream = btSocket.getInputStream();
                } catch (IOException e) {
                    Log.e("Tag", "socket's getInputStream() method failed");
                }

                byte [] buffer = new byte[1];
                try {
                    btInputStream.read(buffer);
                } catch (IOException e) {
                    Log.e("Tag", "Reading failed");
                    finish();
                }
                String b = new String(buffer);

                if (b.equals("y")) {
                    //Starts activity "Ship placement" as player 1
                    Log.i("Tag", " Other activity started 1");
                    registerReceiver(mReceiverActionAclDisconnected, filter_disconnected);
                    player1 = true;
                    Intent intent = new Intent(getApplicationContext(), ShipPlacement.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.connexion_denied), Toast.LENGTH_LONG).show();
                    listenThread = new Thread(listenThreadAction);
                    listenThread.start();
                }
            }
            else {
                btSocket = null;
                Toast.makeText(getApplicationContext(), getString(R.string.connexion_failed), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener makeDiscoverableBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            makeDiscoverable();
        }
    };



    // Create a BroadcastReceiver for Bluetooth.ACTION_FOUND
    private final BroadcastReceiver mReceiverActionFound = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Device Found
                Log.i("Tag", "Device Found");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                otherDevices.add(device);
                String name = device.getName();
                if (name == null) {
                    name = device.getAddress();
                }
                if (!Contains(otherAdapter, name)) {
                    otherAdapter.add(name);
                }
            }
        }
    };

    // Create a BroadcastReceiver for Bluetooth.ACTION_DISCOVERY_FINISHED
    private final BroadcastReceiver mReceiverActionDiscoveryFinished = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("Tag", "DiscoveryFinished");
                //Discovery is finished
                informationsTextView.setVisibility(View.INVISIBLE);
                myBtAdapter.cancelDiscovery();
                Toast.makeText(getApplicationContext(), getString(R.string.device_discovery_finished), Toast.LENGTH_LONG).show();
                unregisterReceiver(mReceiverActionDiscoveryFinished);
            }
        }
    };

    // Create a BroadcastReceiver for Bluetooth.ACTION_ACL_DISCONNECTED
    private final BroadcastReceiver mReceiverActionAclDisconnected = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device disconnected
                Log.i("Tag", "Disconnected");
                //Retour a la page de connexion
                if (getApplicationContext() != Connexion.this) {
                    Intent mintent = new Intent(getApplicationContext(), Connexion.class);
                    startActivity(mintent);
                    finish();
                }
                Toast.makeText(Connexion.this, R.string.connexion_with_host_ended, Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void GetPairedDevices() {
        if (myBtAdapter == null) {
            // Device doesn't support Bluetooth
        }
        else {
            pairedDevices = myBtAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                pairedAdapter.clear();
                for (BluetoothDevice device : pairedDevices) {
                    pairedAdapter.add(device.getName());
                }
            }
        }
    }

    private void SearchOtherDevices() {
        if (myBtAdapter.isDiscovering()) {
            myBtAdapter.cancelDiscovery();
        }
        Log.i("Tag", "Discovery started");
        myBtAdapter.startDiscovery();
        registerReceiver(mReceiverActionDiscoveryFinished, filter_discoveryFinished);
        otherSpinner.setVisibility(View.VISIBLE);
        connectOtherBtn.setVisibility(View.VISIBLE);
        informationsTextView.setVisibility(View.VISIBLE);
        informationsTextView.setText(getString(R.string.looking_for_nearby_devices));
    }

    private boolean Connect(boolean boundedPressed) {

        BluetoothDevice device = GetSelectedSpinnerItem(boundedPressed);

        if (device == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_device_selected), Toast.LENGTH_SHORT).show();
            return false;
        }
        else {

            if (btSocket == null) {
                try {
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                } catch (IOException e) {
                    Log.e("Tag", "Socket's create() method failed");
                }
                try {
                    btSocket.connect();
                } catch (IOException e) {
                    Log.e("Tag", "Socket's connect() method failed");
                }
                return btSocket.isConnected();
            }
            return false;
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private BluetoothDevice GetSelectedSpinnerItem(boolean bounded) {
        if (bounded) {
            for (BluetoothDevice device : pairedDevices) {
                if(device.getName().equals(pairedSpinner.getSelectedItem().toString())) {
                    return device;
                }
            }
            //No device selected
            return null;
        }
        else {
            for (BluetoothDevice device : otherDevices) {
                if(device.getName().equals(otherSpinner.getSelectedItem().toString())) {
                    return device;
                }
            }
        }
        return null;
    }

    private boolean Contains(ArrayAdapter<String> theAdapter, String element) {
        if (!theAdapter.isEmpty()) {
            for (int i = 0; i < theAdapter.getCount(); i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (Objects.requireNonNull(theAdapter.getItem(i)).equals(element)) {
                        return true;
                    }
                }
                else {
                    if (theAdapter.getItem(i).equals(element)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Tag", "Connexion - onStop()");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Tag", "Connexion - onDestroy()");
        try {
            unregisterReceiver(mReceiverActionFound);
        } catch(IllegalArgumentException e) {
            Log.e("Tag", "Unregistering receiver mReceiverActionFound failed");
        }

        try {
            unregisterReceiver(mReceiverActionDiscoveryFinished);
        } catch(IllegalArgumentException e) {
            Log.e("Tag", "Unregistering receiver mReceiverActionDiscoveryFinished failed");
        }

        try {
            unregisterReceiver(mReceiverActionAclDisconnected);
        } catch(IllegalArgumentException e) {
            Log.e("Tag", "Unregistering receivermReceiverActionAclDisconnected failed");
        }
    }
}
