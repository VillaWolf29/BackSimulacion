package com.villalobosmelendez.backsimulacion.service;

import com.villalobosmelendez.backsimulacion.model.Camion;
import com.villalobosmelendez.backsimulacion.model.LocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class SimulationService {
    @Autowired
    private DataLoadService dataLoadService;

    @Autowired
    private GraphService graphService;

    private List<Camion> flota = new ArrayList<>();
    private boolean simulacionActiva = false;
    private final Random random = new Random();

    public void iniciarSimulacion() {
        flota.clear();
        List<LocationDTO> cargas = dataLoadService.getNetworkData().getLoad();
        List<LocationDTO> descargas = dataLoadService.getNetworkData().getDump();

        for (int i = 1; i <= 5; i++) {
            Camion camion = new Camion("CAM-00" + i);
            asignarRutaValida(camion, cargas, descargas);
            flota.add(camion);
        }

        simulacionActiva = true;
        System.out.println("Simulación iniciada con 5 camiones.");
    }

    private void asignarRutaValida(Camion camion, List<LocationDTO> cargas, List<LocationDTO> descargas) {
        List<List<Double>> ruta = new ArrayList<>();
        LocationDTO origen = null;
        LocationDTO destino = null;

        while (ruta.isEmpty()) {
            origen = cargas.get(random.nextInt(cargas.size()));
            destino = descargas.get(random.nextInt(descargas.size()));
            ruta = graphService.findShortestPath(origen.getCoor(), destino.getCoor());
        }

        camion.setRutaCompleta(ruta);
        camion.setPosicionActual(ruta.get(0));
        camion.setEstado("MOVIMIENTO");
        System.out.println(camion.getId() + " asignado. Origen: " + origen.getName() + " -> Destino: " + destino.getName());
    }

    public List<Camion> getEstadoFlota() {
        return flota;
    }

    @Scheduled(fixedRate = 1000)
    public void moverCamiones() {
        if (!simulacionActiva) return;

        boolean todosFinalizados = true;

        for (Camion camion : flota) {
            if ("FINALIZADO".equals(camion.getEstado())) continue;

            todosFinalizados = false;

            // 1. Generar velocidad aleatoria entre 40 y 80 km/h
            double velocidadKmh = 40 + random.nextDouble() * 40;
            camion.setVelocidadActual(Math.round(velocidadKmh * 100.0) / 100.0);

            // Conservar las muestras necesarias para elaborar el reporte
            camion.getRegistroVelocidades().add(camion.getVelocidadActual());

            // 2. Simplificación matemática para mover el camión en el mapa (grados por segundo)
            // (1 grado de lat/lon es aprox 111 km. Esto asegura un movimiento visual fluido)
            double velocidadGradosPorSeg = (velocidadKmh / 3600.0) / 111.0;

            List<List<Double>> ruta = camion.getRutaCompleta();
            int index = camion.getIndiceRuta();

            // 3. Interpolar sobre el recorrido calculado
            if (index < ruta.size() - 1) {
                List<Double> actual = camion.getPosicionActual();
                List<Double> destinoNodo = ruta.get(index + 1);

                double dLat = destinoNodo.get(0) - actual.get(0);
                double dLon = destinoNodo.get(1) - actual.get(1);
                double distanciaPuntos = Math.sqrt(dLat * dLat + dLon * dLon);

                if (distanciaPuntos <= velocidadGradosPorSeg) {
                    // Alcanzó el siguiente nodo de la polilínea
                    camion.setPosicionActual(destinoNodo);
                    camion.setIndiceRuta(index + 1);
                } else {
                    // Aún está en tránsito entre dos nodos, calculamos su nueva posición (interpolación)
                    double ratio = velocidadGradosPorSeg / distanciaPuntos;
                    double nuevaLat = actual.get(0) + dLat * ratio;
                    double nuevaLon = actual.get(1) + dLon * ratio;
                    camion.setPosicionActual(List.of(nuevaLat, nuevaLon));
                }
            } else {
                // Finalizar en el destino sin saltos fuera del recorrido
                camion.setEstado("FINALIZADO");
                camion.setVelocidadActual(0.0);
            }
        }

        if (todosFinalizados) {
            simulacionActiva = false;
            System.out.println("🏁 Simulación finalizada. Todos los camiones llegaron a su destino.");
        }
    }

    public String generarReporteHeuristico() {
        if (flota.isEmpty()) return "La simulación aún no ha iniciado.";

        StringBuilder reporte = new StringBuilder();
        double sumaPromediosFlota = 0;
        int camionesValidos = 0;

        for (Camion c : flota) {
            List<Double> velocidades = c.getRegistroVelocidades();
            if (velocidades.isEmpty()) continue;

            double min = velocidades.stream().min(Double::compareTo).orElse(0.0);
            double max = velocidades.stream().max(Double::compareTo).orElse(0.0);
            double sum = velocidades.stream().mapToDouble(Double::doubleValue).sum();
            double avg = sum / velocidades.size();

            sumaPromediosFlota += avg;
            camionesValidos++;

            reporte.append(String.format("Camión %s - Muestras: %d | Min: %.2f km/h | Max: %.2f km/h | Promedio: %.2f km/h\n",
                    c.getId(), velocidades.size(), min, max, avg));
        }

        if (camionesValidos > 0) {
            double promedioFlota = sumaPromediosFlota / camionesValidos;
            reporte.append("\n=== Análisis de la Flota ===\n");
            reporte.append(String.format("La velocidad promedio de toda la flota fue de %.2f km/h. ", promedioFlota));

            for (Camion c : flota) {
                if (c.getRegistroVelocidades().size() < 10) {
                    reporte.append(String.format("Advertencia: El camión %s tiene muy pocas muestras para un análisis preciso. ", c.getId()));
                }
            }
        }

        return reporte.toString();
    }
}
