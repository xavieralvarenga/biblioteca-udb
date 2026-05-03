package com.biblioteca.view.panels;

import com.biblioteca.view.forms.DialogDetalleDocumento;
import com.biblioteca.view.forms.DialogNuevoDocumento;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelInventario extends JPanel {

    private JTable tablaDocumentos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxFiltroTipo;
    private JButton btnNuevo, btnEditar, btnEliminar, btnEjemplares;

    public PanelInventario() {
        // Configuramos el layout principal de este panel
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Márgenes internos

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

        JButton btnBuscar = new JButton("Filtrar");
        btnBuscar.setBackground(new Color(61, 90, 128));
        btnBuscar.setForeground(Color.WHITE);
        panelBusqueda.add(btnBuscar);

        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);
        //Tabla de datos
        String[] columnas = {"ID", "Tipo", "Título", "Autor", "Ubicación", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDocumentos = new JTable(modeloTabla);
        tablaDocumentos.setRowHeight(30); // Filas más altas para diseño moderno
        tablaDocumentos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaDocumentos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaDocumentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configuraciones de diseño de FlatLaf para tablas
        tablaDocumentos.setShowVerticalLines(false);
        tablaDocumentos.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(tablaDocumentos);
        add(scrollPane, BorderLayout.CENTER);

        // 3. botones
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
        cargarDatosDePrueba();
        configurarEventos();
    }

    // Método auxiliar para crear botones estilizados
    private JButton crearBoton(String texto, Color colorFondo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorFondo);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private void cargarDatosDePrueba() {
        modeloTabla.addRow(new Object[]{"1", "Libro", "El Señor de los Anillos", "J.R.R. Tolkien", "Estante A1", "Disponible"});
        modeloTabla.addRow(new Object[]{"2", "Revista", "National Geographic - Mayo", "NatGeo", "Hemeroteca", "Prestado"});
        modeloTabla.addRow(new Object[]{"3", "CD", "Sinfonía No. 9", "Beethoven", "Multimedia", "Disponible"});
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> {
            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            DialogNuevoDocumento dialog = new DialogNuevoDocumento(ventanaPadre);
            dialog.setVisible(true);
        });

        btnEditar.addActionListener(e -> {
            int filaSeleccionada = tablaDocumentos.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona un documento de la tabla primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            } else {
                String id = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
                String tipo = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
                String titulo = (String) modeloTabla.getValueAt(filaSeleccionada, 2);
                String autor = (String) modeloTabla.getValueAt(filaSeleccionada, 3);
                String ubicacion = (String) modeloTabla.getValueAt(filaSeleccionada, 4);
                String estado = (String) modeloTabla.getValueAt(filaSeleccionada, 5);

                Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
                DialogDetalleDocumento dialogDetalle = new DialogDetalleDocumento(
                        ventanaPadre, id, tipo, titulo, autor, ubicacion, estado
                );
                dialogDetalle.setVisible(true);
            }
        });
    }
}