package com.example.gabmi.battleship;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
import java.util.Set;
import java.util.UUID;

public class Connexion extends AppCompatActivity {

    public static UUID uuid;
    public static boolean player1;

    public static boolean solo;

    private Button getPairedBtn;
    private Button soloButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);
        Log.i("Tag", "Connexion - onCreate()");

        solo = false;
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
        soloButton = (Button)findViewById(R.id.button_solo);
        soloButton.setOnClickListener(SoloPlay);

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
    public void onRestart() {
        super.onRestart();

        try {
            btInputStream.close();
        } catch (IOException e) {
            Log.e("Tag", "InputStream's close() method failed");
        } catch (NullPointerException e) {
            Log.e("Tag", "tried to access InputStream while it was null");
        }

        try {
            btOutputStream.close();
        } catch (IOException e) {
            Log.e("Tag", "OutputStream's close() method failed");
        } catch (NullPointerException e) {
            Log.e("Tag", "tried to access OutputStream while it was null");
        }
        try {
            btSocket.close();
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
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("Tag", "Connexion - onStart()");

        registerReceiver(mReceiverActionFound, filter_found);
        registerReceiver(mReceiverActionDiscoveryFinished, filter_discoveryFinished);

        GetPairedDevices(); //Updates known devices spinner

        //Starts listen thread
        listenThread = new Thread(listenThreadAction);
        listenThread.start();

        otherSpinner.setVisibility(View.INVISIBLE);
        connectOtherBtn.setVisibility(View.INVISIBLE);
        informationsTextView.setVisibility(View.INVISIBLE);

    }

    private Runnable listenThreadAction = new Runnable() {
        @Override
        public void run()  {
            btSocket = null;
            try {
                btServerSocket = myBtAdapter.listenUsingInsecureRfcommWithServiceRecord("BattleShip", uuid);
            } catch (IOException e) {
                Log.e("Tag", "Socket's listen() method failed");
            }

            boolean connected = false;
            while (!connected) {
                try {
                    Log.i("Tag", "Listening");
                    btSocket = btServerSocket.accept(); //Blocking method
                } catch (IOException e) {
                    Log.e("Tag", "Socket's accept() method failed");
                    break;
                }
                if (btSocket!= null) {
                    Log.i("Tag", " Connexion Accepted");
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
                    //Start activity "Ship placement" as player 2
                    Log.i("Tag", " Other activity started 2");
                    registerReceiver(mReceiverActionAclDisconnected, filter_disconnected);
                    player1 = false;
                    Intent intent = new Intent(getApplicationContext(), ShipPlacement.class);
                    startActivity(intent);
                }
            }
            Log.i("Tag", "Thread ended");
        }
    };

    private View.OnClickListener getPairedBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GetPairedDevices();
        }
    };

    private View.OnClickListener SoloPlay = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ShipPlacement.class);
            startActivity(intent);
            solo = true;
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

                Log.i("Tag", " Other activity started 1");
                //Starts activity "Ship placement" as player 1
                registerReceiver(mReceiverActionAclDisconnected, filter_disconnected);
                player1 = true;
                Intent intent = new Intent(getApplicationContext(), ShipPlacement.class);
                startActivity(intent);
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
                Log.i("Tag", "DeviceFound");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                otherDevices.add(device);
                if (!Contains(otherAdapter, device.getName())) {
                    otherAdapter.add(device.getName());
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
                }
                Toast.makeText(Connexion.this, R.string.connexion_with_host_ended, Toast.LENGTH_LONG).show();
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
                if (theAdapter.getItem(i).equals(element)) {
                    return true;
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
