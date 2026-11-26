import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

class PanelPartido extends JPanel {

    // Interfaz interna: avisa cuando termina el partido
    interface AlFinalizar {
        void fin(int golesMio, int golesRival);
    }

    private LogicaPartido L;
    private String ronda;

    private JLabel marcador = new JLabel("", SwingConstants.CENTER);
    private JLabel mensaje = new JLabel("", SwingConstants.CENTER);

    private JButton continuar = new JButton("Continuar");
    private Cancha cancha = new Cancha();

    private javax.swing.Timer timerRivales;

    PanelPartido(Equipo mio, Equipo rival, String ronda, final AlFinalizar cb) {
        L = new LogicaPartido(mio, rival);
        this.ronda = ronda;

        setLayout(new BorderLayout());

        marcador.setFont(new Font("SansSerif", Font.BOLD, 20));
        marcador.setOpaque(true);
        marcador.setBackground(new Color(20, 30, 50));
        marcador.setForeground(Color.WHITE);
        marcador.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        add(marcador, BorderLayout.NORTH);
        add(cancha, BorderLayout.CENTER);

        JPanel sur = new JPanel(new GridLayout(0, 1, 0, 4));

        mensaje.setFont(new Font("SansSerif", Font.BOLD, 16));

        String textoAyuda = "Hacé clic en un jugador para pasarsela. Esquivá a los rivales: "
                + "si te la sacan, volvés al arquero y pueden hacerte gol en la contra.";

        JLabel ayuda = new JLabel(textoAyuda, SwingConstants.CENTER);

        continuar.setFont(new Font("SansSerif", Font.BOLD, 16));
        continuar.setVisible(false);

        continuar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                detenerTimer();
                cb.fin(L.golesMio, L.golesRival);
            }
        });

        sur.add(mensaje);
        sur.add(ayuda);
        sur.add(continuar);

        add(sur, BorderLayout.SOUTH);

        // Cada 45 milisegundos se mueven los rivales
        timerRivales = new javax.swing.Timer(45, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                L.pasoAnimacionRivales();
                cancha.repaint();
            }
        });

        timerRivales.start();

        refrescar();
    }

    void detenerTimer() {
        if (timerRivales != null) {
            if (timerRivales.isRunning()) {
                timerRivales.stop();
            }
        }
    }

    private void refrescar() {
        marcador.setText(ronda + "  |  " + L.mio.nombre + " " + L.golesMio
                + " - " + L.golesRival + " " + L.rival.nombre);

        int pases = L.cancha.minimoPases(L.balon, LogicaPartido.ARCO);
        mensaje.setText(L.mensaje + "  (Pases mínimos al arco desde acá: " + pases + ")");

        if (L.terminado) {
            detenerTimer();

            if (L.golesMio > L.golesRival) {
                mensaje.setText(L.mensaje + "  ¡GANASTE!");
            } else {
                mensaje.setText(L.mensaje + "  Quedaste eliminado.");
            }

            continuar.setVisible(true);
        }

        cancha.repaint();
    }

    private class Cancha extends JPanel {

        private Color linea = new Color(245, 250, 242, 225);
        private Color verde1 = new Color(43, 133, 58);
        private Color verde2 = new Color(36, 119, 51);

        Cancha() {
            setBackground(verde1);
            setOpaque(true);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent ev) {
                    // Veo si el clic cayo cerca de algun jugador
                    for (int i = 0; i < L.cancha.vertices.size(); i++) {
                        Grafo.Vertice v = L.cancha.vertices.get(i);

                        double difX = ev.getX() - v.x * getWidth();
                        double difY = ev.getY() - v.y * getHeight();
                        double distancia = Math.sqrt(difX * difX + difY * difY);

                        if (distancia <= 29) {
                            L.intentarPase(v.id);
                            refrescar();
                            return;
                        }
                    }
                }
            });
        }

        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);

            Graphics2D g = (Graphics2D) g0.create();
            Dibujo.suavizar(g);

            int W = getWidth();
            int H = getHeight();

            if (W <= 0 || H <= 0) {
                g.dispose();
                return;
            }

            // ---------- Cesped con franjas ----------
            g.setColor(new Color(24, 78, 39));
            g.fillRect(0, 0, W, H);

            int margen = 12;
            int fw = W - margen * 2;
            int fh = H - margen * 2;

            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    g.setColor(verde1);
                } else {
                    g.setColor(verde2);
                }

                int x = margen + i * fw / 10;
                g.fillRect(x, margen, fw / 10 + 1, fh);
            }

            // Textura del cesped
            g.setColor(new Color(255, 255, 255, 13));
            for (int y = margen + 18; y < H - margen; y = y + 34) {
                g.drawLine(margen + 2, y, W - margen - 2, y);
            }

            // ---------- Lineas de la cancha ----------
            g.setColor(linea);
            g.setStroke(new BasicStroke(2.2f));

            g.drawRoundRect(margen, margen, fw, fh, 2, 2);
            g.drawLine(W / 2, margen, W / 2, H - margen);

            int radio = H / 8;
            if (radio > 52) {
                radio = 52;
            }

            g.drawOval(W / 2 - radio, H / 2 - radio, radio * 2, radio * 2);
            g.fillOval(W / 2 - 3, H / 2 - 3, 6, 6);

            int areaAlto = (int) (fh * 0.42);
            int areaAncho = (int) (fw * 0.115);

            int chicaAlto = (int) (fh * 0.20);
            int chicaAncho = (int) (fw * 0.045);

            g.drawRect(margen, H / 2 - areaAlto / 2, areaAncho, areaAlto);
            g.drawRect(W - margen - areaAncho, H / 2 - areaAlto / 2, areaAncho, areaAlto);

            g.drawRect(margen, H / 2 - chicaAlto / 2, chicaAncho, chicaAlto);
            g.drawRect(W - margen - chicaAncho, H / 2 - chicaAlto / 2, chicaAncho, chicaAlto);

            g.fillOval(margen + areaAncho * 2 / 3 - 3, H / 2 - 3, 6, 6);
            g.fillOval(W - margen - areaAncho * 2 / 3 - 3, H / 2 - 3, 6, 6);

            // ---------- Arcos ----------
            int arcoAlto = (int) (fh * 0.16);
            if (arcoAlto < 42) {
                arcoAlto = 42;
            }

            g.setColor(new Color(235, 240, 235));
            g.setStroke(new BasicStroke(3f));

            g.drawRect(margen - 8, H / 2 - arcoAlto / 2, 8, arcoAlto);
            g.drawRect(W - margen, H / 2 - arcoAlto / 2, 8, arcoAlto);

            // ---------- Pases posibles (lineas punteadas) ----------
            Grafo gr = L.cancha;
            Grafo.Vertice actual = gr.vertices.get(L.balon);

            for (int i = 0; i < gr.aristas.size(); i++) {
                Grafo.Arista a = gr.aristas.get(i);

                if (a.a == L.balon || a.b == L.balon) {

                    int otro;
                    if (a.a == L.balon) {
                        otro = a.b;
                    } else {
                        otro = a.a;
                    }

                    Grafo.Vertice destino = gr.vertices.get(otro);

                    g.setColor(new Color(255, 255, 255, 175));

                    float[] guiones = new float[2];
                    guiones[0] = 7f;
                    guiones[1] = 7f;

                    g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND, 1f, guiones, 0f));

                    g.drawLine((int) (actual.x * W), (int) (actual.y * H),
                            (int) (destino.x * W), (int) (destino.y * H));

                    g.setColor(new Color(255, 255, 255, 210));
                    g.fillOval((int) (destino.x * W) - 4, (int) (destino.y * H) - 4, 8, 8);
                }
            }

            // ---------- Rivales ----------
            for (int i = 0; i < L.rivales.size(); i++) {
                LogicaPartido.RivalMovil rm = L.rivales.get(i);

                int rx = (int) (rm.x * W);
                int ry = (int) (rm.y * H);

                g.setColor(new Color(0, 0, 0, 65));
                g.fillOval(rx - 13, ry + 11, 27, 8);

                Dibujo.camiseta(g, L.rival, rx - 14, ry - 17, 28, 31);

                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 9));

                String etiqueta;
                if (i == 4) {
                    etiqueta = "DEF";
                } else {
                    etiqueta = "R" + (i + 1);
                }
                Dibujo.centrar(g, etiqueta, rx, ry + 3);
            }

            // ---------- Jugadores propios y el arco ----------
            for (int i = 0; i < gr.vertices.size(); i++) {
                Grafo.Vertice v = gr.vertices.get(i);

                int x = (int) (v.x * W);
                int y = (int) (v.y * H);

                if (v.id == LogicaPartido.ARCO) {
                    // Dibujo del arco rival
                    g.setColor(new Color(20, 35, 25, 90));
                    g.fillRoundRect(x - 15, y - 37, 30, 74, 5, 5);

                    g.setColor(new Color(238, 244, 238));
                    g.fillRoundRect(x - 17, y - 39, 30, 74, 5, 5);

                    g.setColor(new Color(35, 55, 40));
                    g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(x - 17, y - 39, 30, 74, 5, 5);

                    g.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g.setColor(new Color(35, 55, 40));
                    Dibujo.centrar(g, "ARCO", x - 2, y + 52);
                } else {
                    // Sombra
                    g.setColor(new Color(0, 0, 0, 65));
                    g.fillOval(x - 15, y + 12, 30, 9);

                    // Resaltado del que tiene la pelota
                    if (v.id == L.balon) {
                        g.setColor(new Color(255, 220, 80, 100));
                        g.fillOval(x - 23, y - 24, 46, 46);
                    }

                    Dibujo.camiseta(g, L.mio, x - 15, y - 19, 30, 34);

                    g.setColor(Dibujo.contraste(L.mio.c1));
                    g.setFont(new Font("SansSerif", Font.BOLD, 9));
                    Dibujo.centrar(g, String.valueOf(v.dorsal), x, y + 3);

                    g.setColor(Color.WHITE);
                    g.setFont(new Font("SansSerif", Font.BOLD, 10));
                    Dibujo.centrar(g, v.nombre, x, y + 31);
                }
            }

            // ---------- Pelota ----------
            Grafo.Vertice poseedor = gr.vertices.get(L.balon);

            int bx = (int) (poseedor.x * W) + 17;
            int by = (int) (poseedor.y * H) - 19;

            g.setColor(new Color(0, 0, 0, 90));
            g.fillOval(bx - 1, by + 12, 16, 6);

            g.setColor(Color.WHITE);
            g.fillOval(bx - 7, by - 7, 15, 15);

            g.setColor(new Color(25, 25, 25));
            g.setStroke(new BasicStroke(1.3f));

            g.drawOval(bx - 7, by - 7, 15, 15);
            g.drawLine(bx - 3, by - 3, bx + 3, by + 3);
            g.drawLine(bx + 3, by - 3, bx - 3, by + 3);

            g.dispose();
        }
    }
}