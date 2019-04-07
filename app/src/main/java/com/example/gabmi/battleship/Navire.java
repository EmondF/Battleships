package com.example.gabmi.battleship;

import android.view.View;

public class Navire {

    private String nom;
    private int taille;
    private int id;
    private View view;
    private boolean isHorizontal;
    private int HP;


    protected Navire(String _nom, int _taille, int _id, View _view){
        nom = _nom;
        taille = _taille;
        id = _id;
        view = _view;
        isHorizontal = true;
        HP = _taille;

    }

    public String getNom() {
        return nom;
    }

    public int getTaille() {
        return taille;
    }

    public void setId(int _id) {
        id = _id;
    }

    public int getId() {
        return id;
    }

    public void setView(View v) {
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

    public void lose1HP() {
        HP--;
    }

    public int getHP() {
        return HP;
    }
}
