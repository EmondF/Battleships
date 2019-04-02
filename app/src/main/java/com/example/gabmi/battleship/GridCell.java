package com.example.gabmi.battleship;

public class GridCell {

    public boolean hasShip;
    public boolean hasBeenClicked;
    public float[] position;
    public int[] coordonnees;
    Navire navireRef;

    public GridCell(int coordX, int coordY) {
        hasShip = false;
        hasBeenClicked = false;
        position = new float[]{0, 0};
        coordonnees = new int[]{coordX, coordY};
        navireRef = null;
    }

    public GridCell(boolean _hasShip,
                    boolean _hasBeenClicked,
                    float positionX,
                    float positionY,
                    int coordX,
                    int coordY,
                    Navire _navireRef)
    {
        hasShip = _hasShip;
        hasBeenClicked = _hasBeenClicked;
        position = new float[]{positionX, positionY};
        coordonnees = new int[]{coordX, coordY};
        navireRef = _navireRef;
    }

}
