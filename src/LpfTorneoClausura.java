import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

// ============================== JUEGO PRINCIPAL ==============================

public class LpfTorneoClausura extends JFrame {

    private CardLayout cartas = new CardLayout();
    private JPanel contenedor = new JPanel(cartas);

    private PanelLlaves llaves = new PanelLlaves();
    private List<Equipo> equipos = Equipo.todos();

    private PanelPartido partidoActual;
    private ArbolTorneo arbol;
    private Equipo usuario;

    public LpfTorneoClausura() {
        super("LPF Torneo Clausura");

        // Al elegir un equipo se inicia el torneo
        PanelMenu.AlElegirEquipo alElegir = new PanelMenu.AlElegirEquipo() {
            public void elegido(Equipo e) {
                iniciar(e);
            }
        };

        contenedor.add(new PanelMenu(equipos, alElegir), "menu");
        contenedor.add(llaves, "llaves");

        add(contenedor);

        setSize(1050, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cartas.show(contenedor, "menu");
    }

    private void iniciar(Equipo elegido) {
        usuario = elegido;

        arbol = new ArbolTorneo(equipos);

        llaves.arbol = arbol;
        llaves.usuario = usuario;

        arbol.resolverOtrosPartidos(usuario);

        NodoTorneo primero = arbol.partidoDeUsuario(usuario);

        if (primero != null) {
            jugar(primero, primero.rivalDe(usuario));
        } else {
            mostrarLlaves();
        }
    }

    private void mostrarLlaves() {
        cartas.show(contenedor, "llaves");

        final NodoTorneo p = arbol.partidoDeUsuario(usuario);

        if (p != null) {
            // Todavia le queda un partido por jugar
            final Equipo rival = p.rivalDe(usuario);

            llaves.setEstado(
                    p.nombreRonda() + ": " + usuario.nombre + " vs " + rival.nombre,
                    "Jugar partido",
                    new Runnable() {
                        public void run() {
                            jugar(p, rival);
                        }
                    });

        } else if (arbol.raiz.ganador != null && arbol.raiz.ganador.equals(usuario)) {
            // Gano la final
            llaves.setEstado(
                    "¡" + usuario.nombre + " es el CAMPEÓN del Torneo Clausura!",
                    "Nuevo torneo",
                    new Runnable() {
                        public void run() {
                            cartas.show(contenedor, "menu");
                        }
                    });

        } else {
            // Perdio
            llaves.setEstado(
                    "Quedaste eliminado con " + usuario.nombre + ".",
                    "Nuevo torneo",
                    new Runnable() {
                        public void run() {
                            cartas.show(contenedor, "menu");
                        }
                    });
        }

        llaves.repaint();
    }

    private void jugar(final NodoTorneo nodo, final Equipo rival) {

        if (partidoActual != null) {
            partidoActual.detenerTimer();
            contenedor.remove(partidoActual);
        }

        PanelPartido.AlFinalizar alFinalizar = new PanelPartido.AlFinalizar() {
            public void fin(int golesMio, int golesRival) {
                finPartido(nodo, rival, golesMio, golesRival);
            }
        };

        partidoActual = new PanelPartido(usuario, rival, nodo.nombreRonda(), alFinalizar);

        contenedor.add(partidoActual, "partido");

        cartas.show(contenedor, "partido");
    }

    private void finPartido(NodoTorneo nodo, Equipo rival, int golesUsuario, int golesRival) {

        boolean gano = false;
        if (golesUsuario > golesRival) {
            gano = true;
        }

        // Veo de que lado del cuadro estaba el usuario para guardar bien el resultado
        boolean usuarioIzq = nodo.izq.ganador.equals(usuario);

        int golesIzq;
        int golesDer;

        if (usuarioIzq) {
            golesIzq = golesUsuario;
            golesDer = golesRival;
        } else {
            golesIzq = golesRival;
            golesDer = golesUsuario;
        }

        if (gano) {
            arbol.registrar(nodo, usuario, golesIzq, golesDer);
            arbol.resolverOtrosPartidos(usuario);
        } else {
            arbol.registrar(nodo, rival, golesIzq, golesDer);
            arbol.resolverOtrosPartidos(null);
        }

        mostrarLlaves();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LpfTorneoClausura().setVisible(true);
            }
        });
    }
}