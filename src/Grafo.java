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

    // Busqueda en anchura (BFS): cantidad minima de pases para meter gol
    int minimoPases(int origen, int destino) {
        // Arreglo para guardar la minima cantidad de pases desde 'origen' hasta cada jugador
        int[] dist = new int[vertices.size()];

        // Inicializamos todas las distancias en -1 ("aun no visitado")
        for (int i = 0; i < dist.length; i++) {
            dist[i] = -1;
        }

        // El jugador inicial ya tiene la pelota: 0 pases para llegar a si mismo
        dist[origen] = 0;

        // Lista que usamos como cola (FIFO) para ir explorando nivel por nivel
        List<Integer> cola = new ArrayList<Integer>();
        cola.add(origen); // Metemos el jugador inicial en la cola

        // Puntero que indica el proximo elemento a sacar de la cola
        int frente = 0;

        // Mientras queden elementos por procesar en la cola
        while (frente < cola.size()) {
            // Tomamos el siguiente jugador 'u' y avanzamos el puntero 'frente'
            int u = cola.get(frente);
            frente = frente + 1;

            // Si llegamos al jugador destino, devolvemos la cantidad de pases (es el minimo)
            if (u == destino) {
                return dist[u];
            }

            // Obtenemos la lista de jugadores a los que 'u' les puede pasar la pelota
            List<Integer> vecinos = ady.get(u);

            // Revisamos cada posible receptor 'v'
            for (int i = 0; i < vecinos.size(); i++) {
                int v = vecinos.get(i);

                // Solo lo visitamos si todavia no tiene distancia asignada (-1)
                if (dist[v] == -1) {
                    // Llegar a 'v' toma un pase mas que llegar a 'u'
                    dist[v] = dist[u] + 1;
                    // Agregamos a 'v' al final de la cola para explorar sus pases despues
                    cola.add(v);
                }
            }
        }

        // Si salimos del while, no hubo forma de llegar desde 'origen' hasta 'destino'
        return -1;
    }
}