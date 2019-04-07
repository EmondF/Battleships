package com.example.gabmi.battleship;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Iterator;

public class ShipGridView extends View {

    String[] shipsNames = {"Destroyer", "Submarine", "Cruiser", "Battleship", "Aircraft Carrier"};
    int[] shipsLengths = {2, 3, 3, 4, 5};

    private GridCell[][] bsGrid;
    public int gridSize;
    public float gridFraction;
    private ArrayList<GridCell> noHit;
    private ArrayList<GridCell> hit;
    private ArrayList<GridCell> Ships;
    private ArrayList<GridCell> destroyedShips;
    private Paint noHitPaint, hitPaint, shipPaint, destroyedShipPaint;

    public ShipGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("Tag", "MyShipsView - Constructeur - Debut");
        bsGrid = new GridCell[10][10];

        noHit = new ArrayList<>();
        hit = new ArrayList<>();
        Ships = new ArrayList<>();
        destroyedShips = new ArrayList<>();
        noHitPaint = new Paint();
        hitPaint = new Paint();
        shipPaint = new Paint();
        destroyedShipPaint = new Paint();

        noHitPaint.setColor(Color.argb(100, 255, 255, 255));    //Blanc
        hitPaint.setColor(Color.argb(255, 255, 0, 0));          //Rouge
        shipPaint.setColor(Color.argb(255, 0, 0 , 0));          //Noir
        destroyedShipPaint.setColor(Color.argb(255, 0, 255, 0));
        Log.i("Tag", "MyShipsView - Constructeur - Fin");
    }
//Fonctions générales pour la View (la grid)
    //Initier la grid avec des GridCells vides
    public void initGrid() {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                bsGrid[i][j] = new GridCell(i, j, i*gridFraction, j*gridFraction);
            }
        }
    }

    //Methode pour que la grille soit toujours de dimension carre
    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        Log.i("Tag", "MyShipsView - onMeasure()");
        gridSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        gridFraction = (float)gridSize/10;
        setMeasuredDimension(gridSize, gridSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCanvas(canvas);
    }

    //Redessine tous les ships actuels dans le tableau "Ships"
    private void drawCanvas(Canvas canvas){
        for (int i = 0; i < Ships.size(); i++) {
            Rect rectangle = new Rect(
                    Math.round(Ships.get(i).position[0]),
                    Math.round(Ships.get(i).position[1]),
                    Math.round(Ships.get(i).position[0] + gridFraction),
                    Math.round(Ships.get(i).position[1] + gridFraction));

            canvas.drawRect(rectangle, shipPaint);
        }
        for (int i = 0; i < noHit.size(); i++) {
            Rect rectangle = new Rect(
                    Math.round(noHit.get(i).position[0]),
                    Math.round(noHit.get(i).position[1]),
                    Math.round(noHit.get(i).position[0] + gridFraction),
                    Math.round(noHit.get(i).position[1] + gridFraction));

            canvas.drawRect(rectangle, noHitPaint);
        }
        for (int i = 0; i < hit.size(); i++) {
            Rect rectangle = new Rect(
                    Math.round(hit.get(i).position[0]),
                    Math.round(hit.get(i).position[1]),
                    Math.round(hit.get(i).position[0] + gridFraction),
                    Math.round(hit.get(i).position[1] + gridFraction));

            canvas.drawRect(rectangle, hitPaint);
        }
        for (int i = 0; i < destroyedShips.size(); i++) {
            Rect rectangle = new Rect(
                    Math.round(destroyedShips.get(i).position[0]),
                    Math.round(destroyedShips.get(i).position[1]),
                    Math.round(destroyedShips.get(i).position[0] + gridFraction),
                    Math.round(destroyedShips.get(i).position[1] + gridFraction));

            canvas.drawRect(rectangle, destroyedShipPaint);
        }

    }

    public ArrayList<GridCell> GetShipList() {
        return Ships;
    }

    //Retourne l'objet GridCell correspondant à la position en pixel (X,Y) en entrée
    public GridCell getCell(float posX, float posY) {
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        return bsGrid[cellX][cellY];
    }


//Fonctions pour le ShipPlacement
    //Remove tout le ship de la GridCell cliquée, donnée en entrée
    public void removeShip(GridCell cellClicked) {
        int cellX = cellClicked.coordonnees[0];
        int cellY = cellClicked.coordonnees[1];
        Navire navireClicked = cellClicked.navireRef;
        String nom = navireClicked.getNom();

        navireClicked.getView().setVisibility(View.VISIBLE);
        if (navireClicked.getOrientationHorizontal()) {
            for (int i=0; i<10; i++) {
                if (bsGrid[i][cellY].hasShip) {
                    if (bsGrid[i][cellY].navireRef.getNom().equals(nom)) {
                        bsGrid[i][cellY].navireRef = null;
                        bsGrid[i][cellY].hasShip = false;
                        Ships.remove(GetCellIndexInShipsArray(i, cellY));
                    }
                }
            }
        }
        else {
            for (int i=0; i<10; i++) {
                if (bsGrid[cellX][i].hasShip) {
                    if (bsGrid[cellX][i].navireRef.getNom().equals(nom)) {
                        bsGrid[cellX][i].navireRef = null;
                        bsGrid[cellX][i].hasShip = false;
                        Ships.remove(GetCellIndexInShipsArray(cellX, i));
                    }
                }
            }
        }
        invalidate();
    }

    public void ClearGrid() {
        for (int i=0; i<10; i++) {
            for (int j=0; j<10; j++) {
                if (bsGrid[i][j].hasShip) {
                    removeShip(bsGrid[i][j]);
                }
            }
        }
    }

    //Retourne l'index du tableau "Ships" qui contient la case aux coordonnées en entrée
    public int GetCellIndexInShipsArray(int coordX, int coordY) {
        for (int i=0; i<Ships.size(); i++) {
            if (Ships.get(i).coordonnees[0] == coordX && Ships.get(i).coordonnees[1] == coordY) {
                return i;
            }
        }
        return -1;
    }

    //Place un bateau sur la grid en testant s'il y entre
    public boolean testAndPlaceShip(int cellX, int cellY , Navire currentship) {
        int shipSize = currentship.getTaille();
        boolean isOk = true;

        ImageButton ship = currentship.getView().findViewById(currentship.getId());

        if (currentship.getOrientationHorizontal()) {
            //L'orientation du bateau est horizontale
            if (cellX+shipSize <= 10) {
                for (int i = 0; i< shipSize; i++ ) {
                    if (bsGrid[cellX + i][cellY].hasShip) {
                        isOk = false;
                        Log.i("Tag", "Hit autre bateau sur cell X : "+bsGrid[cellX + i][cellY].position[0]+" Y : "+bsGrid[cellX + i][cellY].position[1]);
                        break;
                    }
                }
            }
            else {
                isOk = false;
                Log.i("Tag", "Depassement grid, Cell clicked = "+ cellX +" Ship size = " + shipSize);
            }
            if(isOk) {
                ship.setVisibility(View.INVISIBLE);
                for (int i =0; i< shipSize; i++ ) {
                    bsGrid[cellX + i][cellY].hasShip = true;
                    bsGrid[cellX + i][cellY].navireRef = currentship;
                    Ships.add(bsGrid[cellX + i][cellY]);
                }
            }
        }
        else {
            //L'orientation du bateau est verticale
            if (cellY+shipSize <= 10)  {
                for (int i = 0; i< shipSize; i++ ) {
                    if(bsGrid[cellX][cellY+i].hasShip) {
                        isOk = false;
                        Log.i("Tag", "Hit autre bateau sur cell X : "+bsGrid[cellX][cellY+i].position[0]+" Y : "+bsGrid[cellX][cellY+i].position[1]);
                        break;
                    }
                }
            }
            else {
                isOk = false;
                Log.i("Tag", "Depassement grid, Cell clicked = "+ cellY +" Ship size = " + shipSize);
            }

            if(isOk) {
                ship.setVisibility(View.INVISIBLE);
                for (int i =0; i< shipSize; i++ ) {
                    bsGrid[cellX][cellY+i].hasShip = true;
                    bsGrid[cellX][cellY+i].navireRef = currentship;
                    Ships.add(bsGrid[cellX][cellY+i]);
                }
            }
        }
        invalidate(); //pour caller onDraw
        return isOk;
    }

//Fonctions pour Game
    public void initGridWithShips(String[] shipCoords, boolean isSmallGrid) {
        int cellX;
        int cellY;

        for (int shipNo=0; shipNo<5; shipNo++) {
            Navire currentShip = new Navire(shipsNames[shipNo], shipsLengths[shipNo], 0, null);
            for (int i=0; i<shipCoords[shipNo].length(); i+=2) {
                cellX = Character.getNumericValue(shipCoords[shipNo].charAt(i));
                cellY = Character.getNumericValue(shipCoords[shipNo].charAt(i+1));
                bsGrid[cellX][cellY].navireRef = currentShip;
                bsGrid[cellX][cellY].hasShip = true;
                if (isSmallGrid) {
                    Ships.add(bsGrid[cellX][cellY]);
                }
            }
        }
        if (isSmallGrid) {
            invalidate(); //Pour appeler onDraw()
        }
    }

    public int AttackCell(int cellX, int cellY) {

        if(bsGrid[cellX][cellY].hasShip) {
            //Un bateau est touché
            hit.add(bsGrid[cellX][cellY]);

            //Retire 1 HP au bateau
            Navire navireHit = bsGrid[cellX][cellY].navireRef;
            navireHit.lose1HP();

            //Vérifie si le bateau est coulé
            if (navireHit.getHP() == 0) {
                //Le bateau est coulé

                //Déplace les cells du bateau de "hit" vers "destroyedShips"
                Iterator<GridCell> iter = hit.iterator();
                while (iter.hasNext()) {
                    GridCell cell = iter.next();
                    if (cell.navireRef.getNom().equals(navireHit.getNom())) {
                        destroyedShips.add(cell);
                        iter.remove();
                    }
                }
            }
        }
        else {
            //Aucun bateau n'est touché
            noHit.add(bsGrid[cellX][cellY]);
        }
        bsGrid[cellX][cellY].hasBeenClicked = true;
        invalidate(); //pour caller onDraw
        return destroyedShips.size();
    }

    public void RevealGrid() {
        for (int i=0; i<10; i++) {
            for (int j = 0; j < 10; j++) {
                if (bsGrid[i][j].hasShip && !bsGrid[i][j].hasBeenClicked) {
                    Ships.add(bsGrid[i][j]);
                }
            }
        }
        invalidate();
    }
}
