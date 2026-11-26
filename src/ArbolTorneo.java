import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class ArbolTorneo {
    NodoTorneo raiz;
    Random rnd = new Random();

    ArbolTorneo(List<Equipo> equipos) {
        // Sorteo de los cruces
        List<Equipo> sorteo = new ArrayList<Equipo>(equipos);
        Collections.shuffle(sorteo);

        // Primero creo una hoja por cada equipo
        List<NodoTorneo> actual = new ArrayList<NodoTorneo>();
        for (int i = 0; i < sorteo.size(); i++) {
            actual.add(new NodoTorneo(sorteo.get(i)));
        }

        // Despues voy armando las rondas de a pares hasta quedar con un solo nodo
        int nivel = 0;
        while (actual.size() > 1) {
            nivel = nivel + 1;
            List<NodoTorneo> siguiente = new ArrayList<NodoTorneo>();

            for (int i = 0; i < actual.size(); i = i + 2) {
                NodoTorneo partido = new NodoTorneo(actual.get(i), actual.get(i + 1), nivel);
                siguiente.add(partido);
            }

            actual = siguiente;
        }

        raiz = actual.get(0);
    }

    void registrar(NodoTorneo n, Equipo ganador, int golesIzq, int golesDer) {
        n.ganador = ganador;
        n.resultado = golesIzq + "-" + golesDer;
    }

    void resolverOtrosPartidos(Equipo usuario) {
        resolverRec(raiz, usuario);
    }

    private void resolverRec(NodoTorneo n, Equipo usuario) {
        if (n == null) {
            return;
        }
        if (n.esHoja()) {
            return;
        }

        resolverRec(n.izq, usuario);
        resolverRec(n.der, usuario);

        if (!n.estaListo()) {
            return;
        }

        // Si juega el usuario, no lo simulo
        if (usuario != null) {
            if (n.participa(usuario)) {
                return;
            }
        }

        Equipo a = n.izq.ganador;
        Equipo b = n.der.ganador;

        int ga = 0;
        int gb = 0;

        // Repito hasta que no haya empate
        while (ga == gb) {
            ga = rnd.nextInt(3);
            gb = rnd.nextInt(3);
        }

        if (ga > gb) {
            registrar(n, a, ga, gb);
        } else {
            registrar(n, b, ga, gb);
        }
    }

    NodoTorneo partidoDeUsuario(Equipo usuario) {
        return buscarUsuario(raiz, usuario);
    }

    private NodoTorneo buscarUsuario(NodoTorneo n, Equipo usuario) {
        if (n == null) {
            return null;
        }
        if (n.esHoja()) {
            return null;
        }

        NodoTorneo r = buscarUsuario(n.izq, usuario);
        if (r != null) {
            return r;
        }

        r = buscarUsuario(n.der, usuario);
        if (r != null) {
            return r;
        }

        if (n.estaListo()) {
            if (n.participa(usuario)) {
                return n;
            }
        }

        return null;
    }
}