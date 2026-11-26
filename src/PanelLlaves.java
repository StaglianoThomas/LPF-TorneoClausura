import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PanelLlaves extends JPanel {

    ArbolTorneo arbol;
    Equipo usuario;
    Runnable accion;

    JLabel estado = new JLabel("", SwingConstants.CENTER);
    JButton boton = new JButton();

    static final Color ORO = new Color(230, 170, 0);

    PanelLlaves() {
        setLayout(new BorderLayout());
        add(new Lienzo(), BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout(6, 6));
        sur.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));

        estado.setFont(new Font("SansSerif", Font.BOLD, 16));

        boton.setFont(new Font("SansSerif", Font.BOLD, 17));
        boton.setBackground(new Color(25, 115, 210));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);

        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (accion != null) {
                    accion.run();
                }
            }
        });

        sur.add(estado, BorderLayout.NORTH);
        sur.add(boton, BorderLayout.CENTER);

        add(sur, BorderLayout.SOUTH);
    }

    void setEstado(String texto, String textoBoton, Runnable accion) {
        estado.setText(texto);
        boton.setText(textoBoton);
        this.accion = accion;
        repaint();
    }

    // Devuelve true si el equipo es el que eligio el usuario
    private boolean esUsuario(Equipo e) {
        if (e == null) {
            return false;
        }
        if (usuario == null) {
            return false;
        }
        return e.equals(usuario);
    }

    private class Lienzo extends JPanel {

        int contadorHojas = 0;

        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);

            if (arbol == null) {
                return;
            }

            Graphics2D g = (Graphics2D) g0;
            Dibujo.suavizar(g);

            int W = getWidth();
            int H = getHeight();

            int bw = (W - 40 - 150) / 4;
            if (bw < 120) {
                bw = 120;
            }

            String[] titulos = new String[4];
            titulos[0] = "Cuartos de final";
            titulos[1] = "Semifinales";
            titulos[2] = "Final";
            titulos[3] = "CampeÃ³n";

            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.setColor(Color.DARK_GRAY);

            for (int c = 0; c < 4; c++) {
                Dibujo.centrar(g, titulos[c], 20 + c * (bw + 50) + bw / 2, 28);
            }

            contadorHojas = 0;
            dibujarNodo(g, arbol.raiz, W, H);
        }

        private int dibujarNodo(Graphics2D g, NodoTorneo n, int W, int H) {
            int gap = 50;
            int bh = 38;

            int bw = (W - 40 - 150) / 4;
            if (bw < 120) {
                bw = 120;
            }

            int x = 20 + n.nivel * (bw + gap);
            int y;

            if (n.esHoja()) {
                double posicion = contadorHojas + 0.5;
                y = 55 + (int) (posicion * (H - 65) / 8.0);
                contadorHojas = contadorHojas + 1;
            } else {
                int yi = dibujarNodo(g, n.izq, W, H);
                int yd = dibujarNodo(g, n.der, W, H);

                y = (yi + yd) / 2;

                conectar(g, n.izq, n, x, yi, y, gap);
                conectar(g, n.der, n, x, yd, y, gap);
            }

            Equipo e = n.ganador;
            boolean mio = esUsuario(e);

            // Fondo del casillero
            if (e == null) {
                g.setColor(new Color(235, 235, 235));
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillRoundRect(x, y - bh / 2, bw, bh, 12, 12);

            // Franja con los colores del equipo
            if (e != null) {
                g.setColor(e.c1);
                g.fillRect(x + 2, y - bh / 2 + 3, 8, bh - 6);

                g.setColor(e.c2);
                g.fillRect(x + 10, y - bh / 2 + 3, 4, bh - 6);
            }

            // Borde (dorado y grueso si es el equipo del usuario)
            if (mio) {
                g.setColor(ORO);
                g.setStroke(new BasicStroke(3f));
            } else {
                g.setColor(Color.GRAY);
                g.setStroke(new BasicStroke(1f));
            }
            g.drawRoundRect(x, y - bh / 2, bw, bh, 12, 12);

            // Texto del casillero
            g.setColor(Color.BLACK);
            if (mio) {
                g.setFont(new Font("SansSerif", Font.BOLD, 13));
            } else {
                g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            }

            String texto;
            if (e != null) {
                texto = e.nombre;
            } else if (!n.esHoja() && n.izq.ganador != null && n.der.ganador != null) {
                texto = n.izq.ganador.nombre + " vs " + n.der.ganador.nombre;
            } else {
                texto = "?";
            }

            g.drawString(texto, x + 20, y + 5);

            // Resultado del partido
            if (!n.esHoja() && !n.resultado.isEmpty()) {
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.setColor(new Color(60, 60, 60));
                g.drawString(n.resultado, x + 4, y - bh / 2 - 3);
            }

            return y;
        }

        private void conectar(Graphics2D g, NodoTorneo hijo, NodoTorneo padre,
                              int xPadre, int yHijo, int yPadre, int gap) {

            // El camino se pinta de dorado si el usuario viene avanzando por ahi
            boolean camino = false;
            if (esUsuario(hijo.ganador)) {
                if (padre.ganador == null) {
                    camino = true;
                } else if (esUsuario(padre.ganador)) {
                    camino = true;
                }
            }

            if (camino) {
                g.setColor(ORO);
                g.setStroke(new BasicStroke(3f));
            } else {
                g.setColor(Color.GRAY);
                g.setStroke(new BasicStroke(1.5f));
            }

            g.drawLine(xPadre - gap, yHijo, xPadre - gap / 2, yHijo);
            g.drawLine(xPadre - gap / 2, yHijo, xPadre - gap / 2, yPadre);
            g.drawLine(xPadre - gap / 2, yPadre, xPadre, yPadre);
        }
    }
}