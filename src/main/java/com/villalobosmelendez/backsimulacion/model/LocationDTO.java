package com.villalobosmelendez.backsimulacion.model;

import lombok.Data;

import java.util.List;

@Data
public class LocationDTO {
    private Integer id;
    private String name;
    private List<Double> coordinates;
    private Integer radio;
}
