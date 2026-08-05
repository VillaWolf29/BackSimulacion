package com.villalobosmelendez.backsimulacion.service;

import com.villalobosmelendez.backsimulacion.model.RouteDTO;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    @Autowired
    private DataLoadService dataLoadService;

    private final Map<String, List<Edge>> graph = new HashMap<>();

    @PostConstruct
    public void BuildGraph() {
        List<RouteDTO> routes = dataLoadService.getNetworkData().getRoutes();

        for (RouteDTO route : routes) {
            List<List<Double>> points = route.getPoints();
            for(int i=0; i < points.size() - 1; i++) {
                String nodeA = formatNode(points.get(i));
                String nodeB = formatNode(points.get(i + 1));

                double distance = calculateHaversineDistance(points.get(i), points.get(i + 1));

                graph.computeIfAbsent(nodeA, k -> new ArrayList<>()).add(new Edge(nodeB, distance));
                graph.computeIfAbsent(nodeB, k -> new ArrayList<>()).add(new Edge(nodeA, distance));
            }
        }

        System.out.println("Grafo de rutas construido con " + graph.size() + " intersecciones/nodos.");
    }
    public List<List<Double>> findShortestPath(List<Double> startCoord, List<Double> endCoord) {
        String startNode = formatNode(startCoord);
        String endNode = formatNode(endCoord);

        if (!graph.containsKey(startNode) || !graph.containsKey(endNode)) {
            return Collections.emptyList(); // Origen o destino no están en la red
        }

        PriorityQueue<NodeCost> pq = new PriorityQueue<>(Comparator.comparingDouble(nc -> nc.cost));
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();

        for (String node : graph.keySet()) {
            distances.put(node, Double.MAX_VALUE);
        }

        distances.put(startNode, 0.0);
        pq.add(new NodeCost(startNode, 0.0));

        while (!pq.isEmpty()) {
            NodeCost current = pq.poll();
            String currentNode = current.node;

            if (currentNode.equals(endNode)) break; // Llegamos al destino

            if (current.cost > distances.get(currentNode)) continue;

            for (Edge edge : graph.getOrDefault(currentNode, Collections.emptyList())) {
                double newDist = distances.get(currentNode) + edge.weight;
                if (newDist < distances.get(edge.target)) {
                    distances.put(edge.target, newDist);
                    previousNodes.put(edge.target, currentNode);
                    pq.add(new NodeCost(edge.target, newDist));
                }
            }
        }
        List<List<Double>> path = new ArrayList<>();
        String curr = endNode;
        if (previousNodes.containsKey(curr) || curr.equals(startNode)) {
            while (curr != null) {
                path.add(parseNode(curr));
                curr = previousNodes.get(curr);
            }
            Collections.reverse(path);
        }

        return path;
    }

    public String formatNode(List<Double> coord) {
        return coord.get(0) + "," + coord.get(1);
    }

    public List<Double> parseNode(String nodeStr) {
        String[] parts = nodeStr.split(",");
        return Arrays.asList(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    // Fórmula de Haversine para calcular distancia geográfica real en kilómetros
    private double calculateHaversineDistance(List<Double> coord1, List<Double> coord2) {
        final int R = 6371; // Radio de la Tierra
        double latDistance = Math.toRadians(coord2.get(0) - coord1.get(0));
        double lonDistance = Math.toRadians(coord2.get(1) - coord1.get(1));
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(coord1.get(0))) * Math.cos(Math.toRadians(coord2.get(0)))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Data
    private static class Edge {
        private String target;
        private double weight;
        public Edge(String target, double weight) { this.target = target; this.weight = weight; }
    }

    @Data
    private static class NodeCost {
        private String node;
        private double cost;
        public NodeCost(String node, double cost) { this.node = node; this.cost = cost; }
    }
}
