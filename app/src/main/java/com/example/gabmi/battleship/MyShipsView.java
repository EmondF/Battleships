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
import android.widget.Toast;

import java.util.ArrayList;

public class MyShipsView extends View {

    private GridCell[][] bsGrid;
    public static int gridSize;
    private ArrayList<GridCell> noHit;
    private ArrayList<GridCell> hit;
    private ArrayList<GridCell> Ships;
    private Paint noHitPaint, hitPaint, shipPaint;

    public MyShipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("Tag", "MyShipsView - Constructeur");

        bsGrid = new GridCell[10][10];
        initGrid();

        noHit = new ArrayList<>();
        hit = new ArrayList<>();
        Ships = new ArrayList<>();
        noHitPaint = new Paint();
        hitPaint = new Paint();
        shipPaint = new Paint();

        noHitPaint.setColor(Color.argb(100, 255, 255, 255));    //Blanc
        hitPaint.setColor(Color.argb(255, 255, 0, 0));          //Rouge
        shipPaint.setColor(Color.argb(255, 0, 0 , 0));          //Noir
    }

    //Initier la grid avec des GridCells vides
    private void initGrid() {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                bsGrid[i][j] = new GridCell(i, j);
            }
        }
    }

    //Methode pour que la grille soit toujours de dimension carre
    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        Log.i("Tag", "MyShipsView - onMeasure()");
        gridSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(gridSize, gridSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCanvas(canvas);
    }

    //Redessine tous les ships actuels dans le tableau "Ships"
    private void drawCanvas(Canvas canvas){

        int sideLength = gridSize/10;

        for (int i = 0; i < Ships.size(); i++) {
            Rect rectangle = new Rect(
                    Math.round(Ships.get(i).position[0]),
                    Math.round(Ships.get(i).position[1]),
                    Math.round(Ships.get(i).position[0] + sideLength),
                    Math.round(Ships.get(i).position[1] + sideLength));

            canvas.drawRect(rectangle, shipPaint);
        }
        for (int i = 0; i < noHit.size(); i++) {
            Rect rectangle = new Rect(
                    Math.round(noHit.get(i).position[0]),
                    Math.round(noHit.get(i).position[1]),
                    Math.round(noHit.get(i).position[0] + sideLength),
                    Math.round(noHit.get(i).position[1] + sideLength));

            canvas.drawRect(rectangle, noHitPaint);
        }
        for (int i = 0; i < hit.size(); i++) {
            Rect rectangle = new Rect(
                    Math.round(hit.get(i).position[0]),
                    Math.round(hit.get(i).position[1]),
                    Math.round(hit.get(i).position[0] + sideLength),
                    Math.round(hit.get(i).position[1] + sideLength));

            canvas.drawRect(rectangle, hitPaint);
        }
    }

    public ArrayList<GridCell> GetShipList() {
        return Ships;
    }

    //Retourne l'objet GridCell correspondant à la position en pixel (X,Y) en entrée
    public GridCell getCell(float posX, float posY) {
        float gridFraction = (float)gridSize/10;
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        return bsGrid[cellX][cellY];
    }
/*
    //Savoir quelle cellule on a touch
    public void findCell(float posX, float posY) {
        float gridFraction = (float)gridSize/10;
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        testCell(bsGrid[cellX][cellY]);
        bsGrid[cellX][cellY].position[0] = cellX; //position en pixel de lindice de la cellule
        bsGrid[cellX][cellY].position[1] = cellY;
    }

    //tester si y a un bateau
    private void testCell(GridCell currentCell) {
        if(!currentCell.hasShip && !currentCell.hasBeenClicked) {
            noHit.add(currentCell);
            currentCell.hasBeenClicked = true;
        }
        if(currentCell.hasShip && !currentCell.hasBeenClicked) {
            hit.add(currentCell);
            currentCell.hasBeenClicked = true;
        }
        invalidate(); //pour caller onDraw
    }
*/
    //Remove tout le ship de la GridCell cliquée, donnée en entrée
    public void removeShip(GridCell cellClicked) {
        float gridFraction = (float)gridSize/10;
        int coordX = cellClicked.coordonnees[0];
        int coordY = cellClicked.coordonnees[1];
        Navire navireClicked = cellClicked.navireRef;
        String nom = navireClicked.getNom();

        navireClicked.getView().setVisibility(View.VISIBLE);
        Log.i("Tag", "Before remove : Ships : "+Ships.size());
        if (navireClicked.getOrientationHorizontal()) {
            for (int i=0; i<10; i++) {
                if (bsGrid[i][coordY].hasShip) {
                    if (bsGrid[i][coordY].navireRef.getNom().equals(nom)) {
                        bsGrid[i][coordY].navireRef = null;
                        bsGrid[i][coordY].hasShip = false;
                        Ships.remove(GetCellIndexInShipsArray(i, coordY));
                    }
                }
            }
        }
        else {
            for (int i=0; i<10; i++) {
                if (bsGrid[coordX][i].hasShip) {
                    if (bsGrid[coordX][i].navireRef.getNom().equals(nom)) {
                        bsGrid[coordX][i].navireRef = null;
                        bsGrid[coordX][i].hasShip = false;
                        Ships.remove(GetCellIndexInShipsArray(coordX, i));
                    }
                }
            }
        }
        Log.i("Tag", "After remove : Ships : "+Ships.size());
        invalidate();
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
    public boolean testAndPlaceShip(float posX, float posY , Navire currentship) {

        float gridFraction = (float)gridSize/10;
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        GridCell currentCell;
        int shipSize = currentship.getTaille();
        boolean isOk = true;

        ImageButton ship = currentship.getView().findViewById(currentship.getId());

        if (currentship.getOrientationHorizontal()) {
            //L'orientation du bateau est horizontale
            if (cellX+shipSize <= 10) {
                for (int i = 0; i< shipSize; i++ ) {
                    currentCell = bsGrid[cellX + i][cellY];
                    if (currentCell.hasShip) {
                        isOk = false;
                        Log.i("Tag", "Hit autre bateau sur cell X : "+currentCell.position[0]+" Y : "+currentCell.position[1]);
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
                    currentCell = bsGrid[cellX+i][cellY];
                    currentCell.position[0] = gridFraction*(cellX+i);  //position en pixel de lindice de la cellule
                    currentCell.position[1] = gridFraction*(cellY);    //position en pixel de lindice de la cellule
                    currentCell.coordonnees[0] = cellX+i;
                    currentCell.coordonnees[1] = cellY;
                    currentCell.hasShip = true;
                    currentCell.navireRef = currentship;
                    Ships.add(currentCell);
                }
            }
        }
        else {
            //L'orientation du bateau est verticale
            if (cellY+shipSize <= 10)  {
                for (int i = 0; i< shipSize; i++ ) {
                    currentCell = bsGrid[cellX][cellY+i];
                    if(currentCell.hasShip) {
                        isOk = false;
                        Log.i("Tag", "Hit autre bateau sur cell X : "+currentCell.position[0]+" Y : "+currentCell.position[1]);
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
                    currentCell = bsGrid[cellX][cellY+i];
                    currentCell.position[0] = gridFraction*(cellX);  //position en pixel de lindice de la cellule
                    currentCell.position[1] = gridFraction*(cellY+i);    //position en pixel de lindice de la cellule
                    currentCell.coordonnees[0] = cellX;
                    currentCell.coordonnees[1] = cellY+i;
                    currentCell.hasShip = true;
                    currentCell.navireRef = currentship;
                    Ships.add(currentCell);
                }
            }
        }
        invalidate(); //pour caller onDraw
        return isOk;
    }

}
