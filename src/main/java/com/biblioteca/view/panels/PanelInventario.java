package com.biblioteca.view.panels;

import com.biblioteca.model.Documento;
import com.biblioteca.service.IDocumentoService;
import com.biblioteca.service.impl.DocumentoServiceImpl;
import com.biblioteca.view.forms.DialogEditarDocumento;
import com.biblioteca.view.forms.DialogGestionarEjemplares;
import com.biblioteca.view.forms.DialogNuevoDocumento;
import com.biblioteca.view.forms.DialogEditarDocumento;
import com.biblioteca.view.forms.DialogGestionarEjemplares;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel principal para la gestión del inventario de documentos.
 * Proporciona funcionalidades de visualización, filtrado, creación,
 * edición y eliminación de libros, revistas y CDs.
 */
public class PanelInventario extends JPanel {

    /** Capa de servicio para la lógica de negocio de documentos */
    private final IDocumentoService documentoService;

    private JTable tablaDocumentos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxFiltroTipo;
    private JButton btnNuevo, btnEditar, btnEliminar, btnEjemplares;

    /**
     * Constructor del panel. Inicializa el servicio y construye la interfaz.
     */
    public PanelInventario() {
        // Inicialización del servicio con manejo de excepción de conexión
        try {
            this.documentoService = new DocumentoServiceImpl();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + e.getMessage());
            throw new RuntimeException(e);
        }

        // Obtención del rol del usuario para control de acceso (RBAC)
        com.biblioteca.model.Usuario usuarioActivo = com.biblioteca.util.SessionManager.getInstance().getUsuarioLogueado();
        int rol = (usuarioActivo != null) ? usuarioActivo.getTipoUsuario().getIdTipo() : 0;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents(rol);
        llenarTablaDesdeBD();
        configurarEventos();
    }

    /**
     * Inicializa y organiza los componentes visuales del panel.
     * @param rol El ID del rol del usuario actual para habilitar/deshabilitar acciones.
     */
    private void initComponents(int rol) {
        // --- Panel Norte: Título y Buscador ---
        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));
        panelNorte.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Gestión de Inventario (Documentos)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(20);
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(new JLabel("Tipo:"));
        cbxFiltroTipo = new JComboBox<>(new String[]{"Todos", "Libro", "Revista", "CD"});
        panelBusqueda.add(cbxFiltroTipo);
        panelBusqueda.add(crearBoton("Filtrar", new Color(61, 90, 128)));

        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // --- Panel Centro: Tabla de Datos ---
        String[] columnas = {"ID", "Tipo", "Título", "Autor", "Ubicación", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaDocumentos = new JTable(modeloTabla);
        tablaDocumentos.setRowHeight(30);
        tablaDocumentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tablaDocumentos), BorderLayout.CENTER);

        // --- Panel Sur: Acciones ---
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelSur.setBackground(Color.WHITE);

        btnEjemplares = crearBoton("Gestionar Ejemplares", new Color(41, 171, 135));
        btnNuevo = crearBoton("Nuevo Documento", new Color(61, 90, 128));
        btnEditar = crearBoton("Editar", new Color(244, 162, 97));
        btnEliminar = crearBoton("Eliminar", new Color(231, 111, 81));

        panelSur.add(btnEjemplares);
        panelSur.add(btnNuevo);
        panelSur.add(btnEditar);

        // Solo el administrador (Rol 1) puede eliminar
        if (rol == 1) panelSur.add(btnEliminar);

        add(panelSur, BorderLayout.SOUTH);
    }

    /**
     * Consulta la base de datos mediante el servicio y actualiza el modelo de la tabla.
     */
    private void llenarTablaDesdeBD() {
        modeloTabla.setRowCount(0);
        try {
            List<Documento> lista = documentoService.listarInventario();
            for (Documento doc : lista) {
                modeloTabla.addRow(new Object[]{
                        doc.getIdDocumento(),
                        doc.getClass().getSimpleName(),
                        doc.getTitulo(),
                        doc.getAutor(),
                        doc.getUbicacionFisica(),
                        doc.getEstado()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la tabla: " + e.getMessage());
        }
    }

    /**
     * Busca el objeto Documento completo en la lista de inventario.
     * @param fila Índice de la fila seleccionada en la tabla.
     * @return El objeto Documento correspondiente al ID de la fila.
     */
    private Documento obtenerDocDesdeFila(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        return documentoService.listarInventario().stream()
                .filter(d -> d.getIdDocumento() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Configura los escuchadores de eventos para los botones de acción.
     */
    private void configurarEventos() {
        // Evento Crear Nuevo
        btnNuevo.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            new DialogNuevoDocumento(win).setVisible(true);
            llenarTablaDesdeBD();
        });

        // Evento Editar Existente
        btnEditar.addActionListener(e -> {
            int fila = tablaDocumentos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un documento de la tabla.");
                return;
            }
            Documento doc = obtenerDocDesdeFila(fila);
            if (doc != null) {
                new DialogEditarDocumento(SwingUtilities.getWindowAncestor(this), doc).setVisible(true);
                llenarTablaDesdeBD();
            }
        });

        // Evento Gestionar Ejemplares
        btnEjemplares.addActionListener(e -> {
            int fila = tablaDocumentos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un documento de la tabla primero.");
                return;
            }

            // Obtenemos los datos directamente del modelo para evitar errores de casting
            Integer idDoc = (Integer) modeloTabla.getValueAt(fila, 0);
            String titulo = (String) modeloTabla.getValueAt(fila, 2);

            // Abrimos el diálogo enviando el dueño (ventana padre), el ID y el título
            Window win = SwingUtilities.getWindowAncestor(this);
            DialogGestionarEjemplares dialog = new DialogGestionarEjemplares(win, idDoc, titulo);
            dialog.setVisible(true);

            // Refrescamos por si cambió el estado general del documento
            llenarTablaDesdeBD();
        });

        // Evento Eliminar
        btnEliminar.addActionListener(e -> {
            int fila = tablaDocumentos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un documento de la tabla.");
                return;
            }

            int id = (int) modeloTabla.getValueAt(fila, 0);
            String titulo = (String) modeloTabla.getValueAt(fila, 2);

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de que desea eliminar el documento: '" + titulo + "'?\nEsta acción no se puede deshacer.",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                if (documentoService.darDeBajaDocumento(id)) {
                    JOptionPane.showMessageDialog(this, "El documento ha sido eliminado con éxito.");
                    llenarTablaDesdeBD();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar. Verifique si el documento está prestado.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Genera un botón con estilo uniforme.
     * @param texto Etiqueta del botón.
     * @param color Color de fondo.
     * @return JButton configurado.
     */
    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
}