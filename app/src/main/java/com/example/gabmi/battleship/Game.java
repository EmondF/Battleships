package com.example.gabmi.battleship;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.io.IOException;

public class Game extends AppCompatActivity implements View.OnTouchListener {

    ShipGridView oppGridRef;
    ShipGridView myGridRef;

    String[] oppShipCoords;
    String[] myShipCoords;

    TextView tv_turn_info;

    Thread receiveThread;

    boolean gameEnded;
    boolean myTurn;
    int myRemainingShipCells;
    int oppRemainingShipCells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Tag", "Debut onCreate - Game");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.i("Tag", "Milieu onCreate - Game");
        oppGridRef = findViewById(R.id.oppGrid);
        oppGridRef.setOnTouchListener(this);
        myGridRef = findViewById(R.id.myGrid);
        tv_turn_info = findViewById(R.id.tv_turn_info);

        oppShipCoords = ShipPlacement.oppShipCoords;
        myShipCoords = ShipPlacement.myShipCoords;

        myGridRef.getViewTreeObserver().addOnPreDrawListener(myGridDrawnListener);
        oppGridRef.getViewTreeObserver().addOnPreDrawListener(oppGridDrawnListener);

        gameEnded = false;
        myRemainingShipCells = 17;
        oppRemainingShipCells = 17;
        //Starts listen thread
        receiveThread = new Thread(receiveThreadAction);
        receiveThread.start();

        if (!Connexion.player1) {
            //L'adversaire commence
            tv_turn_info.setText(R.string.opp_turn);
            tv_turn_info.setTextColor(getResources().getColor(R.color.Red));
            findViewById(R.id.surrender_btn).setVisibility(View.INVISIBLE);
            myTurn = false;
        }
        else {
            tv_turn_info.setText(R.string.your_turn);
            tv_turn_info.setTextColor(getResources().getColor(R.color.Green));
            myTurn = true;
        }
        Log.i("Tag", "Fin onCreate - Game");
    }

    private final ViewTreeObserver.OnPreDrawListener myGridDrawnListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            myGridRef.getViewTreeObserver().removeOnPreDrawListener(this);
            Log.i("Tag", "PreDrawListener  - Game - myGrid" + myGridRef.gridSize);
            myGridRef.initGrid();
            myGridRef.initGridWithShips(myShipCoords, true);
            return true;
        }
    };

    private final ViewTreeObserver.OnPreDrawListener oppGridDrawnListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            oppGridRef.getViewTreeObserver().removeOnPreDrawListener(this);
            Log.i("Tag", "PreDrawListener  - Game - oppGrid" + oppGridRef.gridSize);
            oppGridRef.initGrid();
            oppGridRef.initGridWithShips(oppShipCoords, false);
            return true;
        }
    };

    private Runnable receiveThreadAction = new Runnable() {
        @Override
        public void run() {
            while (!gameEnded) {
                byte[] buffer = new byte[2];
                try {
                    //Attend de recevoir les coordonnés de la cellule attaquée
                    Log.i("Tag", "Waiting for attack");
                    Connexion.btInputStream.read(buffer);
                } catch (IOException e) {
                    Log.e("Tag", "Failed to read buffer");
                    gameEnded = true;
                    Intent intent = new Intent(Game.this, Connexion.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } catch (NullPointerException e) {
                    Log.e("Tag", "btInputStream is null");
                    gameEnded = true;
                    Intent intent = new Intent(Game.this, Connexion.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                Log.i("Tag", "Received an attack");
                final String coordinates = new String(buffer);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (coordinates.equals("ff")) {
                            GameEnd(true);
                        }
                        else {
                            int cellX = Character.getNumericValue(coordinates.charAt(0));
                            int cellY = Character.getNumericValue(coordinates.charAt(1));
                            if (cellX != -1 && cellY != -1)
                                myRemainingShipCells = 17 - myGridRef.AttackCell(cellX, cellY);
                            tv_turn_info.setText(R.string.your_turn);
                            tv_turn_info.setTextColor(getResources().getColor(R.color.Green));
                            findViewById(R.id.surrender_btn).setVisibility(View.VISIBLE);
                            myTurn = true;
                            if (myRemainingShipCells == 0) {
                                GameEnd(false);
                            }
                        }
                    }
                });
            }
            Log.i("Tag", "Thread ended");
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (myTurn) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                GridCell cellClicked = oppGridRef.getCell(event.getX(), event.getY());
                if (!cellClicked.hasBeenClicked) {
                    //La cell n'a pas encore été cliquée
                    int cellX = cellClicked.coordonnees[0];
                    int cellY = cellClicked.coordonnees[1];
                    String coordinates = "" + cellX + cellY;
                    try {
                        Connexion.btOutputStream.write(coordinates.getBytes());
                    } catch (IOException e) {
                        Log.e("Tag", "Failed to write to btOutputStream");
                        gameEnded = true;
                        Intent intent = new Intent(this, Connexion.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                    oppRemainingShipCells = 17 - oppGridRef.AttackCell(cellX, cellY);
                    //Redonne le tour a ladversaire
                    tv_turn_info.setText(R.string.opp_turn);
                    tv_turn_info.setTextColor(getResources().getColor(R.color.Red));
                    findViewById(R.id.surrender_btn).setVisibility(View.INVISIBLE);
                    myTurn = false;
                    if (oppRemainingShipCells == 0) {
                        //Victory
                        GameEnd(true);
                    }
                    return true;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }

    public void Surrender(View view) {
        gameEnded = true;
        try {
            Connexion.btOutputStream.write("ff".getBytes());
        } catch (IOException e) {
            Log.e("Tag", "Failed to write to btOutputStream");
            Intent intent = new Intent(this, Connexion.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        Intent intent = new Intent(this, Connexion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void GameEnd(boolean victorious) {
        gameEnded = true;
        myTurn = false;
        findViewById(R.id.surrender_btn).setVisibility(View.GONE);
        findViewById(R.id.gameEndLayout).setVisibility(View.VISIBLE);
        if (victorious) {
            ((TextView)findViewById(R.id.win_lose_tv)).setText(R.string.victory);
        }
        else {
            oppGridRef.RevealGrid();
            ((TextView)findViewById(R.id.win_lose_tv)).setText(R.string.defeat);
        }
    }

    public void GoBackToMenu(View view) {
        Intent intent = new Intent(this, Connexion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Connexion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Tag", "Game - onStop()");
        gameEnded = true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Tag", "Game - onDestroy()");
        gameEnded = true;
    }
}
