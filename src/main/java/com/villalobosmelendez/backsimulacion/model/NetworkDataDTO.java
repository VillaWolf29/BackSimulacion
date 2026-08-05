package com.villalobosmelendez.backsimulacion.model;

import lombok.Data;

import java.util.List;

@Data
public class NetworkDataDTO {
    private List<RouteDTO> routes;
    private List<LocationDTO> load;
    private List<LocationDTO> dump;

}
