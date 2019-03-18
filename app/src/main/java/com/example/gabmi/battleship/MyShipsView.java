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
        bsGrid[5][5].hasShip = true;
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
        drawNoHit(canvas);
        drawShip(canvas);
    }

    //Savoir quelle cellule on a touch
    public void findCell(float posX, float posY) {
        float gridFraction = (float)gridSize/10;
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        testCell(bsGrid[cellX][cellY]);
        bsGrid[cellX][cellY].position[0] = (int)gridFraction*cellX; //position en pixel de lindice de la cellule
        bsGrid[cellX][cellY].position[1] = (int)gridFraction*cellY;
    }

    private void initGrid() {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                bsGrid[i][j] = new GridCell();
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
            Rect rectangle = new Rect(noHit.get(i).position[0], noHit.get(i).position[1], noHit.get(i).position[0] + sideLength, noHit.get(i).position[1] + sideLength);
            canvas.drawRect(rectangle, noHitPaint);
        }
    }

    private void drawShip(Canvas canvas){

        int sideLength = gridSize/10;

        for (int i = 0; i < Ships.size(); i++) {
            Rect rectangle = new Rect(Ships.get(i).position[0], Ships.get(i).position[1], Ships.get(i).position[0] + sideLength, Ships.get(i).position[1] + sideLength);
            canvas.drawRect(rectangle, shipPaint);
        }
    }

    public void findCellPlacement(float posX, float posY, Navire currentship) {


        Log.i("Ship", currentship.nom);
        float gridFraction = (float)gridSize/10;
        int cellX = (int)(posX/gridFraction); //indice de la cellule
        int cellY = (int)(posY/gridFraction);
        testCellPlacement( cellX,  cellY, currentship);
        //testCell(bsGrid[cellX][cellY]);
        bsGrid[cellX][cellY].position[0] = (int)gridFraction*cellX; //position en pixel de lindice de la cellule
        bsGrid[cellX][cellY].position[1] = (int)gridFraction*cellY;
    }

    private void testCellPlacement(int CellX, int CellY , Navire currentship) {

        GridCell currentCell = bsGrid[CellX][CellY];;
        boolean isOk = true;
        float gridFraction = (float)gridSize/10;
        int shipsSize = currentship.taille;

        ImageButton ship = currentship.view.findViewById(currentship.id) ;

        for (int i =0; i< shipsSize; i++ )
        {
            if (CellX+i <= 9)
            {
                currentCell = bsGrid[CellX+i][CellY];
                if(currentCell.hasShip)
                {
                    isOk = false;
                    break;
                }

            }else
                isOk = false;

        }


       if(isOk == true ) {

           ship.setVisibility(getRootView().GONE);

            for (int i =0; i< shipsSize; i++ )
            {

                currentCell = bsGrid[CellX+i][CellY];
                currentCell.position[0] = (int)gridFraction*(CellX+i); //position en pixel de lindice de la cellule
                currentCell.position[1] = (int)gridFraction*CellY;
                Ships.add(currentCell);
                currentCell.hasShip = true;

           }

        }

        invalidate(); //pour caller onDraw
    }

}
