package com.villalobosmelendez.backsimulacion.service;

import com.villalobosmelendez.backsimulacion.model.LocationDTO;
import com.villalobosmelendez.backsimulacion.model.NetworkDataDTO;
import com.villalobosmelendez.backsimulacion.model.RouteDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class DataLoadService {
    private NetworkDataDTO networkData;

    @PostConstruct
    public void init() {
        cargarYValidarDatos();
    }

    private void cargarYValidarDatos() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("data-prueba.json").getInputStream();

            networkData = mapper.readValue(inputStream, NetworkDataDTO.class);

            validarDatos();

            System.out.println("Datos cargados en memoria exitosamente.");
            System.out.println("Rutas válidas: " + networkData.getRoutes().size());
            System.out.println("Puntos de carga (Load) válidos: " + networkData.getLoad().size());
            System.out.println("Puntos de descarga (Dump) válidos: " + networkData.getDump().size());

        } catch (Exception e) {
            System.err.println("Error crítico al leer el archivo JSON: " + e.getMessage());
        }
    }

    private void validarDatos() {
        System.out.println("Iniciando validación de datos...");

        List<RouteDTO> rutasValidas = networkData.getRoutes().stream()
                .filter(r -> r.getId_trm_cs() != null && r.getPoints() != null && !r.getPoints().isEmpty())
                .collect(Collectors.toList());

        if (rutasValidas.size() < networkData.getRoutes().size()) {
            System.out.println("⚠️ Se encontraron y descartaron rutas con datos incompletos.");
            networkData.setRoutes(rutasValidas);
        }

        networkData.setLoad(validarUbicaciones(networkData.getLoad(), "Carga"));

        networkData.setDump(validarUbicaciones(networkData.getDump(), "Descarga"));

    }

    private List<LocationDTO> validarUbicaciones(List<LocationDTO> ubicaciones, String tipo) {
        if (ubicaciones == null) return List.of();

        List<LocationDTO> validas = ubicaciones.stream()
                .filter(loc -> loc.getId() != null && loc.getCoor() != null && loc.getCoor().size() == 2)
                .collect(Collectors.toList());

        if (validas.size() < ubicaciones.size()) {
            System.out.println("Se encontraron y descartaron ubicaciones de " + tipo + " con datos incompletos.");
        }
        return validas;

    }

    public NetworkDataDTO getNetworkData() {
        return networkData;
    }

}