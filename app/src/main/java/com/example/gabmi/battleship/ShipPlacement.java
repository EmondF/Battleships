package com.example.gabmi.battleship;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ShipPlacement extends AppCompatActivity  implements View.OnTouchListener {

    int[] shipsLengths = {2, 3, 3, 4, 5};

    public static String[] oppShipCoords;
    public static String[] myShipCoords;

    Destroyer myDestroyer;
    Submarine mySubmarine;
    Cruiser myCruiser;
    Battleship myBattleship;
    AircraftCarrier myAircraftCarrier;

    Navire SelectedShip;
    ShipGridView placementGridRef;
    int shipsPlaced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Tag", "ShipPlacement - onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_placement);
        placementGridRef = findViewById(R.id.myShipsView);
        placementGridRef.setOnTouchListener(this);

        myDestroyer = new Destroyer(R.id.Destroyer, findViewById(R.id.Destroyer));
        mySubmarine = new Submarine(R.id.Submarine, findViewById(R.id.Submarine));
        myCruiser = new Cruiser(R.id.Cruiser, findViewById(R.id.Cruiser));
        myBattleship = new Battleship(R.id.Battleship, findViewById(R.id.Battleship));
        myAircraftCarrier = new AircraftCarrier(R.id.AircraftCarrier, findViewById(R.id.AircraftCarrier));
        shipsPlaced = 0;

        Intent intent = getIntent();
        oppShipCoords = new String[5];
        myShipCoords = new String[5];

        placementGridRef.getViewTreeObserver().addOnPreDrawListener(gridDrawnListener);
    }

    private final ViewTreeObserver.OnPreDrawListener gridDrawnListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            placementGridRef.getViewTreeObserver().removeOnPreDrawListener(this);
            Log.i("Tag", "PreDrawListener  - Game - "+ placementGridRef.gridSize);
            placementGridRef.initGrid();
            return true;
        }
    };
    @Override
    protected void onStart() {
        Log.i("Tag", "ShipPlacement - onStart()");
        super.onStart();

        if(Connexion.solo == false)
        {
            if (Connexion.btOutputStream==null || Connexion.btInputStream==null || Connexion.btSocket==null) {
                Intent intent = new Intent(getApplicationContext(), Connexion.class);
                startActivity(intent);
            }
        }

    }

    @Override
    protected void onRestart() {
        Log.i("Tag", "ShipPlacement - onRestart()");
        super.onRestart();
        if (Connexion.btOutputStream==null || Connexion.btInputStream==null || Connexion.btSocket==null) {
            Intent intent = new Intent(getApplicationContext(), Connexion.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (SelectedShip != null) {
                //Place ship
                if (placementGridRef.testAndPlaceShip((int)(event.getX()/placementGridRef.gridFraction), (int)(event.getY()/placementGridRef.gridFraction), SelectedShip)) {
                    DeselectSelectedShip();
                    shipsPlaced++;
                    if (shipsPlaced == 5) {
                        findViewById(R.id.startGameLayout).setVisibility(View.VISIBLE);
                        findViewById(R.id.shipsLayout).setVisibility(View.GONE);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Le navire n'entre pas à l'endroit cliqué", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
            else {
                //Remove placed ship
                GridCell cell = placementGridRef.getCell(event.getX(), event.getY());
                if (cell.hasShip) {
                     placementGridRef.removeShip(cell);
                     shipsPlaced--;
                     if (shipsPlaced == 4) {
                         findViewById(R.id.startGameLayout).setVisibility(View.GONE);
                         findViewById(R.id.shipsLayout).setVisibility(View.VISIBLE);
                     }
                    return true;
                }
            }
        }
        return false;
    }

    //Sélectionner le bateau cliqué (Fct appelée par le XML)
    public void SelectShip(View view) {
        int deselectedShipID = DeselectSelectedShip();
        int viewID = view.getId();

        if(deselectedShipID != viewID) {
            //Si le bateau cliqué n'est pas celui qui etait déja sélectionné
            //On selectionne le bateau cliqué
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

            if (SelectedShip.getOrientationHorizontal()) {
                view.setBackgroundResource(R.drawable.destroyer_selected);
            }
            else {
                view.setBackgroundResource(R.drawable.destroyer_selected_vertical);
            }
            findViewById(R.id.rotateBtn).setVisibility(View.VISIBLE);
        }
    }

    //Déselectionner le bateau actuellement sélectionné
    public int DeselectSelectedShip() {
        if (SelectedShip != null) {
            int shipID = SelectedShip.getId();
            ImageButton shipImBtn = (ImageButton)SelectedShip.getView();
            if (SelectedShip.getOrientationHorizontal()) {
                shipImBtn.setBackgroundResource(R.drawable.destroyer);
            }
            else {
                shipImBtn.setBackgroundResource(R.drawable.destroyer_vertical);
            }
            findViewById(R.id.rotateBtn).setVisibility(View.INVISIBLE);
            SelectedShip = null;
            return shipID;
        }
        return -1;
    }

    //Place tous les bateaux aléatoirement sur la grille
    public void PlaceShipsRandom(View view) {

        Navire[] ships = new Navire[]{myAircraftCarrier, myBattleship, myCruiser, mySubmarine, myDestroyer};
        int cellX,cellY;
        boolean shipPlaced, orientation;
        Random rand = new Random();

        placementGridRef.ClearGrid();

        for (int i=0; i<5; i++) {
            shipPlaced = false;
            //Orientation random
            orientation = rand.nextBoolean();
            SetShipOrientation(orientation, ships[i]);

            //Cell random
            while(!shipPlaced) {
                cellX = rand.nextInt(10);
                cellY = rand.nextInt(10);
                shipPlaced = placementGridRef.testAndPlaceShip(cellX, cellY, ships[i]);
            }
        }
        shipsPlaced = 5;
        findViewById(R.id.startGameLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.shipsLayout).setVisibility(View.GONE);
        DeselectSelectedShip();
    }

    //Retirer tous les  bateaux de la grille
    public void ClearGrid(View view) {
        placementGridRef.ClearGrid();
        shipsPlaced = 0;
        findViewById(R.id.startGameLayout).setVisibility(View.GONE);
        findViewById(R.id.shipsLayout).setVisibility(View.VISIBLE);
    }

    //Changer l'orientation du bateau sélectionné
    public void ChangeShipOrientation(View view) {
        ImageButton shipImBtn = (ImageButton)SelectedShip.getView();
        ViewGroup.LayoutParams oldLayoutParams = shipImBtn.getLayoutParams();
        int tmpLayoutHeight = oldLayoutParams.height;
        int tmpLayoutWidth = oldLayoutParams.width;
        LinearLayout.LayoutParams newLayoutParams = new LinearLayout.LayoutParams(tmpLayoutHeight, tmpLayoutWidth);

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
        shipImBtn.setLayoutParams(newLayoutParams);
    }

    //Attribuer l'orientation en entrée au bateau donné aussi en entrée
    private void SetShipOrientation(boolean orientation, Navire ship) {
        if (ship.getOrientationHorizontal() != orientation) {
            ImageButton shipImBtn = (ImageButton)ship.getView();
            ViewGroup.LayoutParams oldLayoutParams = shipImBtn.getLayoutParams();
            int tmpLayoutHeight = oldLayoutParams.height;
            int tmpLayoutWidth = oldLayoutParams.width;
            LinearLayout.LayoutParams newLayoutParams = new LinearLayout.LayoutParams(tmpLayoutHeight, tmpLayoutWidth);

            ship.setOrientationHorizontal(orientation);
            shipImBtn.setLayoutParams(newLayoutParams);
            if (orientation) {
                //Set Horizontal
                shipImBtn.setBackgroundResource(R.drawable.destroyer);
            }
            else {
                shipImBtn.setBackgroundResource(R.drawable.destroyer_vertical);
            }

        }
    }

    //Échange les informations de grid avec l'adversaire et start la partie

    public void StartGame(View v) {

        //findViewById(R.id.waiting_opponent_tv).setVisibility(View.VISIBLE);

        /*Remplir les Strings de coordonnées des 5 bateaux placés,
         où chaque coordonnée = 2 caractères (Ex : Ligne 3, colonne 5 = "35")*/
        ArrayList<GridCell> shipList = placementGridRef.GetShipList();
        StringBuilder destroyerCoords = new StringBuilder();
        StringBuilder submarineCoords = new StringBuilder();
        StringBuilder cruiserCoords = new StringBuilder();
        StringBuilder battleshipCoords = new StringBuilder();
        StringBuilder aircraftCarrierCoords = new StringBuilder();

        for (GridCell cell : shipList) {
            switch (cell.navireRef.getNom()) {
                case "Destroyer":
                    destroyerCoords.append(cell.coordonnees[0]).append(cell.coordonnees[1]);
                    break;
                case "Submarine":
                    submarineCoords.append(cell.coordonnees[0]).append(cell.coordonnees[1]);
                    break;
                case "Cruiser":
                    cruiserCoords.append(cell.coordonnees[0]).append(cell.coordonnees[1]);
                    break;
                case "Battleship":
                    battleshipCoords.append(cell.coordonnees[0]).append(cell.coordonnees[1]);
                    break;
                case "Aircraft Carrier":
                    aircraftCarrierCoords.append(cell.coordonnees[0]).append(cell.coordonnees[1]);
                    break;
            }
        }
        myShipCoords[0] = destroyerCoords.toString();
        myShipCoords[1] = submarineCoords.toString();
        myShipCoords[2] = cruiserCoords.toString();
        myShipCoords[3] = battleshipCoords.toString();
        myShipCoords[4] = aircraftCarrierCoords.toString();

        if (Connexion.player1) {
            //Player 1 : Send -> Receive
            Log.i("Tag", "Player1");

            //Envoyer les coordonnées des bateaux placés a l'adversaire
            try {
                for (int i = 0; i < 5; i++) {
                    Log.i("Tag", "Before send "+i);
                    Connexion.btOutputStream.write(myShipCoords[i].getBytes());
                    Log.i("Tag", "After send "+i+" Sent : "+myShipCoords[i]);
                }
            } catch (IOException e) {
                Log.e("Tag", "btOutputStream's write() method failed", e);
            }

            //Recevoir le data des ships de l'adversaire
            try {
                for (int i = 0; i < 5; i++) {
                    Log.i("Tag", "Before recv "+i);
                    byte[] buffer = new byte[2*shipsLengths[i]];
                    Connexion.btInputStream.read(buffer);
                    String str = new String(buffer);
                    oppShipCoords[i] = str;
                    Log.i("Tag", "After recv "+i+" Received : "+oppShipCoords[i]);
                }
            } catch (IOException e) {
                Log.e("Tag", "btInputStream's read() method failed", e);
            }
        } else {
            //Player 2 : Receive -> Send
            Log.i("Tag", "Player2");
            //Recevoir le data des ships de l'adversaire
            try {
                for (int i = 0; i < 5; i++) {
                    Log.i("Tag", "Before recv "+i);
                    byte[] buffer = new byte[2*shipsLengths[i]];
                    Connexion.btInputStream.read(buffer);
                    String str = new String(buffer);
                    oppShipCoords[i] = str;
                    Log.i("Tag", "After recv "+i+" Received : "+oppShipCoords[i]);
                }
            } catch (IOException e) {
                Log.e("Tag", "btInputStream's read() method failed", e);
            }

            //Envoyer les coordonnées des bateaux placés a l'adversaire
            try {
                for (int i = 0; i < 5; i++) {
                    Log.i("Tag", "Before send "+i);
                    Connexion.btOutputStream.write(myShipCoords[i].getBytes());
                    Log.i("Tag", "After send "+i+" Sent : "+myShipCoords[i]);
                }
            } catch (IOException e) {
                Log.e("Tag", "btOutputStream's write() method failed", e);
            }
        }
        //findViewById(R.id.waiting_opponent_tv).setVisibility(View.INVISIBLE);

        Intent intent = new Intent(getApplicationContext(), Game.class);
        startActivity(intent);
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

