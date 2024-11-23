package com.example.subeteya.Beans;

import java.io.Serializable;
import java.util.ArrayList;

public class LineaBus implements Serializable {

    // -------------------
    //      ATRIBUTOS:
    // -------------------

    private String uid;
    private String nombre;
    private Double precioUnitario;
    private Double precioSuscripcion;
    private Double recaudacion;
    private Usuario empresa;
    private ArrayList<String> rutasCarrusel;

    // ---------------------------
    //      GETTER Y SETTERS:
    // ---------------------------

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Usuario getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Usuario empresa) {
        this.empresa = empresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecioSuscripcion() {
        return precioSuscripcion;
    }

    public void setPrecioSuscripcion(Double precioSuscripcion) {
        this.precioSuscripcion = precioSuscripcion;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getRecaudacion() {
        return recaudacion;
    }

    public void setRecaudacion(Double recaudacion) {
        this.recaudacion = recaudacion;
    }

    public ArrayList<String> getRutasCarrusel() {
        return rutasCarrusel;
    }

    public void setRutasCarrusel(ArrayList<String> rutasCarrusel) {
        this.rutasCarrusel = rutasCarrusel;
    }
}
