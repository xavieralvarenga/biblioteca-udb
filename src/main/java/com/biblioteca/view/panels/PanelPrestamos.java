package com.biblioteca.view.panels;

import com.biblioteca.service.PrestamoService;
import com.biblioteca.view.forms.DialogDetallePrestamo;
import com.biblioteca.view.forms.DialogNuevoPrestamo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelPrestamos extends JPanel {

    private JTable tablaPrestamos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxFiltroEstado;
    // Eliminamos cbxFiltroPago porque los pagos se ven adentro del detalle
    private JButton btnNuevoPrestamo, btnVerDetalles, btnBuscar, btnEditarLector;

    private final PrestamoService prestamoService;

    public PanelPrestamos() {
        this.prestamoService = new PrestamoService();

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. TÍTULO Y BARRA DE BÚSQUEDA ---
        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));
        panelNorte.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Control de Préstamos (Tickets)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);

        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(25);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Carnet de usuario...");
        panelBusqueda.add(txtBuscar);

        panelBusqueda.add(new JLabel("Estado:"));
        cbxFiltroEstado = new JComboBox<>(new String[]{"Todos", "Activo", "Parcial", "Con Deuda", "Finalizado"});
        panelBusqueda.add(cbxFiltroEstado);

        btnBuscar = new JButton("Recargar");
        btnBuscar.setBackground(new Color(61, 90, 128));
        btnBuscar.setForeground(Color.WHITE);
        panelBusqueda.add(btnBuscar);

        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // --- 2. TABLA DE DATOS (Cabecera) ---
        // ¡Solo 6 columnas!
        String[] columnas = {"ID Préstamo", "Carnet", "Lector", "Fecha Préstamo", "Cant. Ítems", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaPrestamos = new JTable(modeloTabla);
        tablaPrestamos.setRowHeight(30);
        tablaPrestamos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaPrestamos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPrestamos.setShowVerticalLines(false);

        aplicarColoresATabla();

        add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);

        // --- 3. BOTONES DE ACCIÓN (Sur) ---
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelSur.setBackground(Color.WHITE);
        btnEditarLector = crearBoton("Cambiar Lector", new Color(108, 117, 125));
        btnVerDetalles = crearBoton("Gestionar / Ver Detalles", new Color(108, 117, 125));
        btnNuevoPrestamo = crearBoton("Nuevo Préstamo", new Color(41, 171, 135));
        panelSur.add(btnEditarLector);
        panelSur.add(btnVerDetalles);
        panelSur.add(btnNuevoPrestamo);

        add(panelSur, BorderLayout.SOUTH);

        cargarPrestamosDesdeBD();
        configurarEventos();
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

    private void aplicarColoresATabla() {
        DefaultTableCellRenderer renderizadorEstado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && !isSelected) {
                    String estado = value.toString();
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    switch (estado) {
                        case "Activo": c.setForeground(new Color(41, 171, 135)); break; // Verde
                        case "Parcial": c.setForeground(new Color(244, 162, 97)); break; // Naranja
                        case "Con Deuda": c.setForeground(new Color(220, 53, 69)); break;
                        case "Finalizado": c.setForeground(new Color(108, 117, 125)); break; // Gris
                        default: c.setForeground(table.getForeground());
                    }
                } else if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        };
        // ¡La columna "Estado" ahora es el índice 5!
        tablaPrestamos.getColumnModel().getColumn(5).setCellRenderer(renderizadorEstado);
    }

    public void cargarPrestamosDesdeBD() {
        modeloTabla.setRowCount(0);
        try {
            List<Object[]> listaPrestamos = prestamoService.obtenerPrestamosCabecera();
            for (Object[] fila : listaPrestamos) {
                modeloTabla.addRow(fila);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la tabla: " + e.getMessage());
        }
    }
    private void filtrarDatos() {
        String texto = txtBuscar.getText().trim();
        String estado = (String) cbxFiltroEstado.getSelectedItem();

        modeloTabla.setRowCount(0); // Limpiar la tabla

        try {
            List<Object[]> listaFiltrada = prestamoService.buscarPrestamosCabeceraConFiltro(texto, estado);
            for (Object[] fila : listaFiltrada) {
                modeloTabla.addRow(fila);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar la tabla: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> filtrarDatos());
        txtBuscar.addActionListener(e -> filtrarDatos()); // Al presionar Enter en la caja de texto
        cbxFiltroEstado.addActionListener(e -> filtrarDatos());
        // Al darle clic recargamos la info para asegurar que los estados sean frescos
        btnBuscar.addActionListener(e -> cargarPrestamosDesdeBD());

        btnNuevoPrestamo.addActionListener(e -> {
            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            DialogNuevoPrestamo dialog = new DialogNuevoPrestamo(ventanaPadre);
            dialog.setVisible(true);
            cargarPrestamosDesdeBD();
        });
        btnEditarLector.addActionListener(e -> {
            int fila = tablaPrestamos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un préstamo."); return;
            }

            int idPrestamo = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
            int cantItems = Integer.parseInt(modeloTabla.getValueAt(fila, 4).toString()); // Cantidad de libros

            try {
                if (!prestamoService.esPrestamoDeHoy(idPrestamo)) {
                    JOptionPane.showMessageDialog(this, "Solo puedes editar el lector el mismo día en que se creó el préstamo.", "Edición Bloqueada", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Abrimos buscador de usuarios
                com.biblioteca.view.forms.DialogBuscarUsuario dialog = new com.biblioteca.view.forms.DialogBuscarUsuario(SwingUtilities.getWindowAncestor(this));
                dialog.setVisible(true);

                com.biblioteca.model.Usuario nuevoUser = dialog.getUsuarioSeleccionado();
                if (nuevoUser != null) {
                    if (nuevoUser.getEstadoMora()) {
                        JOptionPane.showMessageDialog(this, "El nuevo lector tiene mora pendiente.", "Bloqueado", JOptionPane.ERROR_MESSAGE); return;
                    }
                    if (nuevoUser.getTipoUsuario().getMaxLibrosPermitidos() < cantItems) {
                        JOptionPane.showMessageDialog(this, "El nuevo lector solo tiene permitido " + nuevoUser.getTipoUsuario().getMaxLibrosPermitidos() + " libros, y este préstamo tiene " + cantItems + ".", "Límite Excedido", JOptionPane.ERROR_MESSAGE); return;
                    }

                    // Ejecutar cambio
                    prestamoService.cambiarLector(idPrestamo, nuevoUser.getIdUsuario(), nuevoUser.getTipoUsuario().getMaxDiasPrestamo());
                    JOptionPane.showMessageDialog(this, "Lector actualizado exitosamente.");
                    cargarPrestamosDesdeBD();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnVerDetalles.addActionListener(e -> {
            int fila = tablaPrestamos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un préstamo de la tabla para ver sus ítems.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int idPrestamo = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
            String lector = modeloTabla.getValueAt(fila, 2).toString();

            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            // Abrimos el nuevo gestor de detalles
            DialogDetallePrestamo dialog = new DialogDetallePrestamo(ventanaPadre, idPrestamo, lector);
            dialog.setVisible(true);

            // Al cerrar el gestor, recargamos la tabla principal por si cambió a "Parcial" o "Finalizado"
            cargarPrestamosDesdeBD();
        });
    }
}