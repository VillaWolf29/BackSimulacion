package com.villalobosmelendez.backsimulacion.model;

import lombok.Data;

import java.util.List;

@Data
public class RouteDTO {
    private Integer idTrmCs;
    private String nombreTramo;
    private String color;
    private List<List<Double>> points;
}
