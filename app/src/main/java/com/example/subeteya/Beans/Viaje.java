package com.example.subeteya.Beans;

import android.icu.text.SimpleDateFormat;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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

    // ---------------------------
    //          MÉTODOS:
    // ---------------------------

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

    public String getDuracionBonita() {
        long segundosTotales = this.duracion.getSeconds();
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;

        // Retornar el string formateado
        return Math.floor(minutos) + " minutos con " + Math.floor(segundos) + " segundos";
    }

    // ---------------------------
    //      GETTER Y SETTERS:
    // ---------------------------

    public Timestamp getDuracion() {
        return duracion;
    }

    public void setDuracion(Timestamp duracion) {
        this.duracion = duracion;
    }

    public Double getCostoFinal() {
        return costoFinal;
    }

    public void setCostoFinal(Double costoFinal) {
        this.costoFinal = Double.parseDouble(String.format("%.1f",costoFinal));
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
