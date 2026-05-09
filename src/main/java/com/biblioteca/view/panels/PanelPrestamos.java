package com.biblioteca.view.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelPrestamos extends JPanel {

    private JTable tablaPrestamos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxFiltroEstado;
    private JComboBox<String> cbxFiltroPago;
    private JButton btnNuevoPrestamo, btnDevolucion, btnVerDetalles;
    private JButton btnBuscar;

    public PanelPrestamos() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. TÍTULO Y BARRA DE BÚSQUEDA (Norte) ---
        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));
        panelNorte.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Control y Historial de Préstamos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);

        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(25);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Carnet de usuario o Código de Ejemplar...");
        panelBusqueda.add(txtBuscar);

        panelBusqueda.add(new JLabel("Estado:"));
        cbxFiltroEstado = new JComboBox<>(new String[]{"Todos", "Activos", "Vencidos", "Devueltos"});
        panelBusqueda.add(cbxFiltroEstado);

        panelBusqueda.add(new JLabel("Pago:"));
        cbxFiltroPago = new JComboBox<>(new String[]{"Todos", "Pendiente", "Pagado", "N/A"});
        panelBusqueda.add(cbxFiltroPago);

        btnBuscar = new JButton("Filtrar");
        btnBuscar.setBackground(new Color(61, 90, 128));
        btnBuscar.setForeground(Color.WHITE);
        panelBusqueda.add(btnBuscar);

        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // --- 2. TABLA DE DATOS (Centro) ---
        // Basado en tu diagrama ER: Unimos datos de Prestamo, Usuario, Ejemplar y Documento
        String[] columnas = {"ID", "Carnet", "Lector", "Cód. Ejemplar", "Título", "Fecha Préstamo", "Fecha Límite", "Estado", "Pago"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        tablaPrestamos = new JTable(modeloTabla);
        tablaPrestamos.setRowHeight(30);
        tablaPrestamos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaPrestamos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPrestamos.setShowVerticalLines(false);

        aplicarColoresATabla(); // Aplicamos nuestro renderizador de colores

        JScrollPane scrollPane = new JScrollPane(tablaPrestamos);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. BOTONES DE ACCIÓN (Sur) ---
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelSur.setBackground(Color.WHITE);

        btnVerDetalles = crearBoton("Ver Detalles", new Color(108, 117, 125));
        btnDevolucion = crearBoton("Registrar Devolución", new Color(244, 162, 97)); // Naranja
        btnNuevoPrestamo = crearBoton("Nuevo Préstamo", new Color(41, 171, 135)); // Verde

        panelSur.add(btnVerDetalles);
        panelSur.add(btnDevolucion);
        panelSur.add(btnNuevoPrestamo);

        add(panelSur, BorderLayout.SOUTH);

        // Cargar datos de prueba iniciales
        cargarPrestamosDesdeBD();

        // Eventos
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
        // --- 1. Renderizador para la columna "Estado" (Índice 7) ---
        DefaultTableCellRenderer renderizadorEstado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && !isSelected) {
                    String estado = value.toString();
                    c.setFont(c.getFont().deriveFont(Font.BOLD));

                    switch (estado) {
                        case "Activo":
                            c.setForeground(new Color(41, 171, 135)); // Verde
                            break;
                        case "Vencido":
                            c.setForeground(new Color(220, 53, 69)); // Rojo fuego
                            break;
                        case "Devuelto":
                            c.setForeground(new Color(108, 117, 125)); // Gris (ya no importa tanto)
                            break;
                        default:
                            c.setForeground(table.getForeground());
                    }
                } else if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        };
        // Aplicamos el color solo a la columna "Estado" (Índice 7)
        tablaPrestamos.getColumnModel().getColumn(7).setCellRenderer(renderizadorEstado);

        // --- 2. Renderizador para la nueva columna "Pago" (Índice 8) ---
        DefaultTableCellRenderer renderizadorPago = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && !isSelected) {
                    String pago = value.toString();
                    c.setFont(c.getFont().deriveFont(Font.BOLD));

                    switch (pago) {
                        case "Pendiente":
                            c.setForeground(new Color(220, 53, 69)); // Rojo fuego para alertar la deuda
                            break;
                        case "Pagado":
                            c.setForeground(new Color(41, 171, 135)); // Verde
                            break;
                        case "N/A":
                        default:
                            c.setForeground(new Color(108, 117, 125)); // Gris para los que no aplican
                            break;
                    }
                } else if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        };
        // Aplicamos el color solo a la nueva columna "Pago" (Índice 8)
        tablaPrestamos.getColumnModel().getColumn(8).setCellRenderer(renderizadorPago);
    }

    public void cargarPrestamosDesdeBD() {
        modeloTabla.setRowCount(0); // Limpiamos la tabla

        try {
            com.biblioteca.repository.impl.PrestamoDAO prestamoDAO = new com.biblioteca.repository.impl.PrestamoDAO();
            java.util.List<Object[]> listaPrestamos = prestamoDAO.obtenerTodosLosPrestamos();

            for (Object[] fila : listaPrestamos) {
                modeloTabla.addRow(fila);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la tabla de préstamos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void filtrarDatos() {
        String texto = txtBuscar.getText().trim();
        String estado = (String) cbxFiltroEstado.getSelectedItem();
        String estadoPago = (String) cbxFiltroPago.getSelectedItem();

        modeloTabla.setRowCount(0); // Limpiar la tabla antes de rellenar

        try {
            com.biblioteca.repository.impl.PrestamoDAO prestamoDAO = new com.biblioteca.repository.impl.PrestamoDAO();
            java.util.List<Object[]> listaFiltrada = prestamoDAO.buscarPrestamosConFiltro(texto, estado,estadoPago);

            for (Object[] fila : listaFiltrada) {
                modeloTabla.addRow(fila);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar la tabla: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> filtrarDatos()); // <-- Usamos la variable directa
        txtBuscar.addActionListener(e -> filtrarDatos());
        cbxFiltroEstado.addActionListener(e -> filtrarDatos());
        cbxFiltroPago.addActionListener(e -> filtrarDatos());

        // Evento para cambiar el botón según la deuda
        tablaPrestamos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaPrestamos.getSelectedRow();
            if (fila != -1) {
                String estado = modeloTabla.getValueAt(fila, 7).toString();
                // Si ya está devuelto, cambiamos el texto del botón para que sirva para cobrar
                if ("Devuelto".equals(estado)) {
                    btnDevolucion.setText("Cobrar Mora");
                    btnDevolucion.setBackground(new Color(61, 90, 128)); // Azul
                } else {
                    btnDevolucion.setText("Registrar Devolución");
                    btnDevolucion.setBackground(new Color(244, 162, 97)); // Naranja
                }
            }
        });


        btnNuevoPrestamo.addActionListener(e -> {
            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            // Asegúrate de importar com.biblioteca.view.forms.DialogNuevoPrestamo si te lo pide
            com.biblioteca.view.forms.DialogNuevoPrestamo dialog = new com.biblioteca.view.forms.DialogNuevoPrestamo(ventanaPadre);
            dialog.setVisible(true);

            cargarPrestamosDesdeBD();

            // Aquí pondremos el cargarPrestamosDesdeBD() más adelante
        });

        btnDevolucion.addActionListener(e -> {
            int fila = tablaPrestamos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un préstamo de la tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // 1. Extraer datos básicos de forma ultra-segura
                int idPrestamo = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
                String carnet = modeloTabla.getValueAt(fila, 1).toString();
                String titulo = modeloTabla.getValueAt(fila, 4).toString();
                String estado = modeloTabla.getValueAt(fila, 7).toString();

                com.biblioteca.repository.impl.PrestamoDAO dao = new com.biblioteca.repository.impl.PrestamoDAO();

                // --- CASO 1: EL LIBRO YA SE DEVOLVIÓ (Gestión de pagos pendientes) ---
                if ("Devuelto".equals(estado) || btnDevolucion.getText().equals("Cobrar Mora")) {

                    // Consultamos rápido los saldos para este préstamo específico
                    String sqlSaldos = "SELECT id_usuario, monto_calculado, monto_pagado FROM Prestamo WHERE id_prestamo = ?";
                    try (java.sql.Connection con = com.biblioteca.config.DatabaseConnection.getConnection();
                         java.sql.PreparedStatement ps = con.prepareStatement(sqlSaldos)) {

                        ps.setInt(1, idPrestamo);
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                int idUsuario = rs.getInt("id_usuario");
                                double calculado = rs.getDouble("monto_calculado");
                                double pagado = rs.getDouble("monto_pagado");

                                if (calculado > pagado) {
                                    // Si aún debe, abrimos la ventanita de cobrar
                                    Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
                                    com.biblioteca.view.forms.DialogPagarMora dialog = new com.biblioteca.view.forms.DialogPagarMora(
                                            ventanaPadre, idPrestamo, idUsuario, carnet, calculado, pagado
                                    );
                                    dialog.setVisible(true);
                                    cargarPrestamosDesdeBD(); // Refrescar tabla al cerrar
                                } else {
                                    JOptionPane.showMessageDialog(this, "Este préstamo ya está devuelto y está 100% solvente.", "Información", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                    }
                    return; // Terminamos aquí para que no ejecute el código de abajo
                }

                // --- CASO 2: EL LIBRO AÚN LO TIENE EL LECTOR (Proceso Normal de Devolución) ---
                Object[] datosExtra = dao.obtenerDatosParaDevolucion(idPrestamo);

                if (datosExtra != null) {
                    int idEjemplar = (int) datosExtra[0];
                    java.time.LocalDate fechaLimite = (java.time.LocalDate) datosExtra[1];
                    double valorMora = (double) datosExtra[2];
                    int idUsuario = (int) datosExtra[3];

                    // Abrir la ventana de devolución estándar
                    Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
                    com.biblioteca.view.forms.DialogDevolucion dialog = new com.biblioteca.view.forms.DialogDevolucion(
                            ventanaPadre, idPrestamo, idUsuario, carnet, titulo, fechaLimite, idEjemplar, valorMora
                    );
                    dialog.setVisible(true);

                    // Actualizar tabla al terminar
                    cargarPrestamosDesdeBD();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudieron recuperar los detalles técnicos del préstamo.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error crítico de Base de Datos:\n" + ex.getMessage(), "Falla al procesar", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }
}