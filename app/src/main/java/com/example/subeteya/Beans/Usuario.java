package com.example.subeteya.Beans;

import android.icu.text.SimpleDateFormat;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Usuario implements Serializable {

    // -------------------
    //      ATRIBUTOS:
    // -------------------

    private String uid;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private LineaBus lineaBus;
    private List<LineaBus> lineaBusSuscripcion;
    private Double saldo;
    private Double costoFinal;
    // Empresa de transporte
    private Double recaudacion;
    private String nombreEmpresa;

    // ---------------------------
    //          MÉTODOS:
    // ---------------------------

    public String getNombreCompleto(){
        return  this.nombre + " " + this.apellido;
    }

    public String getFechaInicioBonita() {
        if (this.fechaInicio == null) {
            return "-";
        }
        Date fecha = this.fechaInicio.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy 'a las' HH:mm", Locale.getDefault());
        return sdf.format(fecha);
    }

    public String getFechaFinBonita() {
        if (this.fechaFin == null) {
            return "-";
        }
        Date fecha = this.fechaFin.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy 'a las' HH:mm", Locale.getDefault());
        return sdf.format(fecha);
    }

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

    public LineaBus getLineaBus() {
        return lineaBus;
    }

    public void setLineaBus(LineaBus lineaBus) {
        this.lineaBus = lineaBus;
    }

    public List<LineaBus> getLineaBusSuscripcion() {
        return lineaBusSuscripcion;
    }

    public void setLineaBusSuscripcion(List<LineaBus> lineaBusSuscripcion) {
        this.lineaBusSuscripcion = lineaBusSuscripcion;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
