package com.biblioteca.view.panels;

import com.biblioteca.model.Documento;
import com.biblioteca.service.IDocumentoService;
import com.biblioteca.service.impl.DocumentoServiceImpl;
import com.biblioteca.view.forms.DialogDetalleDocumento;
import com.biblioteca.view.forms.DialogNuevoDocumento;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PanelInventario extends JPanel {

    // Capa de Negocio
    private final IDocumentoService documentoService;

    private JTable tablaDocumentos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxFiltroTipo;
    private JButton btnNuevo, btnEditar, btnEliminar, btnEjemplares;

    public PanelInventario() {
        // 1. Inicialización del Servicio (Manejo de la excepción SQL)
        try {
            this.documentoService = new DocumentoServiceImpl();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error crítico: No se pudo conectar a la base de datos.");
            throw new RuntimeException(e);
        }

        com.biblioteca.model.Usuario usuarioActivo = com.biblioteca.util.SessionManager.getInstance().getUsuarioLogueado();
        int rol = usuarioActivo.getTipoUsuario().getIdTipo();

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Construcción de UI (Panel Norte) ---
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

        JButton btnBuscar = crearBoton("Filtrar", new Color(61, 90, 128));
        panelBusqueda.add(btnBuscar);
        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // --- Configuración de Tabla ---
        String[] columnas = {"ID", "Tipo", "Título", "Autor", "Ubicación", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaDocumentos = new JTable(modeloTabla);
        tablaDocumentos.setRowHeight(30);
        tablaDocumentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaDocumentos);
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel Sur (Botones) ---
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelSur.setBackground(Color.WHITE);

        btnEjemplares = crearBoton("Gestionar Ejemplares", new Color(41, 171, 135));
        btnNuevo = crearBoton("Nuevo Documento", new Color(61, 90, 128));
        btnEditar = crearBoton("Editar", new Color(244, 162, 97));
        btnEliminar = crearBoton("Eliminar", new Color(231, 111, 81));

        panelSur.add(btnEjemplares);
        panelSur.add(btnNuevo);
        panelSur.add(btnEditar);
        if (rol == 1) panelSur.add(btnEliminar);
        add(panelSur, BorderLayout.SOUTH);

        // 2. CARGA DE DATOS REALES
        llenarTablaDesdeBD();
        configurarEventos();
    }

    /**
     * Reemplaza a cargarDatosDePrueba(). Obtiene la lista del Service y la vuelca en la tabla.
     */
    private void llenarTablaDesdeBD() {
        modeloTabla.setRowCount(0); // Limpiar tabla antes de recargar
        try {
            List<Documento> lista = documentoService.listarInventario();
            for (Documento doc : lista) {
                modeloTabla.addRow(new Object[]{
                        doc.getIdDocumento(),
                        doc.getClass().getSimpleName(), // Obtiene "Libro", "Revista" o "Cd"
                        doc.getTitulo(),
                        doc.getAutor(),
                        doc.getUbicacionFisica(),
                        doc.getEstado()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage());
        }
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> {
            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            DialogNuevoDocumento dialog = new DialogNuevoDocumento(ventanaPadre);
            dialog.setVisible(true);
            llenarTablaDesdeBD(); // Refrescar tabla al volver
        });

        btnEditar.addActionListener(e -> {
            int filaSeleccionada = tablaDocumentos.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un documento.");
                return;
            }
            // Recuperar datos de la fila y abrir detalle...
            // (Tu lógica de DialogDetalleDocumento se mantiene igual)
        });
    }

    private JButton crearBoton(String texto, Color colorFondo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorFondo);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
}