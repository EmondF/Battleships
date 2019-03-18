package com.example.gabmi.battleship;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class ShipPlacement extends AppCompatActivity  implements View.OnTouchListener {


    Destroyer destroyer;
    Navire SelectedShip;
    MyShipsView myShipsRef;
    boolean ShipPlaced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_placement);
        myShipsRef = findViewById(R.id.myShipsView);
        myShipsRef.setOnTouchListener(this);
        destroyer = new Destroyer();
        ShipPlaced = false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            myShipsRef.findCellPlacement(event.getX(), event.getY(), SelectedShip);
            //myShipsRef.findCell(event.getX(), event.getY());
        }
        return false;
    }

    public void SelectDestroyer(View view)
    {

        SelectedShip = destroyer;
        SelectedShip.setId(view.getId());
        SelectedShip.setView(view);
    }
}
