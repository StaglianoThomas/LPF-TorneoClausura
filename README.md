# LPF Torneo Clausura

Juego de fútbol por turnos hecho en Java con Swing, para la materia Programación II.

Elegís uno de ocho equipos del fútbol argentino y tratás de ganar el torneo: cuartos de final, semifinal y final.

## Cómo se juega

1. Elegí tu equipo en el menú inicial.
2. Se sortean los cruces y se muestra el cuadro del torneo (las llaves).
3. En cada partido, hacé clic en un compañero para pasarle la pelota. Las líneas punteadas muestran los pases posibles.
4. Los rivales se mueven por la cancha e intentan interceptar los pases.
5. Para hacer un gol, llegá hasta el arco con pases y pegale.
6. El partido se gana llegando a 2 goles. Si perdés, quedás eliminado.

Si un rival te saca la pelota, volvés a empezar desde el arquero y hay una probabilidad de que te hagan un gol en la contra.

## Estructuras de datos usadas

- **Árbol binario** (`ArbolTorneo`, `NodoTorneo`): representa el cuadro del torneo. Las hojas son los equipos y cada nodo interno es un partido. Se resuelve con un recorrido recursivo en post-orden.
- **Grafo** (`Grafo`): cada jugador de la cancha es un vértice y cada pase posible es una arista. Se implementa con lista de adyacencia.
- **BFS**: calcula la cantidad mínima de pases desde el jugador con la pelota hasta el arco.

## Estructura del proyecto

| Clase | Qué hace |
|---|---|
| `LpfTorneoClausura` | Clase principal, maneja las pantallas del juego |
| `Equipo`, `Estilo` | Datos de los equipos y estilo de camiseta |
| `ArbolTorneo`, `NodoTorneo` | Lógica del torneo |
| `Grafo` | Cancha como grafo y BFS |
| `LogicaPartido` | Reglas del partido y movimiento de rivales |
| `PanelMenu`, `BotonEquipo` | Pantalla de elección de equipo |
| `PanelLlaves` | Dibujo del cuadro del torneo |
| `PanelPartido` | Pantalla del partido |
| `Dibujo` | Utilidades para dibujar camisetas y textos |

## Cómo ejecutarlo

Requisitos: JDK instalado (versión 8 o superior).

1. Clonar el repositorio:
   ```
   git clone https://github.com/TU_USUARIO/NOMBRE_DEL_REPO.git
   ```
2. Abrir la carpeta en IntelliJ IDEA.
3. Ejecutar la clase `LpfTorneoClausura`.

## Autores

- Thomas Stagliano
- Bautista Zorrilla
