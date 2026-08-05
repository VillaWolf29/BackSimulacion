package com.villalobosmelendez.backsimulacion.controller;

import com.villalobosmelendez.backsimulacion.model.Camion;
import com.villalobosmelendez.backsimulacion.model.NetworkDataDTO;
import com.villalobosmelendez.backsimulacion.service.DataLoadService;
import com.villalobosmelendez.backsimulacion.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*")
public class SimulationController {
    @Autowired
    private SimulationService simulationService;

    @Autowired
    private DataLoadService dataLoadService;

    @GetMapping("/red")
    public ResponseEntity<NetworkDataDTO> getRedVial() {
        return ResponseEntity.ok(dataLoadService.getNetworkData());
    }

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarSimulacion() {
        simulationService.iniciarSimulacion();
        return ResponseEntity.ok("Simulación iniciada con éxito");
    }

    @GetMapping("/estado")
    public ResponseEntity<List<Camion>> getEstadoFlota() {
        return ResponseEntity.ok(simulationService.getEstadoFlota());
    }

    @GetMapping("/reporte")
    public ResponseEntity<String> getReporte() {
        return ResponseEntity.ok(simulationService.generarReporteHeuristico());
    }
}
