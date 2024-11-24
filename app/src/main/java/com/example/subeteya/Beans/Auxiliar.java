package com.example.subeteya.Beans;

import java.util.List;

public class Auxiliar {

    // -------------------
    //      ATRIBUTOS:
    // -------------------

    private List<Viaje> listaViajes;
    private List<LineaBus> listaLineasBuses;

    // -------------------
    //     CONTRUCTOR:
    // -------------------

    public Auxiliar() {
    }

    public Auxiliar(List<Viaje> listaViajes, List<LineaBus> listaLineasBuses) {
        this.listaViajes = listaViajes;
        this.listaLineasBuses = listaLineasBuses;
    }

    // ---------------------------
    //      GETTER Y SETTERS:
    // ---------------------------

    public List<LineaBus> getListaLineasBuses() {
        return listaLineasBuses;
    }

    public void setListaLineasBuses(List<LineaBus> listaLineasBuses) {
        this.listaLineasBuses = listaLineasBuses;
    }

    public List<Viaje> getListaViajes() {
        return listaViajes;
    }

    public void setListaViajes(List<Viaje> listaViajes) {
        this.listaViajes = listaViajes;
    }
}
