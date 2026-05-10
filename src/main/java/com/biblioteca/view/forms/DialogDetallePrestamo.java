package com.biblioteca.view.forms;

import com.biblioteca.service.PrestamoService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DialogDetallePrestamo extends JDialog {

    private int idPrestamo;
    private JTable tablaDetalles;
    private DefaultTableModel modeloDetalles;
    private JButton btnDevolver, btnCobrar;
    private JButton btnCambiarMaterial;
    private PrestamoService service;

    public DialogDetallePrestamo(Window owner, int idPrestamo, String lector) {
        super(owner, "Gestión de Préstamo #" + idPrestamo + " - " + lector, ModalityType.APPLICATION_MODAL);
        this.idPrestamo = idPrestamo;
        this.service = new PrestamoService();

        setSize(750, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Tabla de detalles
        String[] columnas = {"ID Detalle", "Cód. Barras", "Título", "Fecha Límite", "Estado", "Pago Mora"};
        modeloDetalles = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDetalles = new JTable(modeloDetalles);
        mainPanel.add(new JScrollPane(tablaDetalles), BorderLayout.CENTER);

        // Botones de acción
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.setBackground(Color.WHITE);
        btnCambiarMaterial = new JButton("Cambiar Material");
        btnCobrar = new JButton("Cobrar Mora");
        btnDevolver = new JButton("Registrar Devolución");
        btnDevolver.setBackground(new Color(41, 171, 135));
        btnDevolver.setForeground(Color.WHITE);

        panelSur.add(btnCambiarMaterial);
        panelSur.add(btnCobrar);
        panelSur.add(btnDevolver);
        mainPanel.add(panelSur, BorderLayout.SOUTH);

        com.biblioteca.model.Usuario user = com.biblioteca.util.SessionManager.getInstance().getUsuarioLogueado();
        if (user != null && user.getTipoUsuario().getIdTipo() != 1) {
            // Si NO es Administrador, ocultamos todo el panel de botones de abajo
            panelSur.setVisible(false);
        }
        add(mainPanel);

        cargarDetalles();
        configurarEventos();
    }

    private void cargarDetalles() {
        modeloDetalles.setRowCount(0);
        List<Object[]> detalles = service.obtenerDetallesPorPrestamo(idPrestamo);
        for (Object[] fila : detalles) modeloDetalles.addRow(fila);
    }

    private void configurarEventos() {
        btnDevolver.addActionListener(e -> {
            int fila = tablaDetalles.getSelectedRow();
            if (fila == -1) return;

            String estado = modeloDetalles.getValueAt(fila, 4).toString();
            if ("Devuelto".equals(estado)) {
                JOptionPane.showMessageDialog(this, "Este ítem ya fue devuelto.");
                return;
            }

            try {
                int idDetalle = (int) modeloDetalles.getValueAt(fila, 0);
                Object[] datosExtra = service.obtenerDatosParaDevolucion(idDetalle);

                int idEjemplar = (int) datosExtra[0];
                java.time.LocalDate fechaLimite = (java.time.LocalDate) datosExtra[1];
                double valorMora = (double) datosExtra[2];
                int idUsuario = (int) datosExtra[3];

                DialogDevolucion dialog = new DialogDevolucion(this, idDetalle, idUsuario, "N/A",
                        modeloDetalles.getValueAt(fila, 2).toString(), fechaLimite, idEjemplar, valorMora);
                dialog.setVisible(true);
                cargarDetalles(); // Refrescar tabla interna
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnCambiarMaterial.addActionListener(e -> {
            int fila = tablaDetalles.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona el ítem que deseas cambiar."); return;
            }

            try {
                if (!service.esPrestamoDeHoy(idPrestamo)) { // Usamos la variable idPrestamo global de esta ventana
                    JOptionPane.showMessageDialog(this, "Solo puedes cambiar materiales el mismo día en que se creó el préstamo.", "Edición Bloqueada", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idDetalle = (int) modeloDetalles.getValueAt(fila, 0);
                String estadoItem = modeloDetalles.getValueAt(fila, 4).toString();

                if ("Devuelto".equals(estadoItem)) {
                    JOptionPane.showMessageDialog(this, "No puedes cambiar un material que ya fue devuelto."); return;
                }

                // Extraemos el ID del ejemplar actual
                Object[] datosExtra = service.obtenerDatosParaDevolucion(idDetalle);
                int idEjemplarAntiguo = (int) datosExtra[0];

                // Abrimos el buscador para elegir el nuevo
                DialogBuscarEjemplar dialog = new DialogBuscarEjemplar(this);
                dialog.setVisible(true);

                int idEjemplarNuevo = dialog.getIdEjemplarSeleccionado();
                if (idEjemplarNuevo != -1) {
                    if (!"Disponible".equalsIgnoreCase(dialog.getEstadoSeleccionado())) {
                        JOptionPane.showMessageDialog(this, "El nuevo material seleccionado no está disponible."); return;
                    }

                    // Ejecutar cambio
                    service.cambiarEjemplar(idDetalle, idEjemplarAntiguo, idEjemplarNuevo);
                    JOptionPane.showMessageDialog(this, "Material intercambiado exitosamente.");
                    cargarDetalles(); // Recargar la tablita
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // --- LÓGICA DEL BOTÓN COBRAR MORA ---
        btnCobrar.addActionListener(e -> {
            int fila = tablaDetalles.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un ítem de la tabla para cobrar mora.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Verificamos si realmente debe dinero leyendo la columna "Pago Mora" (Índice 5)
            String estadoPago = modeloDetalles.getValueAt(fila, 5).toString();
            if ("Pagado".equals(estadoPago) || "Sin Mora".equals(estadoPago)) {
                JOptionPane.showMessageDialog(this, "Este ítem no tiene moras pendientes por pagar.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try {
                int idDetalle = (int) modeloDetalles.getValueAt(fila, 0);

                // Consultamos a la base de datos cuánto debe exactamente de ese ítem
                String sqlSaldos = "SELECT p.id_usuario, u.Nombres, u.Apellidos, dp.monto_mora AS monto_calculado, dp.monto_pagado " +
                        "FROM Detalle_Prestamo dp " +
                        "INNER JOIN Prestamo p ON dp.id_prestamo = p.id_prestamo " +
                        "INNER JOIN Usuarios u ON p.id_usuario = u.ID_Usuario " +
                        "WHERE dp.id_detalle = ?";

                try (java.sql.Connection con = com.biblioteca.config.DatabaseConnection.getConnection();
                     java.sql.PreparedStatement ps = con.prepareStatement(sqlSaldos)) {

                    ps.setInt(1, idDetalle);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int idUsuario = rs.getInt("id_usuario");
                            String lectorStr = rs.getString("Nombres") + " " + rs.getString("Apellidos");
                            double calculado = rs.getDouble("monto_calculado");
                            double pagado = rs.getDouble("monto_pagado");

                            if (calculado > pagado) {
                                // Abrimos la ventana de cobro pasándole el idDetalle
                                DialogPagarMora dialog = new DialogPagarMora(
                                        this, idDetalle, idUsuario, lectorStr, calculado, pagado
                                );
                                dialog.setVisible(true);

                                // Refrescar la tabla interna al cerrar para que diga "Pagado"
                                cargarDetalles();
                            } else {
                                JOptionPane.showMessageDialog(this, "La mora de este ítem ya está completamente saldada.", "Información", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al procesar el cobro:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });


    }
}