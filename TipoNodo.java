package diagramador;

/**
 * Representa los tipos de nodo que puede tener un diagrama de flujo.
 * Cada tipo determina la forma con la que se dibuja el nodo.
 */
public enum TipoNodo {
    INICIO,      // óvalo
    FIN,         // óvalo
    PROCESO,     // rectángulo
    DECISION     // rombo
}
