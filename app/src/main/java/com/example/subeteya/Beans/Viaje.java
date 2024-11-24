package com.example.subeteya.Beans;

import android.icu.text.SimpleDateFormat;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

public class Viaje implements Serializable {

    // -------------------
    //      ATRIBUTOS:
    // -------------------

    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private Double costoOriginal;
    private Double costoFinal;
    private String lineaBusUid;
    private String usuarioUid;
    private Timestamp duracion;

    // Auxiliares:
    private String fechaInicioBonita;
    private String fechaFinBonita;
    private String duracionBonita;


    // ---------------------------
    //          MÉTODOS:
    // ---------------------------

    public String getFechaInicioBonitaMetodo() {
        if (this.fechaInicio == null) {
            return "-";
        }
        Date fecha = this.fechaInicio.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault());
        return sdf.format(fecha);
    }

    public String getFechaFinBonitaMetodo() {
        if (this.fechaFin == null) {
            return "-";
        }
        Date fecha = this.fechaFin.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault());
        return sdf.format(fecha);
    }

    public String getDuracionBonitaMetodo() {
        if(this.duracion == null){
            return "-";
        }

        long segundosTotales = this.duracion.getSeconds();
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;

        // Retornar el string formateado
        return Math.floor(minutos) + " minutos con " + Math.floor(segundos) + " segundos";
    }

    // ---------------------------
    //      GETTER Y SETTERS:
    // ---------------------------


    public String getDuracionBonita() {
        return duracionBonita;
    }

    public void setDuracionBonita(String duracionBonita) {
        this.duracionBonita = duracionBonita;
    }

    public String getFechaFinBonita() {
        return fechaFinBonita;
    }

    public void setFechaFinBonita(String fechaFinBonita) {
        this.fechaFinBonita = fechaFinBonita;
    }

    public String getFechaInicioBonita() {
        return fechaInicioBonita;
    }

    public void setFechaInicioBonita(String fechaInicioBonita) {
        this.fechaInicioBonita = fechaInicioBonita;
    }

    public Timestamp getDuracion() {
        return duracion;
    }

    public void setDuracion(Timestamp duracion) {
        this.duracion = duracion;
    }

    public Double getCostoFinal() {
        return costoFinal;
    }

    public void setCostoFinal(Object costoFinal) {
        if (costoFinal instanceof String) {
            this.costoFinal = Double.parseDouble((String) costoFinal);
        } else if (costoFinal instanceof Number) {
            this.costoFinal = ((Number) costoFinal).doubleValue();
        } else {
            this.costoFinal = 0.0; // Valor por defecto o manejo de error
        }
    }

    public Double getCostoOriginal() {
        return costoOriginal;
    }

    public void setCostoOriginal(Double costoOriginal) {
        this.costoOriginal = Double.parseDouble(String.format("%.1f",costoOriginal));
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

    public String getLineaBusUid() {
        return lineaBusUid;
    }

    public void setLineaBusUid(String lineaBusUid) {
        this.lineaBusUid = lineaBusUid;
    }

    public String getUsuarioUid() {
        return usuarioUid;
    }

    public void setUsuarioUid(String usuarioUid) {
        this.usuarioUid = usuarioUid;
    }
}
