package com.example.gabmi.battleship;

import android.view.View;

public class Navire {

    protected String nom;
    protected int taille;
    protected int id;
    protected View view;
    protected boolean isHorizontal;


    protected Navire(String _nom, int _taille, int _id, View _view){
        nom = _nom;
        taille = _taille;
        id = _id;
        view = _view;
        isHorizontal = true;
    }

    public void setId(int _id)
    {
        id = _id;
    }

    public void setView(View v)
    {
        view = v;
    }

    public View getView() {
        return view;
    }

    public void setOrientationHorizontal(boolean orientation) {
        isHorizontal = orientation;
    }

    public boolean getOrientationHorizontal() {
        return isHorizontal;
    }
}
