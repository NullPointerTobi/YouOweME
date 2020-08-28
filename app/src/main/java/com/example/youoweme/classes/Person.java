package com.example.youoweme.classes;

import java.io.Serializable;

public class Person implements Serializable
{
    private  int ID;
    private String name;
    private double betrag;

    public int getID()
    {
        return ID;
    }

    public Person(String name) {
        this.name = name;
    }

    public void setID(int ID)
    {
        this.ID = ID;
    }

    public Person()
    {

    }

    public Person(int ID, String name, double betrag) {
        this.ID = ID;
        this.name = name;
        this.betrag = betrag;
    }

    public Person(String name, double betrag)
    {
        this.name = name;
        this.betrag = betrag;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBetrag()
    {
        return betrag;
    }

    public void setBetrag(double betrag) {
        this.betrag = betrag;
    }
}
