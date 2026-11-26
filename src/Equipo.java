import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// ============================== 1) DATOS ===================================

class Equipo {
    String nombre;
    Color c1;
    Color c2;
    Estilo estilo;

    Equipo(String nombre, Color c1, Color c2, Estilo estilo) {
        this.nombre = nombre;
        this.c1 = c1;
        this.c2 = c2;
        this.estilo = estilo;
    }

    // Dos equipos son iguales si tienen el mismo nombre
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Equipo)) {
            return false;
        }
        Equipo otro = (Equipo) o;
        return this.nombre.equals(otro.nombre);
    }

    public int hashCode() {
        return nombre.hashCode();
    }

    static List<Equipo> todos() {
        List<Equipo> lista = new ArrayList<Equipo>();

        lista.add(new Equipo("River Plate", Color.WHITE, Color.RED, Estilo.BANDA_DIAGONAL));
        lista.add(new Equipo("Boca Juniors", Color.BLUE, Color.YELLOW, Estilo.BANDA_HORIZONTAL));
        lista.add(new Equipo("Independiente", Color.RED, Color.WHITE, Estilo.LISO));
        lista.add(new Equipo("Racing Club", new Color(73, 250, 250, 255), Color.WHITE, Estilo.RAYAS));
        lista.add(new Equipo("Estudiantes de La Plata", Color.RED, Color.WHITE, Estilo.RAYAS));
        lista.add(new Equipo("Rosario Central", Color.BLUE, Color.YELLOW, Estilo.RAYAS));
        lista.add(new Equipo("San Lorenzo", Color.RED, Color.BLUE, Estilo.RAYAS));
        lista.add(new Equipo("Vélez Sarsfield", Color.WHITE, Color.BLUE, Estilo.V));

        return lista;
    }
}