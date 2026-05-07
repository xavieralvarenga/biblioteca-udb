package com.biblioteca.view.panels;

import com.biblioteca.model.Documento;
import com.biblioteca.service.IDocumentoService;
import com.biblioteca.service.impl.DocumentoServiceImpl;
import com.biblioteca.view.forms.DialogNuevoDocumento;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PanelInventario extends JPanel {

    // Capa de Negocio (Service)
    private final IDocumentoService documentoService;

    // Componentes de la Interfaz
    private JTable tablaDocumentos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxFiltroTipo;
    private JButton btnNuevo, btnEditar, btnEliminar, btnEjemplares;

    public PanelInventario() throws SQLException {
        // 1. INICIALIZACIÓN DE DEPENDENCIAS (Crítico: Debe ir primero)
        // Esto evita el NullPointerException al llamar a llenarTablaDesdeBD()
        this.documentoService = new DocumentoServiceImpl();

        // 2. CONFIGURACIÓN DEL PANEL Y LAYOUT
        setLayout(new BorderLayout(10, 10)); // Espaciado entre componentes
        setBackground(Color.WHITE); // Fondo limpio
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Márgenes para que respire el diseño

        // 3. CONSTRUCCIÓN DE LA PARTE NORTE (Título y Buscador)
        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));
        panelNorte.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Gestión de Inventario (Documentos)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);

        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(20);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Título, autor o código...");
        panelBusqueda.add(txtBuscar);

        panelBusqueda.add(new JLabel("Tipo:"));
        cbxFiltroTipo = new JComboBox<>(new String[]{"Todos", "Libro", "Revista", "CD"});
        panelBusqueda.add(cbxFiltroTipo);

        JButton btnFiltrar = crearBoton("Filtrar", new Color(61, 90, 128));
        panelBusqueda.add(btnFiltrar);

        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // 4. CONFIGURACIÓN DE LA TABLA (Centro)
        String[] columnas = {"ID", "Tipo", "Título", "Autor", "Ubicación", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Desactivar edición directa en celdas
            }
        };

        tablaDocumentos = new JTable(modeloTabla);
        tablaDocumentos.setRowHeight(30); // Filas amplias para estilo moderno
        tablaDocumentos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaDocumentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Solo una fila a la vez

        JScrollPane scrollPane = new JScrollPane(tablaDocumentos);
        add(scrollPane, BorderLayout.CENTER);

        // 5. CONSTRUCCIÓN DE LA PARTE SUR (Botones de Acción)
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelSur.setBackground(Color.WHITE);

        btnEjemplares = crearBoton("Gestionar Ejemplares", new Color(41, 171, 135));
        btnNuevo = crearBoton("Nuevo Documento", new Color(61, 90, 128));
        btnEditar = crearBoton("Editar", new Color(244, 162, 97));
        btnEliminar = crearBoton("Eliminar", new Color(231, 111, 81));

        panelSur.add(btnEjemplares);
        panelSur.add(btnNuevo);
        panelSur.add(btnEditar);
        panelSur.add(btnEliminar);

        add(panelSur, BorderLayout.SOUTH);

        // 6. CARGA DE DATOS REALES DESDE LA BASE DE DATOS
        // Ahora que service no es null y modeloTabla existe, podemos llamar al método
        llenarTablaDesdeBD();

        // 7. ASIGNACIÓN DE EVENTOS
        configurarEventos();
    }

    /**
     * Recupera los datos del catálogo llamando al Service.
     */
    private void llenarTablaDesdeBD() {
        // Limpiamos el modelo actual para no duplicar filas
        modeloTabla.setRowCount(0);

        try {
            // Obtenemos la lista polimórfica (Libros, Revistas, CDs)
            List<Documento> documentos = documentoService.listarInventario();

            // Llenamos el modelo con los datos reales
            for (Documento doc : documentos) {
                modeloTabla.addRow(new Object[]{
                        doc.getIdDocumento(),
                        doc.getClass().getSimpleName(), // Nombre de la clase (Libro, Revista, etc.)
                        doc.getTitulo(),
                        doc.getAutor(),
                        doc.getUbicacionFisica(),
                        doc.getEstado()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage());
        }
    }

    private void configurarEventos() {
        // Evento para abrir el diálogo de creación
        btnNuevo.addActionListener(e -> {
            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            DialogNuevoDocumento dialog = new DialogNuevoDocumento(ventanaPadre);
            dialog.setVisible(true);
            llenarTablaDesdeBD(); // Refrescar al cerrar el diálogo
        });

//        // Evento para eliminar un registro
//        btnEliminar.addActionListener(e -> {
//            int fila = tablaDocumentos.getSelectedRow();
//            if (fila == -1) {
//                JOptionPane.showMessageDialog(this, "Selecciona un documento para eliminar.");
//                return;
//            }
//
//            // Obtenemos el ID de la primera columna
//            int id = (int) modeloTabla.getValueAt(fila, 0);
//            int confirmar = JOptionPane.showConfirmDialog(this, "¿Eliminar documento ID: " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
//
//            if (confirmar == JOptionPane.YES_OPTION) {
//                if (documentoService.darDeBajaDocumento(id)) {
//                    JOptionPane.showMessageDialog(this, "Documento eliminado con éxito.");
//                    llenarTablaDesdeBD(); // Actualizar vista
//                } else {
//                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el documento.");
//                }
//            }
//        });
    }

    // Método auxiliar para crear botones estilizados con FlatLaf en mente
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