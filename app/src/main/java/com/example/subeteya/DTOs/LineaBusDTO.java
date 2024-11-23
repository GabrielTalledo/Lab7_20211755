package com.example.subeteya.DTOs;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class LineaBusDTO {

    // -------------------
    //      ATRIBUTOS:
    // -------------------

    private String nombre;
    private Double precioUnitario;
    private Double precioSuscripcion;
    private Double recaudacion;
    private DocumentReference refEmpresa;
    private ArrayList<String> rutasCarrusel;

    // ---------------------------
    //      GETTER Y SETTERS:
    // ---------------------------

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

    public DocumentReference getRefEmpresa() {
        return refEmpresa;
    }

    public void setRefEmpresa(DocumentReference refEmpresa) {
        this.refEmpresa = refEmpresa;
    }

    public ArrayList<String> getRutasCarrusel() {
        return rutasCarrusel;
    }

    public void setRutasCarrusel(ArrayList<String> rutasCarrusel) {
        this.rutasCarrusel = rutasCarrusel;
    }
}
