package com.example.gabmi.battleship;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.IOException;

public class ShipPlacement extends AppCompatActivity  implements View.OnTouchListener {


    Destroyer myDestroyer;
    Submarine mySubmarine;
    Cruiser myCruiser;
    Battleship myBattleship;
    AircraftCarrier myAircraftCarrier;

    Navire SelectedShip;
    MyShipsView myShipsRef;
    boolean ShipPlaced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Tag", "ShipPlacement - onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_placement);
        myShipsRef = findViewById(R.id.myShipsView);
        myShipsRef.setOnTouchListener(this);


        myDestroyer = new Destroyer(R.id.Destroyer);
        mySubmarine = new Submarine(R.id.Submarine);
        myCruiser = new Cruiser(R.id.Cruiser);
        myBattleship = new Battleship(R.id.Battleship);
        myAircraftCarrier = new AircraftCarrier(R.id.AircraftCarrier);

        ShipPlaced = false;
    }

    @Override
    protected void onStart() {
        Log.i("Tag", "ShipPlacement - onStart()");
        super.onStart();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (SelectedShip != null) {
                myShipsRef.findCellPlacement(event.getX(), event.getY(), SelectedShip);
                DeselectSelectedShip();
            }

        }
        return false;
    }

    // Create a BroadcastReceiver for Bluetooth.ACTION_ACL_DISCONNECTED
    private final BroadcastReceiver mReceiverActionAclDisconnected = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device disconnected
                Log.i("Tag", "Disconnected");
                if (Connexion.btSocket != null) {
                    try {
                        Connexion.btSocket.close();
                        Connexion.btSocket = null;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Retour a la page de connexion
                ChangeView(Connexion.class);
            }
        }
    };

    private void ChangeView(Class activity) {
        Intent intent = new Intent(getApplicationContext(), activity);
        startActivity(intent);
    }

    public void SelectShip(View view) {
        DeselectSelectedShip();

        switch (view.getId()) {
            case R.id.Destroyer:
                SelectedShip = myDestroyer;
                break;
            case R.id.Submarine:
                SelectedShip = mySubmarine;
                break;
            case R.id.Cruiser:
                SelectedShip = myCruiser;
                break;
            case R.id.Battleship:
                SelectedShip = myBattleship;
                break;
            case R.id.AircraftCarrier:
                SelectedShip = myAircraftCarrier;
                break;
        }
        if (SelectedShip.getView() == null) {
            SelectedShip.setView(view);
        }
        if (SelectedShip.getOrientationHorizontal()) {
            view.setBackgroundResource(R.drawable.destroyer_selected);
        }
        else {
            view.setBackgroundResource(R.drawable.destroyer_selected_vertical);
        }
        findViewById(R.id.rotateBtn).setVisibility(View.VISIBLE);
    }

    public void DeselectSelectedShip() {
        if (SelectedShip != null) {
            ImageButton shipImBtn = (ImageButton)SelectedShip.getView();
            if (SelectedShip.getOrientationHorizontal()) {
                shipImBtn.setBackgroundResource(R.drawable.destroyer);
            }
            else {
                shipImBtn.setBackgroundResource(R.drawable.destroyer_vertical);
            }
            findViewById(R.id.rotateBtn).setVisibility(View.INVISIBLE);
            SelectedShip = null;
        }
    }

    public void ChangeShipOrientation(View view) {
        ImageButton shipImBtn = (ImageButton)SelectedShip.getView();
        ViewGroup.LayoutParams oldLayoutParams = shipImBtn.getLayoutParams();
        int tmpLayoutHeight = oldLayoutParams.height;
        int tmpLayoutWidth = oldLayoutParams.width;

        if (SelectedShip.getOrientationHorizontal()) {
            //Le bateau est actuellement horizontal

            //Met le bateau a la verticale
            SelectedShip.setOrientationHorizontal(false);
            shipImBtn.setBackgroundResource(R.drawable.destroyer_selected_vertical);

        }
        else {
            //Le bateau est actuellement vertical

            //Met le bateau a l'hotizontal
            SelectedShip.setOrientationHorizontal(true);
            shipImBtn.setBackgroundResource(R.drawable.destroyer_selected);
        }
        //Swap sa hauteur et sa largeur pour que la nouvelle image apparaisse correctement(rotate)
        LinearLayout.LayoutParams newLayoutParams = new LinearLayout.LayoutParams(tmpLayoutHeight, tmpLayoutWidth);
        shipImBtn.setLayoutParams(newLayoutParams);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Tag", "ShipPlacement - onStop()");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Tag", "ShipPlacement - onDestroy()");
    }
}

