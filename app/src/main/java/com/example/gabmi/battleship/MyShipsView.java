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

public class MyShipsView extends View {

    private GridCell[][] bsGrid;
    private int gridSize;
    private ArrayList<GridCell> noHit;
    private ArrayList<GridCell> HitCell;
    private ArrayList<GridCell> Ships;
    private Paint noHitPaint, shipPaint;

    public MyShipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bsGrid = new GridCell[10][10];
        initGrid();
        //bsGrid[5][5].hasShip = true;
        noHit = new ArrayList<>();
        HitCell = new ArrayList<>();
        noHitPaint = new Paint();
        shipPaint = new Paint();
        Ships = new ArrayList<>();
        noHitPaint.setColor(Color.argb(100, 255, 255, 255));
        shipPaint.setColor(Color.argb(200, 0, 0 , 0));
    }

    //Methode pour que la grille soit toujours de dimension carre
    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        gridSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(gridSize, gridSize);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //drawNoHit(canvas);
        drawShip(canvas);
    }

    public GridCell getCell(float posX, float posY) {
        float gridFraction = (float)gridSize/10;
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        return bsGrid[cellX][cellY];
    }

    public void removeShip(GridCell cellClicked) {
        float gridFraction = (float)gridSize/10;
        int coordX = cellClicked.coordonnees[0];
        int coordY = cellClicked.coordonnees[1];

        Navire navireClicked = cellClicked.navireRef;
        boolean horizontal = navireClicked.isHorizontal;
        String nom = navireClicked.nom;

        navireClicked.getView().setVisibility(View.VISIBLE);

        Log.i("Tag", "Before remove : Ships : "+Ships.size());
        if (horizontal) {
            for (int i=0; i<10; i++) {
                if (bsGrid[i][coordY].hasShip) {
                    if (bsGrid[i][coordY].navireRef.nom.equals(nom)) {
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
                    if (bsGrid[coordX][i].navireRef.nom.equals(nom)) {
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

    public int GetCellIndexInShipsArray(int coordX, int coordY) {
        for (int i=0; i<Ships.size(); i++) {
            if (Ships.get(i).coordonnees[0] == coordX && Ships.get(i).coordonnees[1] == coordY) {
                return i;
            }
        }
        return -1;
    }

    //Savoir quelle cellule on a touch
    public void findCell(float posX, float posY) {
        float gridFraction = (float)gridSize/10;
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        testCell(bsGrid[cellX][cellY]);
        bsGrid[cellX][cellY].position[0] = cellX; //position en pixel de lindice de la cellule
        bsGrid[cellX][cellY].position[1] = cellY;
    }

    private void initGrid() {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                bsGrid[i][j] = new GridCell(i, j);
            }
        }
    }

    //tester si y a un bateau
    private void testCell(GridCell currentCell) {
        if(!currentCell.hasShip && !currentCell.hasBeenClicked) {
            noHit.add(currentCell);
            currentCell.hasBeenClicked = true;
        }
        if(currentCell.hasShip && !currentCell.hasBeenClicked) {
            HitCell.add(currentCell);
            currentCell.hasBeenClicked = true;
        }
        invalidate(); //pour caller onDraw
    }

    private void drawNoHit(Canvas canvas){

        int sideLength = gridSize/10;

        for (int i = 0; i < noHit.size(); i++) {
            Rect rectangle = new Rect((int)noHit.get(i).position[0], (int)noHit.get(i).position[1], (int)noHit.get(i).position[0] + sideLength, (int)noHit.get(i).position[1] + sideLength);
            canvas.drawRect(rectangle, noHitPaint);
        }
    }

    private void drawShip(Canvas canvas){

        int sideLength = gridSize/10;

        for (int i = 0; i < Ships.size(); i++) {
            Rect rectangle = new Rect(
                    Math.round(Ships.get(i).position[0]),
                    Math.round(Ships.get(i).position[1]),
                    Math.round(Ships.get(i).position[0] + sideLength),
                    Math.round(Ships.get(i).position[1] + sideLength));
            canvas.drawRect(rectangle, shipPaint);
        }
    }

    public void findCellPlacement(float posX, float posY, Navire currentship) {

        Log.i("Ship", currentship.nom);
        float gridFraction = (float)gridSize/10;
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        testCellPlacement(cellX,  cellY, currentship, currentship.isHorizontal);
        //bsGrid[cellX][cellY].position[0] = (int)gridFraction*cellX; //position en pixel de lindice de la cellule
        //bsGrid[cellX][cellY].position[1] = (int)gridFraction*cellY;
        //bsGrid[cellX][cellY].coordX = cellX;
        //bsGrid[cellX][cellY].coordY = cellY;
    }

    private void testCellPlacement(int CellX, int CellY , Navire currentship, boolean isHorizontal) {

        Log.i("Tag", "CellX : "+CellX);
        Log.i("Tag", "CellY : "+CellY);
        GridCell currentCell;
        boolean isOk = true;
        float gridFraction = (float)gridSize/10;
        int shipSize = currentship.taille;

        ImageButton ship = currentship.view.findViewById(currentship.id);

        if (isHorizontal) {
            //L'orientation du bateau est horizontale
            if (CellX+shipSize <= 10) {
                for (int i = 0; i< shipSize; i++ ) {
                    currentCell =bsGrid[CellX + i][CellY];
                    if (currentCell.hasShip) {
                        isOk = false;
                        Log.i("Tag", "Hit autre bateau sur cell X : "+currentCell.position[0]+" Y : "+currentCell.position[1]);
                        break;
                    }
                }
            }
            else {
                isOk = false;
                Log.i("Tag", "Depassement grid, Cell clicked = "+ CellX +" Ship size = " + shipSize);
            }
            if(isOk) {
                ship.setVisibility(View.INVISIBLE);
                for (int i =0; i< shipSize; i++ ) {
                    currentCell = bsGrid[CellX+i][CellY];
                    currentCell.position[0] = gridFraction*(CellX+i);  //position en pixel de lindice de la cellule
                    currentCell.position[1] = gridFraction*(CellY);    //position en pixel de lindice de la cellule
                    currentCell.coordonnees[0] = CellX+i;
                    currentCell.coordonnees[1] = CellY;
                    currentCell.hasShip = true;
                    currentCell.navireRef = currentship;
                    Ships.add(currentCell);
                }
            }
        }
        else {
            //L'orientation du bateau est verticale
            if (CellY+shipSize <= 10)  {
                for (int i = 0; i< shipSize; i++ ) {
                    currentCell = bsGrid[CellX][CellY+i];
                    if(currentCell.hasShip) {
                        isOk = false;
                        Log.i("Tag", "Hit autre bateau sur cell X : "+currentCell.position[0]+" Y : "+currentCell.position[1]);
                        break;
                    }
                }
            }
            else {
                isOk = false;
                Log.i("Tag", "Depassement grid, Cell clicked = "+ CellY +" Ship size = " + shipSize);
            }

            if(isOk) {
                ship.setVisibility(View.INVISIBLE);
                for (int i =0; i< shipSize; i++ ) {
                    currentCell = bsGrid[CellX][CellY+i];
                    currentCell.position[0] = gridFraction*(CellX);  //position en pixel de lindice de la cellule
                    currentCell.position[1] = gridFraction*(CellY+i);    //position en pixel de lindice de la cellule
                    currentCell.coordonnees[0] = CellX;
                    currentCell.coordonnees[1] = CellY+i;
                    currentCell.hasShip = true;
                    currentCell.navireRef = currentship;
                    Ships.add(currentCell);
                }
            }
        }
        invalidate(); //pour caller onDraw
    }

}
