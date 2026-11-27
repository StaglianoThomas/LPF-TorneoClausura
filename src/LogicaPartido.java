// ======================== LOGICA: REGLAS DEL PARTIDO =======================

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class LogicaPartido {

    static final int ARCO = 11;
    static final int META = 2;
    static final double PROB_GOL_RIVAL_AL_QUITAR = 0.20;
    static final double PROB_GOL_TIRO = 0.80;

    static final int TIPO_DEFENSOR = 0;
    static final int TIPO_MEDIO = 1;
    static final int TIPO_DELANTERO = 2;

    // Clase interna: rival que se mueve por la cancha
    static class RivalMovil {
        int tipo;
        String etiqueta;
        double x;
        double y;
        double vx;
        double vy;

        // Limites de movimiento de cada rival
        double minX = 0.18;
        double maxX = 0.89;
        double minY = 0.10;
        double maxY = 0.90;

        RivalMovil(int tipo, String etiqueta, double x, double y, double vx, double vy) {
            this.tipo = tipo;
            this.etiqueta = etiqueta;
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
    }

    Grafo cancha = crearCancha();
    Equipo mio;
    Equipo rival;
    Random rnd = new Random();
    List<RivalMovil> rivales = new ArrayList<RivalMovil>();

    int golesMio = 0;
    int golesRival = 0;
    int balon = 0;
    boolean terminado = false;

    String mensaje = "¡Arranca! Elegí a qué compañero pasar y esquivá a los rivales.";

    LogicaPartido(Equipo mio, Equipo rival) {
        this.mio = mio;
        this.rival = rival;

        crearRivales();

        reiniciarDesdeArquero(mensaje);
    }

    private void crearRivales() {
        // 4 defensores: suben y bajan, cada uno en su carril
        for (int i = 0; i < 4; i++) {
            double y = 0.20 + i * 0.20;
            double velocidad = 0.005 + i * 0.001;

            if (i % 2 == 1) {
                velocidad = -velocidad;
            }

            RivalMovil defensor = new RivalMovil(TIPO_DEFENSOR, "DEF", 0.72, y, velocidad, 0.0);
            defensor.minX = 0.58;
            defensor.maxX = 0.85;
            rivales.add(defensor);
        }

        // 4 mediocampistas: suben y bajan por el medio
        double[] carriles = new double[4];
        carriles[0] = 0.15;
        carriles[1] = 0.38;
        carriles[2] = 0.62;
        carriles[3] = 0.85;

        for (int i = 0; i < 4; i++) {
            double velocidad = 0.006 + i * 0.001;

            if (i % 2 == 1) {
                velocidad = -velocidad;
            }

            RivalMovil medio = new RivalMovil(TIPO_MEDIO, "MED", 0.50, carriles[i], velocidad, 0.0);
            medio.minX = 0.32;
            medio.maxX = 0.62;
            rivales.add(medio);
        }

        // 2 delanteros: presionan a quien tiene la pelota
        for (int i = 0; i < 2; i++) {
            double angulo = rnd.nextDouble() * Math.PI * 2;
            double velocidad = 0.010;
            double y = 0.35 + i * 0.30;

            rivales.add(new RivalMovil(TIPO_DELANTERO, "DEL", 0.35, y,
                    Math.cos(angulo) * velocidad, Math.sin(angulo) * velocidad));
        }
    }

    static Grafo crearCancha() {
        Grafo g = new Grafo();

        g.agregarVertice(1, "PO", 0.07, 0.50);
        g.agregarVertice(3, "LI", 0.23, 0.15);
        g.agregarVertice(6, "DFC", 0.23, 0.38);
        g.agregarVertice(2, "DFC", 0.23, 0.62);
        g.agregarVertice(4, "LD", 0.23, 0.85);
        g.agregarVertice(7, "MDI", 0.49, 0.20);
        g.agregarVertice(5, "MC", 0.49, 0.50);
        g.agregarVertice(8, "MDD", 0.49, 0.80);
        g.agregarVertice(11, "EI", 0.74, 0.15);
        g.agregarVertice(9, "DC", 0.76, 0.50);
        g.agregarVertice(10, "ED", 0.74, 0.85);
        g.agregarVertice(0, "ARCO", 0.94, 0.50);

        // Conecto a los jugadores que estan cerca entre si
        for (int i = 0; i < g.vertices.size(); i++) {
            for (int j = i + 1; j < g.vertices.size(); j++) {
                Grafo.Vertice a = g.vertices.get(i);
                Grafo.Vertice b = g.vertices.get(j);

                double difX = a.x - b.x;
                double difY = a.y - b.y;
                double distancia = Math.sqrt(difX * difX + difY * difY);

                if (distancia <= 0.42) {
                    // El arquero no puede pegarle directo al arco
                    if (!(i == 0 && j == ARCO)) {
                        g.agregarArista(i, j);
                    }
                }
            }
        }

        return g;
    }

    void pasoAnimacionRivales() {
        if (terminado) {
            return;
        }

        Grafo.Vertice vBal = cancha.vertices.get(balon);

        for (int i = 0; i < rivales.size(); i++) {
            RivalMovil rm = rivales.get(i);

            // Solo los delanteros persiguen la pelota
            if (rm.tipo == TIPO_DELANTERO) {
                double dx = vBal.x - rm.x;
                double dy = vBal.y - rm.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 0.06) {
                    dist = 0.06;
                }

                double empuje = 0.0010;

                rm.vx = rm.vx + (dx / dist) * empuje + (rnd.nextDouble() - 0.5) * 0.003;
                rm.vy = rm.vy + (dy / dist) * empuje + (rnd.nextDouble() - 0.5) * 0.003;

                // Limito la velocidad maxima
                double maxVel = 0.015;
                double velActual = Math.sqrt(rm.vx * rm.vx + rm.vy * rm.vy);

                if (velActual > maxVel) {
                    rm.vx = (rm.vx / velActual) * maxVel;
                    rm.vy = (rm.vy / velActual) * maxVel;
                }
            }

            rm.x = rm.x + rm.vx;
            rm.y = rm.y + rm.vy;

            // Rebote en los limites propios de cada rival
            if (rm.x < rm.minX) {
                rm.x = rm.minX;
                rm.vx = Math.abs(rm.vx);
            }
            if (rm.x > rm.maxX) {
                rm.x = rm.maxX;
                rm.vx = -Math.abs(rm.vx);
            }
            if (rm.y < rm.minY) {
                rm.y = rm.minY;
                rm.vy = Math.abs(rm.vy);
            }
            if (rm.y > rm.maxY) {
                rm.y = rm.maxY;
                rm.vy = -Math.abs(rm.vy);
            }
        }
    }

    void reiniciarDesdeArquero(String nuevoMensaje) {
        balon = 0;
        mensaje = nuevoMensaje;
    }

    // Devuelve true si algun rival intercepta el pase
    private boolean hayInterceptor(int origenId, int destinoId) {
        Grafo.Vertice u = cancha.vertices.get(origenId);
        Grafo.Vertice v = cancha.vertices.get(destinoId);

        double vx = v.x - u.x;
        double vy = v.y - u.y;
        double largoCuadrado = vx * vx + vy * vy;

        if (largoCuadrado < 0.000001) {
            return false;
        }

        for (int i = 0; i < rivales.size(); i++) {
            RivalMovil rm = rivales.get(i);

            // Punto mas cercano del rival sobre la linea del pase
            double t = ((rm.x - u.x) * vx + (rm.y - u.y) * vy) / largoCuadrado;
            if (t < 0.0) {
                t = 0.0;
            }
            if (t > 1.0) {
                t = 1.0;
            }

            double px = u.x + t * vx;
            double py = u.y + t * vy;

            double d1x = rm.x - px;
            double d1y = rm.y - py;
            double distLinea = Math.sqrt(d1x * d1x + d1y * d1y);

            double d2x = rm.x - v.x;
            double d2y = rm.y - v.y;
            double distDestino = Math.sqrt(d2x * d2x + d2y * d2y);

            if (distLinea < 0.042) {
                return true;
            }
            if (distDestino < 0.050) {
                return true;
            }
        }

        return false;
    }

    void intentarPase(int destino) {
        if (terminado) {
            return;
        }

        if (destino == balon) {
            mensaje = "Ya tiene la pelota ese jugador. Elegí otro pase.";
            return;
        }

        if (!cancha.sonAdyacentes(balon, destino)) {
            mensaje = "Pase demasiado largo o sin línea directa desde ahí.";
            return;
        }

        if (hayInterceptor(balon, destino)) {
            teLaSaco("¡Un rival te sacó la pelota en el pase!");
            return;
        }

        balon = destino;

        if (destino == ARCO) {
            tirarAlArco();
            return;
        }

        mensaje = "Pase a " + cancha.vertices.get(destino).nombre + ". ¡Seguí avanzando!";
    }

    private void tirarAlArco() {
        if (rnd.nextDouble() < PROB_GOL_TIRO) {
            golesMio = golesMio + 1;

            if (golesMio >= META) {
                terminado = true;
                mensaje = "¡¡GOOOL de " + mio.nombre + "!! ¡Ganaste el partido!";
            } else {
                reiniciarDesdeArquero("¡¡GOOOL de " + mio.nombre + "!! Volvés a empezar desde el arquero.");
            }
        } else {
            teLaSaco("¡El arquero rival tapó el remate!");
        }
    }

    private void teLaSaco(String motivo) {
        if (rnd.nextDouble() < PROB_GOL_RIVAL_AL_QUITAR) {
            golesRival = golesRival + 1;

            if (golesRival >= META) {
                terminado = true;
                mensaje = motivo + " ¡Gol de " + rival.nombre + "!";
            } else {
                reiniciarDesdeArquero(motivo + " ¡GOL de " + rival.nombre + "! Volvés a empezar desde el arquero.");
            }
        } else {
            reiniciarDesdeArquero(motivo + " Volvés a empezar desde el arquero.");
        }
    }
}