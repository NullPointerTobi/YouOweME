package com.example.youoweme.classes;

import java.io.Serializable;

public class Schuld implements Serializable {
    private int schuldenNR;
    private double schuldenbetrag;
    private String betreff;
    private  String datum;

    private int schuldnerid;

    public Schuld( double schuldenbetrag, String betreff, String datum, int schuldnerid)
    {
        this.schuldenbetrag = schuldenbetrag;
        this.betreff = betreff;
        this.datum = datum;
        this.schuldnerid = schuldnerid;
    }

    public Schuld(int schuldenNR, double schuldenbetrag, String betreff, String datum, int schuldnerid) {
        this.schuldenNR = schuldenNR;
        this.schuldenbetrag = schuldenbetrag;
        this.betreff = betreff;
        this.datum = datum;
        this.schuldnerid = schuldnerid;
    }

    public String getBetreff() {
        return betreff;
    }

    @Override
    public String toString() {
        return "Schuld{" +
                "schuldenNR=" + schuldenNR +
                ", schuldenbetrag=" + schuldenbetrag +
                ", betreff='" + betreff + '\'' +
                ", datum='" + datum + '\'' +
                ", schuldnerid=" + schuldnerid +
                '}';
    }

    public int getSchuldenNR() {
        return schuldenNR;
    }

    public double getSchuldenbetrag() {
        return schuldenbetrag;
    }

    public String getDatum() {
        return datum;
    }
}
