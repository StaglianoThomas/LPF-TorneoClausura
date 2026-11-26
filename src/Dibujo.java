import java.awt.*;
import java.awt.geom.Path2D;

class Dibujo {

    static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    static void centrar(Graphics2D g, String texto, int cx, int y) {
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, cx - ancho / 2, y);
    }

    // Devuelve negro o blanco segun lo claro que sea el color de fondo
    static Color contraste(Color c) {
        int brillo = (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000;
        if (brillo > 150) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    static void camiseta(Graphics2D g, Equipo e, int x, int y, int w, int h) {
        Path2D p = new Path2D.Double();

        p.moveTo(x + w * 0.30, y);
        p.lineTo(x + w * 0.70, y);
        p.lineTo(x + w, y + h * 0.25);
        p.lineTo(x + w * 0.85, y + h * 0.40);
        p.lineTo(x + w * 0.75, y + h * 0.30);
        p.lineTo(x + w * 0.75, y + h);
        p.lineTo(x + w * 0.25, y + h);
        p.lineTo(x + w * 0.25, y + h * 0.30);
        p.lineTo(x + w * 0.15, y + h * 0.40);
        p.lineTo(x, y + h * 0.25);
        p.closePath();

        Shape clipViejo = g.getClip();

        // Color base
        g.setColor(e.c1);
        g.fill(p);

        // Dibujo el diseÃ±o solo adentro de la camiseta
        g.setClip(p);
        g.setColor(e.c2);

        if (e.estilo == Estilo.RAYAS) {
            for (int i = 1; i < 8; i = i + 2) {
                g.fillRect(x + i * w / 8, y, w / 8 + 1, h);
            }
        } else if (e.estilo == Estilo.BANDA_HORIZONTAL) {
            g.fillRect(x, y + (int) (h * 0.40), w, (int) (h * 0.22));
        } else if (e.estilo == Estilo.BANDA_DIAGONAL) {
            int[] xs = new int[4];
            int[] ys = new int[4];

            xs[0] = x + (int) (w * 0.55);
            xs[1] = x + (int) (w * 0.90);
            xs[2] = x + (int) (w * 0.45);
            xs[3] = x + (int) (w * 0.10);

            ys[0] = y;
            ys[1] = y;
            ys[2] = y + h;
            ys[3] = y + h;

            g.fillPolygon(xs, ys, 4);
        } else if (e.estilo == Estilo.V) {
            int[] xs = new int[6];
            int[] ys = new int[6];

            xs[0] = x + (int) (w * 0.25);
            xs[1] = x + (int) (w * 0.40);
            xs[2] = x + (int) (w * 0.50);
            xs[3] = x + (int) (w * 0.60);
            xs[4] = x + (int) (w * 0.75);
            xs[5] = x + (int) (w * 0.50);

            ys[0] = y;
            ys[1] = y;
            ys[2] = y + (int) (h * 0.50);
            ys[3] = y;
            ys[4] = y;
            ys[5] = y + (int) (h * 0.85);

            g.fillPolygon(xs, ys, 6);
        }
        // Si es LISO no se dibuja nada extra

        g.setClip(clipViejo);

        // Borde de la camiseta
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(1.5f));
        g.draw(p);
    }
}