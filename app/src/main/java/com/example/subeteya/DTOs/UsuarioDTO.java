package com.example.subeteya.DTOs;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class UsuarioDTO {

    // -------------------
    //      ATRIBUTOS:
    // -------------------

    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private DocumentReference refLineaBus;
    private List<DocumentReference> refLineaBusSuscripcion;
    private Double saldo;
    private Double costoFinal;
    // Empresa de transporte
    private Double recaudacion;
    private String nombreEmpresa;

    // ---------------------------
    //      GETTER Y SETTERS:
    // ---------------------------

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Double getCostoFinal() {
        return costoFinal;
    }

    public void setCostoFinal(Double costoFinal) {
        this.costoFinal = costoFinal;
    }

    public Timestamp getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Timestamp fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Timestamp getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Timestamp fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public Double getRecaudacion() {
        return recaudacion;
    }

    public void setRecaudacion(Double recaudacion) {
        this.recaudacion = recaudacion;
    }

    public DocumentReference getRefLineaBus() {
        return refLineaBus;
    }

    public void setRefLineaBus(DocumentReference refLineaBus) {
        this.refLineaBus = refLineaBus;
    }

    public List<DocumentReference> getRefLineaBusSuscripcion() {
        return refLineaBusSuscripcion;
    }

    public void setRefLineaBusSuscripcion(List<DocumentReference> refLineaBusSuscripcion) {
        this.refLineaBusSuscripcion = refLineaBusSuscripcion;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

}
