import javax.swing.*;
import java.awt.*;

class BotonEquipo extends JButton {
    Equipo e;

    BotonEquipo(Equipo e) {
        this.e = e;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
    }

    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        Dibujo.suavizar(g);

        if (getModel().isRollover()) {
            g.setColor(new Color(215, 232, 255));
        } else {
            g.setColor(Color.WHITE);
        }
        g.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

        g.setColor(Color.GRAY);
        g.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

        Dibujo.camiseta(g, e, getWidth() / 2 - 45, 16, 90, 90);

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        Dibujo.centrar(g, e.nombre, getWidth() / 2, getHeight() - 22);
    }
}