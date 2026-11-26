// ============================ LOGICA: GRAFO ================================

import java.util.ArrayList;
import java.util.List;

class Grafo {

    // Clase interna: cada jugador de la cancha es un vertice
    static class Vertice {
        int id;
        int dorsal;
        String nombre;
        double x;
        double y;

        Vertice(int id, int dorsal, String nombre, double x, double y) {
            this.id = id;
            this.dorsal = dorsal;
            this.nombre = nombre;
            this.x = x;
            this.y = y;
        }
    }

    // Clase interna: un pase posible entre dos jugadores
    static class Arista {
        int a;
        int b;

        Arista(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    List<Vertice> vertices = new ArrayList<Vertice>();
    List<Arista> aristas = new ArrayList<Arista>();
    List<List<Integer>> ady = new ArrayList<List<Integer>>();

    int agregarVertice(int dorsal, String nombre, double x, double y) {
        int id = vertices.size();
        vertices.add(new Vertice(id, dorsal, nombre, x, y));
        ady.add(new ArrayList<Integer>());
        return id;
    }

    void agregarArista(int a, int b) {
        aristas.add(new Arista(a, b));
        ady.get(a).add(b);
        ady.get(b).add(a);
    }

    boolean sonAdyacentes(int a, int b) {
        List<Integer> vecinos = ady.get(a);
        for (int i = 0; i < vecinos.size(); i++) {
            if (vecinos.get(i) == b) {
                return true;
            }
        }
        return false;
    }

    // Busqueda en anchura (BFS): cantidad minima de pases entre dos jugadores
    int minimoPases(int origen, int destino) {
        int[] dist = new int[vertices.size()];
        for (int i = 0; i < dist.length; i++) {
            dist[i] = -1;
        }
        dist[origen] = 0;

        List<Integer> cola = new ArrayList<Integer>();
        cola.add(origen);
        int frente = 0;

        while (frente < cola.size()) {
            int u = cola.get(frente);
            frente = frente + 1;

            if (u == destino) {
                return dist[u];
            }

            List<Integer> vecinos = ady.get(u);
            for (int i = 0; i < vecinos.size(); i++) {
                int v = vecinos.get(i);
                if (dist[v] == -1) {
                    dist[v] = dist[u] + 1;
                    cola.add(v);
                }
            }
        }

        return -1;
    }
}