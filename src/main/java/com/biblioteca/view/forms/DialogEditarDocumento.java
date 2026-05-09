package com.biblioteca.view.forms;

import com.biblioteca.model.*;
import com.biblioteca.service.IDocumentoService;
import com.biblioteca.service.impl.DocumentoServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo modal para la edición de documentos existentes (Libros, Revistas, CDs).
 * Permite modificar tanto datos generales como específicos según el tipo de documento.
 */
public class DialogEditarDocumento extends JDialog {

    // Campos generales
    private JTextField txtTitulo, txtAutor, txtUbicacion, txtCodigoBarras;
    private JComboBox<String> cbxEstado;

    // Campos específicos para Libro
    private JTextField txtIsbn, txtEditorial, txtEdicion;
    // Campos específicos para Revista
    private JTextField txtIssn, txtVolumen, txtMes;
    // Campos específicos para CD
    private JTextField txtDuracion, txtContenido;

    private final Documento documento;
    private JPanel panelDetallesDinamico;
    private CardLayout cardLayout;

    /**
     * Constructor del diálogo de edición.
     * @param parent Ventana de la cual depende este diálogo.
     * @param documento El objeto documento que se desea editar.
     */
    public DialogEditarDocumento(Window parent, Documento documento) {
        super(parent, "Editar Documento - ID: " + documento.getIdDocumento(), ModalityType.APPLICATION_MODAL);
        this.documento = documento;

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        cargarDatos();
    }

    /**
     * Inicializa los componentes de la interfaz gráfica.
     */
    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Sección de Datos Generales
        JPanel panelGeneral = new JPanel(new GridLayout(5, 2, 10, 15));
        panelGeneral.setBackground(Color.WHITE);
        panelGeneral.setBorder(BorderFactory.createTitledBorder("Datos Generales"));

        txtTitulo = crearCampo(panelGeneral, "Título *:");
        txtAutor = crearCampo(panelGeneral, "Autor *:");
        txtUbicacion = crearCampo(panelGeneral, "Ubicación:");
        txtCodigoBarras = crearCampo(panelGeneral, "Cód. Barras:");

        cbxEstado = new JComboBox<>(new String[]{"Disponible", "Prestado", "En Mantenimiento", "Baja"});
        panelGeneral.add(new JLabel(" Estado:"));
        panelGeneral.add(cbxEstado);

        mainPanel.add(panelGeneral);

        // Sección Dinámica según el tipo de objeto (CardLayout)
        cardLayout = new CardLayout();
        panelDetallesDinamico = new JPanel(cardLayout);

        // Paneles específicos
        panelDetallesDinamico.add(crearPanelLibro(), "Libro");
        panelDetallesDinamico.add(crearPanelRevista(), "Revista");
        panelDetallesDinamico.add(crearPanelCd(), "Cd");

        mainPanel.add(panelDetallesDinamico);
        add(mainPanel, BorderLayout.CENTER);

        // Panel de Botones
        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar Cambios");

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarCambios());

        pBotones.add(btnCancelar);
        pBotones.add(btnGuardar);
        add(pBotones, BorderLayout.SOUTH);
    }

    /**
     * Llena los campos de texto con los datos actuales del objeto documento.
     */
    private void cargarDatos() {
        // Carga de datos comunes
        txtTitulo.setText(documento.getTitulo());
        txtAutor.setText(documento.getAutor());
        txtUbicacion.setText(documento.getUbicacionFisica());
        txtCodigoBarras.setText(documento.getCodigoBarrasObra());
        cbxEstado.setSelectedItem(documento.getEstado());

        // Carga de datos específicos mediante casting y cambio de vista en CardLayout
        String tipoClase = documento.getClass().getSimpleName();
        cardLayout.show(panelDetallesDinamico, tipoClase);

        if (documento instanceof Libro) {
            Libro l = (Libro) documento;
            txtIsbn.setText(l.getIsbn());
            txtEditorial.setText(l.getEditorial());
            txtEdicion.setText(l.getEdicion());
        } else if (documento instanceof Revista) {
            Revista r = (Revista) documento;
            txtIssn.setText(r.getIssn());
            txtVolumen.setText(r.getVolumen());
            txtMes.setText(r.getMesPublicacion());
        } else if (documento instanceof Cd) {
            Cd c = (Cd) documento;
            txtDuracion.setText(String.valueOf(c.getDuracionMinutos()));
            txtContenido.setText(c.getTipoContenido());
        }
    }

    /**
     * Procesa la actualización del documento llamando a la capa de servicio.
     */
    private void guardarCambios() {
        try {
            // Actualizar datos comunes en el objeto existente
            documento.setTitulo(txtTitulo.getText().trim());
            documento.setAutor(txtAutor.getText().trim());
            documento.setUbicacionFisica(txtUbicacion.getText().trim());
            documento.setCodigoBarrasObra(txtCodigoBarras.getText().trim());
            documento.setEstado((String) cbxEstado.getSelectedItem());

            // Actualizar datos específicos según instancia
            if (documento instanceof Libro) {
                Libro l = (Libro) documento;
                l.setIsbn(txtIsbn.getText().trim());
                l.setEditorial(txtEditorial.getText().trim());
                l.setEdicion(txtEdicion.getText().trim());
            } else if (documento instanceof Revista) {
                Revista r = (Revista) documento;
                r.setIssn(txtIssn.getText().trim());
                r.setVolumen(txtVolumen.getText().trim());
                r.setMesPublicacion(txtMes.getText().trim());
            } else if (documento instanceof Cd) {
                Cd c = (Cd) documento;
                c.setDuracionMinutos(Integer.parseInt(txtDuracion.getText().isEmpty() ? "0" : txtDuracion.getText()));
                c.setTipoContenido(txtContenido.getText().trim());
            }

            // Invocar al servicio para persistir cambios
            IDocumentoService service = new DocumentoServiceImpl();
            // Nota: Debes tener implementado el método actualizarDocumento en tu Service y DAO
            if (service.actualizarDocumento(documento)) {
                JOptionPane.showMessageDialog(this, "Documento actualizado con éxito");
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Métodos auxiliares de creación de UI ---

    private JTextField crearCampo(JPanel p, String label) {
        p.add(new JLabel(label));
        JTextField t = new JTextField();
        p.add(t);
        return t;
    }

    private JPanel crearPanelLibro() {
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Detalles de Libro"));
        txtIsbn = crearCampo(p, "ISBN:");
        txtEditorial = crearCampo(p, "Editorial:");
        txtEdicion = crearCampo(p, "Edición:");
        return p;
    }

    private JPanel crearPanelRevista() {
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Detalles de Revista"));
        txtIssn = crearCampo(p, "ISSN:");
        txtVolumen = crearCampo(p, "Volumen:");
        txtMes = crearCampo(p, "Mes Pub:");
        return p;
    }

    private JPanel crearPanelCd() {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Detalles de CD"));
        txtDuracion = crearCampo(p, "Duración (min):");
        txtContenido = crearCampo(p, "Contenido:");
        return p;
    }
}