package com.villalobosmelendez.backsimulacion.model;

import lombok.Data;

import java.util.List;

@Data
public class NetworkDataDTO {
    private List<RouteDTO> Routes;
    private List<LocationDTO> Load;
    private List<LocationDTO> Dump;

}
