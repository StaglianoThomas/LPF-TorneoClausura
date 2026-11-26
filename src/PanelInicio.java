import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PanelInicio extends JPanel {

    PanelInicio(Runnable iniciar, Runnable instrucciones, Runnable salir) {
        setLayout(new GridBagLayout());
        setBackground(new Color(15, 35, 28));

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        JLabel titulo = new JLabel("LPF 2026");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 38));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("TORNEO CLAUSURA");
        subtitulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        subtitulo.setForeground(new Color(190, 210, 190));
        subtitulo.setAlignmentX(CENTER_ALIGNMENT);

        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(55));

        contenido.add(crearBoton("INICIAR JUEGO", iniciar));
        contenido.add(Box.createVerticalStrut(15));
        contenido.add(crearBoton("INSTRUCCIONES", instrucciones));
        contenido.add(Box.createVerticalStrut(15));
        contenido.add(crearBoton("SALIR", salir));

        add(contenido);
    }

    private JButton crearBoton(String texto, final Runnable accion) {
        JButton boton = new JButton(texto);

        boton.setFont(new Font("SansSerif", Font.BOLD, 19));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(30, 125, 65));
        boton.setFocusPainted(false);
        boton.setAlignmentX(CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(280, 48));
        boton.setPreferredSize(new Dimension(280, 48));

        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                accion.run();
            }
        });

        return boton;
    }
}