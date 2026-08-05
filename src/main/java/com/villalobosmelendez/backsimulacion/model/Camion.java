package com.villalobosmelendez.backsimulacion.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Camion {
    private String id;
    private List<Double> posicionActual;
    private double velocidadActual;
    private String estado;
    private List<List<Double>> rutaCompleta;
    private int indiceRuta;
    private List<Double> registroVelocidades = new ArrayList<>();
    public Camion(String id) {
        this.id = id;
        this.estado = "espera";
        this.velocidadActual = 0.0;
        this.indiceRuta = 0;
    }
}
