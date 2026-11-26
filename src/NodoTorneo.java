class NodoTorneo {
    Equipo ganador;
    NodoTorneo izq;
    NodoTorneo der;
    int nivel;
    String resultado = "";

    // Constructor para las hojas (los equipos del principio)
    NodoTorneo(Equipo e) {
        this.ganador = e;
        this.izq = null;
        this.der = null;
        this.nivel = 0;
    }

    // Constructor para los partidos
    NodoTorneo(NodoTorneo i, NodoTorneo d, int nivel) {
        this.izq = i;
        this.der = d;
        this.nivel = nivel;
        this.ganador = null;
    }

    boolean esHoja() {
        if (izq == null) {
            return true;
        } else {
            return false;
        }
    }

    String nombreRonda() {
        if (nivel == 1) {
            return "Cuartos de final";
        } else if (nivel == 2) {
            return "Semifinal";
        } else {
            return "Final";
        }
    }

    // Un partido esta listo cuando no tiene ganador y los dos rivales ya estan definidos
    boolean estaListo() {
        if (esHoja()) {
            return false;
        }
        if (ganador != null) {
            return false;
        }
        if (izq.ganador == null) {
            return false;
        }
        if (der.ganador == null) {
            return false;
        }
        return true;
    }

    // Devuelve true si el equipo e juega este partido
    boolean participa(Equipo e) {
        if (esHoja()) {
            return false;
        }
        if (izq.ganador == null || der.ganador == null) {
            return false;
        }
        if (e == null) {
            return false;
        }
        if (izq.ganador.equals(e)) {
            return true;
        }
        if (der.ganador.equals(e)) {
            return true;
        }
        return false;
    }

    Equipo rivalDe(Equipo e) {
        if (!participa(e)) {
            return null;
        }
        if (izq.ganador.equals(e)) {
            return der.ganador;
        } else {
            return izq.ganador;
        }
    }
}