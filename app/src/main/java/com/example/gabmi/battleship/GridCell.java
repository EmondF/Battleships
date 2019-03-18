package com.example.gabmi.battleship;

public class GridCell {

    public boolean hasShip;
    public boolean hasBeenClicked;
    public int[] position;
    Navire navireRef;

    public GridCell() {
        hasShip = false;
        position = new int[]{0, 0};
        navireRef = null;
        hasBeenClicked = false;
    }

}
