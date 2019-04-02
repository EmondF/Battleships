package com.example.gabmi.battleship;

import android.view.View;

public class AircraftCarrier extends Navire {

    public AircraftCarrier(int id, View view){
        super("Aircraft Carrier", 5, id, view);
    }
    @Override
    public String toString(){
        return "";
    }
}
