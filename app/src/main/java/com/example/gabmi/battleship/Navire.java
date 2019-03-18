package com.example.gabmi.battleship;

import android.view.View;

public class Navire {

    protected String nom;
    protected int taille;
    protected int id;
    View view;

    protected Navire(String nom, int taille){
        this.nom = nom;
        this.taille = taille;

    }

    public void setId(int _id)
    {
        id = _id;
    }

    public void setView(View v)
    {
        view = v;
    }
}
