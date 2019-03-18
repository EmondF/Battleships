package com.example.gabmi.battleship;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    MyShipsView myShipsRef;
    //ImageButton ship1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myShipsRef = findViewById(R.id.myShipsView);
        myShipsRef.setOnTouchListener(this);


        //ship1 =  findViewById(R.id.ship1);
        //ship1.setOnClickListener((View.OnClickListener) this);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            myShipsRef.findCell(event.getX(), event.getY());
        }
        return false;
    }
}
