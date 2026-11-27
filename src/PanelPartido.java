import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;
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

    // ---------- Festejo de gol ----------
    private static final int DURACION_FESTEJO = 2600; // milisegundos
    private static final int CANT_PAPELITOS = 90;

    private javax.swing.Timer timerFestejo;
    private long festejoInicio = 0;
    private Equipo festejoEquipo = null;

    // Papelitos (confeti) con los colores del equipo que hizo el gol
    private double[] papelX = new double[CANT_PAPELITOS];
    private double[] papelY = new double[CANT_PAPELITOS];
    private double[] papelVel = new double[CANT_PAPELITOS];
    private double[] papelVaiven = new double[CANT_PAPELITOS];
    private int[] papelTam = new int[CANT_PAPELITOS];
    private boolean[] papelColor1 = new boolean[CANT_PAPELITOS];
    private Random rndFestejo = new Random();

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
                // Durante el festejo la cancha queda congelada
                if (!festejando()) {
                    L.pasoAnimacionRivales();
                }
                cancha.repaint();
            }
        });

        timerRivales.start();

        // Timer del festejo: repinta seguido para que la animacion sea fluida
        timerFestejo = new javax.swing.Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (!festejando()) {
                    timerFestejo.stop();
                    festejoEquipo = null;

                    // Si el partido ya termino, recien ahora se puede continuar
                    if (L.terminado) {
                        continuar.setVisible(true);
                    }
                }
                cancha.repaint();
            }
        });

        refrescar();
    }

    void detenerTimer() {
        if (timerRivales != null) {
            if (timerRivales.isRunning()) {
                timerRivales.stop();
            }
        }
        if (timerFestejo != null) {
            if (timerFestejo.isRunning()) {
                timerFestejo.stop();
            }
        }
    }

    private boolean festejando() {
        if (festejoEquipo == null) {
            return false;
        }
        long transcurrido = System.currentTimeMillis() - festejoInicio;
        return transcurrido < DURACION_FESTEJO;
    }

    private void arrancarFestejo(Equipo e) {
        festejoEquipo = e;
        festejoInicio = System.currentTimeMillis();

        for (int i = 0; i < CANT_PAPELITOS; i++) {
            papelX[i] = rndFestejo.nextDouble();
            papelY[i] = -rndFestejo.nextDouble() * 0.6;   // arrancan arriba, fuera de la pantalla
            papelVel[i] = 0.35 + rndFestejo.nextDouble() * 0.55;
            papelVaiven[i] = rndFestejo.nextDouble() * Math.PI * 2;
            papelTam[i] = 5 + rndFestejo.nextInt(6);
            papelColor1[i] = rndFestejo.nextBoolean();
        }

        timerFestejo.restart();
    }

    private void refrescar() {
        marcador.setText(ronda + "  |  " + L.mio.nombre + " " + L.golesMio
                + " - " + L.golesRival + " " + L.rival.nombre);

        int pases = L.cancha.minimoPases(L.balon, LogicaPartido.ARCO);
        mensaje.setText(L.mensaje + "  (Pases mínimos al arco desde acá: " + pases + ")");

        if (L.terminado) {
            // Solo freno a los rivales: el timer del festejo sigue corriendo
            // para que la animacion del ultimo gol termine completa
            if (timerRivales.isRunning()) {
                timerRivales.stop();
            }

            if (L.golesMio > L.golesRival) {
                mensaje.setText(L.mensaje + "  ¡GANASTE!");
            } else {
                mensaje.setText(L.mensaje + "  Quedaste eliminado.");
            }

            // El boton aparece cuando termina el festejo
            continuar.setVisible(!festejando());
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
                    // Mientras se festeja el gol no se puede jugar
                    if (festejando()) {
                        return;
                    }

                    // Veo si el clic cayo cerca de algun jugador
                    for (int i = 0; i < L.cancha.vertices.size(); i++) {
                        Grafo.Vertice v = L.cancha.vertices.get(i);

                        double difX = ev.getX() - v.x * getWidth();
                        double difY = ev.getY() - v.y * getHeight();
                        double distancia = Math.sqrt(difX * difX + difY * difY);

                        if (distancia <= 29) {
                            int antesMio = L.golesMio;
                            int antesRival = L.golesRival;

                            L.intentarPase(v.id);

                            // Si cambio el marcador, arranca el festejo
                            if (L.golesMio > antesMio) {
                                arrancarFestejo(L.mio);
                            } else if (L.golesRival > antesRival) {
                                arrancarFestejo(L.rival);
                            }

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

                g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif", Font.BOLD, 9));

                // El 1 lo usa el arquero, los de campo van del 2 al 11
                String etiqueta = String.valueOf(i + 2);
                Dibujo.centrar(g, etiqueta, rx, ry + 3);
            }

            // ---------- Arquero rival ----------
            dibujarArqueroRival(g, W, H);

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

            // ---------- Festejo (va arriba de todo) ----------
            if (festejando()) {
                dibujarFestejo(g, W, H);
            }

            g.dispose();
        }

        private void dibujarArqueroRival(Graphics2D g, int W, int H) {
            LogicaPartido.RivalMovil arq = L.arqueroRival;

            int ax = (int) (arq.x * W);
            int ay = (int) (arq.y * H);

            // Sombra
            g.setColor(new Color(0, 0, 0, 65));
            g.fillOval(ax - 14, ay + 12, 29, 8);

            // Se inclina hacia el lado al que se esta moviendo
            int inclinacion = (int) (arq.vy * 700);
            if (inclinacion > 5) {
                inclinacion = 5;
            }
            if (inclinacion < -5) {
                inclinacion = -5;
            }

            // Brazos abiertos (arriba y abajo, cubriendo el arco)
            g.setColor(new Color(70, 70, 70));
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(ax - 4, ay - 12, ax - 12, ay - 26 + inclinacion);
            g.drawLine(ax - 4, ay + 6, ax - 12, ay + 20 + inclinacion);

            Dibujo.camiseta(g, L.rival, ax - 15, ay - 18 + inclinacion / 2, 30, 33);

            // Guantes
            g.setColor(new Color(190, 255, 60));
            g.fillOval(ax - 17, ay - 31 + inclinacion, 10, 10);
            g.fillOval(ax - 17, ay + 15 + inclinacion, 10, 10);
            g.setColor(new Color(40, 60, 20));
            g.setStroke(new BasicStroke(1f));
            g.drawOval(ax - 17, ay - 31 + inclinacion, 10, 10);
            g.drawOval(ax - 17, ay + 15 + inclinacion, 10, 10);

            // Dorsal
            g.setColor(Dibujo.contraste(L.rival.c1));
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            Dibujo.centrar(g, "1", ax, ay + 3);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            Dibujo.centrar(g, "ARQ", ax, ay + 30);
        }

        private void dibujarFestejo(Graphics2D g, int W, int H) {
            Equipo e = festejoEquipo;
            long transcurrido = System.currentTimeMillis() - festejoInicio;
            double t = transcurrido / (double) DURACION_FESTEJO; // 0 -> 1

            // Opacidad general: aparece rapido y se va al final
            double alfa = 1.0;
            if (t > 0.85) {
                alfa = (1.0 - t) / 0.15;
            }
            if (alfa < 0) {
                alfa = 0;
            }

            // Oscurezco la cancha
            g.setColor(new Color(0, 0, 0, (int) (110 * alfa)));
            g.fillRect(0, 0, W, H);

            // ---- Papelitos ----
            for (int i = 0; i < CANT_PAPELITOS; i++) {
                double py = papelY[i] + papelVel[i] * t * 1.6;
                if (py > 1.05) {
                    continue;
                }
                double px = papelX[i] + Math.sin(papelVaiven[i] + t * 12) * 0.015;

                Color c;
                if (papelColor1[i]) {
                    c = e.c1;
                } else {
                    c = e.c2;
                }
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (235 * alfa)));

                int tam = papelTam[i];
                AffineTransform viejo = g.getTransform();
                g.translate(px * W, py * H);
                g.rotate(papelVaiven[i] + t * 9);
                g.fillRect(-tam / 2, -tam / 4, tam, tam / 2 + 2);
                g.setTransform(viejo);
            }

            // ---- Franja central con los colores del equipo ----
            int franjaAlto = (int) (H * 0.36);
            int franjaY = H / 2 - franjaAlto / 2;

            // Entra deslizandose desde la izquierda en el primer 12% del tiempo
            double entrada = t / 0.12;
            if (entrada > 1) {
                entrada = 1;
            }
            entrada = 1 - Math.pow(1 - entrada, 3); // suavizado
            int desplazamiento = (int) ((1 - entrada) * -W);

            Shape clipViejo = g.getClip();
            g.clipRect(desplazamiento, franjaY, W, franjaAlto);

            Color c1 = conAlfa(e.c1, (int) (235 * alfa));
            Color c2 = conAlfa(e.c2, (int) (235 * alfa));

            g.setColor(c1);
            g.fillRect(desplazamiento, franjaY, W, franjaAlto);

            // Rayas diagonales en movimiento con el color secundario
            g.setColor(conAlfa(e.c2, (int) (110 * alfa)));
            int paso = 46;
            int corrimiento = (int) (transcurrido / 12) % paso;
            for (int x = -franjaAlto - paso; x < W + paso; x = x + paso) {
                int xx = desplazamiento + x + corrimiento;
                int[] xs = { xx, xx + 18, xx + 18 + franjaAlto, xx + franjaAlto };
                int[] ys = { franjaY, franjaY, franjaY + franjaAlto, franjaY + franjaAlto };
                g.fillPolygon(xs, ys, 4);
            }

            // Bordes de la franja
            g.setColor(c2);
            g.fillRect(desplazamiento, franjaY, W, 7);
            g.fillRect(desplazamiento, franjaY + franjaAlto - 7, W, 7);

            g.setClip(clipViejo);

            if (entrada < 1) {
                return; // el texto aparece cuando la franja ya entro
            }

            // ---- Texto GOOOL que se va alargando ----
            int cantO = 2 + (int) (t * 14);
            if (cantO > 7) {
                cantO = 7;
            }
            StringBuilder sb = new StringBuilder("¡¡G");
            for (int i = 0; i < cantO; i++) {
                sb.append('O');
            }
            sb.append("L!!");
            String texto = sb.toString();

            // Tamanio que entre en la pantalla, con un "latido"
            int tamFuente = (int) (franjaAlto * 0.55);
            Font fuente = new Font("SansSerif", Font.BOLD | Font.ITALIC, tamFuente);
            FontRenderContext frc = g.getFontRenderContext();
            TextLayout largoMax = new TextLayout("¡¡GOOOOOOOL!!", fuente, frc);
            double anchoMax = largoMax.getBounds().getWidth();
            if (anchoMax > W * 0.70) {
                tamFuente = (int) (tamFuente * (W * 0.70) / anchoMax);
                fuente = new Font("SansSerif", Font.BOLD | Font.ITALIC, tamFuente);
            }

            double latido = 1.0 + 0.06 * Math.sin(transcurrido / 70.0);
            if (t < 0.22) {
                // "golpe" de entrada: arranca grande y se acomoda
                double k = (t - 0.12) / 0.10;
                if (k < 0) {
                    k = 0;
                }
                latido = latido + (1 - k) * 0.6;
            }

            TextLayout tl = new TextLayout(texto, fuente, frc);
            double tw = tl.getBounds().getWidth();
            double th = tl.getBounds().getHeight();

            AffineTransform viejo = g.getTransform();
            g.translate(W / 2.0, H / 2.0 - franjaAlto * 0.06);
            g.scale(latido, latido);
            g.rotate(-0.05);

            Shape forma = tl.getOutline(AffineTransform.getTranslateInstance(-tw / 2, th / 2));

            Color relleno = Dibujo.contraste(e.c1);
            Color borde = elegirBorde(e, relleno);

            // Sombra del texto
            g.translate(4, 5);
            g.setColor(new Color(0, 0, 0, (int) (120 * alfa)));
            g.fill(forma);
            g.translate(-4, -5);

            // Contorno grueso + relleno
            g.setStroke(new BasicStroke(Math.max(4f, tamFuente / 11f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(conAlfa(borde, (int) (255 * alfa)));
            g.draw(forma);
            g.setColor(conAlfa(relleno, (int) (255 * alfa)));
            g.fill(forma);

            g.setTransform(viejo);

            // ---- Nombre del equipo abajo ----
            String nombre = "GOL DE " + e.nombre.toUpperCase();
            int tamNombre = Math.max(14, franjaAlto / 9);
            g.setFont(new Font("SansSerif", Font.BOLD, tamNombre));
            int yNombre = franjaY + franjaAlto - 16;

            g.setColor(new Color(0, 0, 0, (int) (130 * alfa)));
            Dibujo.centrar(g, nombre, W / 2 + 2, yNombre + 2);
            g.setColor(conAlfa(relleno, (int) (255 * alfa)));
            Dibujo.centrar(g, nombre, W / 2, yNombre);

            // ---- Camisetas a los costados ----
            int camAncho = (int) (franjaAlto * 0.42);
            int camAlto = (int) (camAncho * 1.12);
            int camY = H / 2 - camAlto / 2 + (int) (Math.sin(transcurrido / 110.0) * 6);
            if (W > 520) {
                Dibujo.camiseta(g, e, (int) (W * 0.03), camY, camAncho, camAlto);
                Dibujo.camiseta(g, e, (int) (W * 0.97) - camAncho, camY, camAncho, camAlto);
            }
        }

        private Color conAlfa(Color c, int a) {
            if (a < 0) {
                a = 0;
            }
            if (a > 255) {
                a = 255;
            }
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
        }

        // Elige el color del contorno del texto: el secundario del equipo,
        // salvo que se parezca mucho al relleno (ahi usa el principal)
        private Color elegirBorde(Equipo e, Color relleno) {
            if (diferencia(e.c2, relleno) > 120) {
                return e.c2;
            }
            if (diferencia(e.c1, relleno) > 120) {
                return e.c1;
            }
            return Dibujo.contraste(relleno);
        }

        private int diferencia(Color a, Color b) {
            return Math.abs(a.getRed() - b.getRed())
                    + Math.abs(a.getGreen() - b.getGreen())
                    + Math.abs(a.getBlue() - b.getBlue());
        }
    }
}