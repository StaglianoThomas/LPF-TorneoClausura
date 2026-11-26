import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

class PanelMenu extends JPanel {

    // Interfaz interna: avisa cual equipo eligio el usuario
    interface AlElegirEquipo {
        void elegido(Equipo e);
    }

    PanelMenu(List<Equipo> equipos, final AlElegirEquipo alElegir) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("LPF Torneo Clausura - Elegí tu equipo", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(titulo, BorderLayout.NORTH);

        JPanel grilla = new JPanel(new GridLayout(2, 4, 12, 12));

        for (int i = 0; i < equipos.size(); i++) {
            final Equipo equipoActual = equipos.get(i);

            BotonEquipo boton = new BotonEquipo(equipoActual);
            boton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    alElegir.elegido(equipoActual);
                }
            });

            grilla.add(boton);
        }

        add(grilla, BorderLayout.CENTER);
    }
}