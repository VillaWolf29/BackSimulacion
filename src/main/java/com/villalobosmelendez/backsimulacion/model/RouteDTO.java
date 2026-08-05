package com.villalobosmelendez.backsimulacion.model;

import lombok.Data;

import java.util.List;

@Data
public class RouteDTO {
    private Integer id_trm_cs;
    private String nombre_tramo;
    private String color;
    private List<List<Double>> points;
}
