package diagramador;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class PanelDiagrama extends JPanel {

    public enum Modo {
        SELECCIONAR,
        AGREGAR_INICIO,
        AGREGAR_FIN,
        AGREGAR_PROCESO,
        AGREGAR_DECISION,
        CONECTAR,
        ELIMINAR
    }

    private final DiagramaModelo modelo;
    private Modo modo = Modo.SELECCIONAR;

    private Nodo nodoArrastrado;
    private int offsetArrastreX, offsetArrastreY;

    private Nodo primerNodoConexion; // primer nodo elegido al conectar

    public PanelDiagrama(DiagramaModelo modelo) {
        this.modelo = modelo;
        setBackground(Color.WHITE);

        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                manejarPresion(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                nodoArrastrado = null;
            }
        };
        addMouseListener(listener);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (nodoArrastrado != null && modo == Modo.SELECCIONAR) {
                    nodoArrastrado.moverA(e.getX() - offsetArrastreX, e.getY() - offsetArrastreY);
                    repaint();
                }
            }
        });
    }

    public void setModo(Modo modo) {
        this.modo = modo;
        primerNodoConexion = null;
        repaint();
    }

    private void manejarPresion(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        switch (modo) {
            case AGREGAR_INICIO -> agregarNodo(TipoNodo.INICIO, x, y);
            case AGREGAR_FIN -> agregarNodo(TipoNodo.FIN, x, y);
            case AGREGAR_PROCESO -> agregarNodo(TipoNodo.PROCESO, x, y);
            case AGREGAR_DECISION -> agregarNodo(TipoNodo.DECISION, x, y);
            case SELECCIONAR -> iniciarArrastre(x, y);
            case CONECTAR -> manejarConexion(x, y);
            case ELIMINAR -> {
                Nodo n = modelo.buscarNodoEn(x, y);
                if (n != null) {
                    modelo.eliminarNodo(n);
                    repaint();
                }
            }
        }
    }

    private void agregarNodo(TipoNodo tipo, int x, int y) {
        Nodo nodo = new Nodo(tipo, x - 55, y - 27);
        String texto = JOptionPane.showInputDialog(this, "Texto del nodo:", tipo.name());
        if (texto != null && !texto.isBlank()) {
            nodo.setTexto(texto);
        }
        modelo.agregarNodo(nodo);
        repaint();
    }

    private void iniciarArrastre(int x, int y) {
        Nodo nodo = modelo.buscarNodoEn(x, y);
        if (nodo != null) {
            nodoArrastrado = nodo;
            offsetArrastreX = x - nodo.getX();
            offsetArrastreY = y - nodo.getY();
        }
    }

    private void manejarConexion(int x, int y) {
        Nodo nodo = modelo.buscarNodoEn(x, y);
        if (nodo == null) {
            return;
        }
        if (primerNodoConexion == null) {
            primerNodoConexion = nodo;
        } else if (primerNodoConexion != nodo) {
            Conexion conexion = new Conexion(primerNodoConexion, nodo);
            if (primerNodoConexion.getTipo() == TipoNodo.DECISION) {
                String etiqueta = JOptionPane.showInputDialog(
                        this, "Etiqueta de esta salida (ej. Sí / No):", "");
                if (etiqueta != null) {
                    conexion.setEtiqueta(etiqueta);
                }
            }
            modelo.agregarConexion(conexion);
            primerNodoConexion = null;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Conexion c : modelo.getConexiones()) {
            dibujarConexion(g2, c);
        }
        for (Nodo n : modelo.getNodos()) {
            dibujarNodo(g2, n);
        }

        if (primerNodoConexion != null) {
            g2.setColor(Color.BLUE);
            Point c = primerNodoConexion.getCentro();
            g2.drawOval(c.x - 5, c.y - 5, 10, 10);
        }
    }

    private void dibujarNodo(Graphics2D g2, Nodo n) {
        Color relleno = n.isResaltado() ? new Color(255, 235, 130) : segunTipo(n.getTipo());
        g2.setColor(relleno);

        switch (n.getTipo()) {
            case INICIO, FIN -> g2.fillOval(n.getX(), n.getY(), n.getAncho(), n.getAlto());
            case PROCESO -> g2.fillRect(n.getX(), n.getY(), n.getAncho(), n.getAlto());
            case DECISION -> g2.fillPolygon(rombo(n));
        }

        g2.setColor(n.isResaltado() ? new Color(200, 120, 0) : Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(n.isResaltado() ? 3f : 1.5f));
        switch (n.getTipo()) {
            case INICIO, FIN -> g2.drawOval(n.getX(), n.getY(), n.getAncho(), n.getAlto());
            case PROCESO -> g2.drawRect(n.getX(), n.getY(), n.getAncho(), n.getAlto());
            case DECISION -> g2.drawPolygon(rombo(n));
        }

        g2.setColor(Color.BLACK);
        dibujarTextoCentrado(g2, n.getTexto(), n.getBounds());
    }

    private Polygon rombo(Nodo n) {
        int x = n.getX(), y = n.getY(), w = n.getAncho(), h = n.getAlto();
        Polygon p = new Polygon();
        p.addPoint(x + w / 2, y);
        p.addPoint(x + w, y + h / 2);
        p.addPoint(x + w / 2, y + h);
        p.addPoint(x, y + h / 2);
        return p;
    }

    private Color segunTipo(TipoNodo tipo) {
        return switch (tipo) {
            case INICIO -> new Color(180, 230, 180);
            case FIN -> new Color(230, 180, 180);
            case PROCESO -> new Color(180, 210, 230);
            case DECISION -> new Color(230, 220, 180);
        };
    }

    private void dibujarTextoCentrado(Graphics2D g2, String texto, java.awt.Rectangle bounds) {
        var fm = g2.getFontMetrics();
        int tx = bounds.x + (bounds.width - fm.stringWidth(texto)) / 2;
        int ty = bounds.y + (bounds.height + fm.getAscent()) / 2 - 3;
        g2.drawString(texto, tx, ty);
    }

    private void dibujarConexion(Graphics2D g2, Conexion c) {
        Point origen = c.getOrigen().getCentro();
        Point destino = c.getDestino().getCentro();

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(origen.x, origen.y, destino.x, destino.y);

        dibujarPuntaFlecha(g2, origen, destino);

        if (c.getEtiqueta() != null && !c.getEtiqueta().isBlank()) {
            int mx = (origen.x + destino.x) / 2;
            int my = (origen.y + destino.y) / 2;
            g2.drawString(c.getEtiqueta(), mx + 4, my - 4);
        }
    }

    private void dibujarPuntaFlecha(Graphics2D g2, Point origen, Point destino) {
        double angulo = Math.atan2(destino.y - origen.y, destino.x - origen.x);
        int largo = 10;
        int x = destino.x - (int) (25 * Math.cos(angulo));
        int y = destino.y - (int) (25 * Math.sin(angulo));

        Polygon punta = new Polygon();
        punta.addPoint(x, y);
        punta.addPoint(
                (int) (x - largo * Math.cos(angulo - Math.PI / 6)),
                (int) (y - largo * Math.sin(angulo - Math.PI / 6)));
        punta.addPoint(
                (int) (x - largo * Math.cos(angulo + Math.PI / 6)),
                (int) (y - largo * Math.sin(angulo + Math.PI / 6)));
        g2.fillPolygon(punta);
    }
}
