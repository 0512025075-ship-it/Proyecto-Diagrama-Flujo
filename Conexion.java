package diagramador;

/**
 * Representa una conexión (flecha) dirigida entre dos nodos del diagrama.
 * La etiqueta se usa opcionalmente en nodos de tipo DECISION (ej. "Sí"/"No").
 */
public class Conexion {

    private final Nodo origen;
    private final Nodo destino;
    private String etiqueta;

    public Conexion(Nodo origen, Nodo destino) {
        this.origen = origen;
        this.destino = destino;
        this.etiqueta = "";
    }

    public Nodo getOrigen() {
        return origen;
    }

    public Nodo getDestino() {
        return destino;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }
}
