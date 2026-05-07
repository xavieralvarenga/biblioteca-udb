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
    private JButton btnNuevoPrestamo, btnDevolucion, btnVerDetalles;

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

        JButton btnBuscar = new JButton("Filtrar");
        btnBuscar.setBackground(new Color(61, 90, 128));
        btnBuscar.setForeground(Color.WHITE);
        panelBusqueda.add(btnBuscar);

        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // --- 2. TABLA DE DATOS (Centro) ---
        // Basado en tu diagrama ER: Unimos datos de Prestamo, Usuario, Ejemplar y Documento
        String[] columnas = {"ID", "Carnet", "Lector", "Cód. Ejemplar", "Título", "Fecha Préstamo", "Fecha Límite", "Estado"};
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
        cargarDatosDePrueba();

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
        DefaultTableCellRenderer renderizadorColores = new DefaultTableCellRenderer() {
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
        tablaPrestamos.getColumnModel().getColumn(7).setCellRenderer(renderizadorColores);
    }

    private void cargarDatosDePrueba() {
        modeloTabla.addRow(new Object[]{"1", "AD262401", "Andy Alvarado", "EJ-0012", "El Principito", "2024-05-01", "2024-05-08", "Vencido"});
        modeloTabla.addRow(new Object[]{"2", "NR1234", "Nicole Rivera", "EJ-0089", "Cálculo de Stewart", "2024-05-05", "2024-05-19", "Activo"});
        modeloTabla.addRow(new Object[]{"3", "AD4321", "Victor Iraheta", "EJ-0102", "Don Quijote", "2024-04-10", "2024-04-24", "Devuelto"});
    }

    private void configurarEventos() {
        btnNuevoPrestamo.addActionListener(e -> {
            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            // Asegúrate de importar com.biblioteca.view.forms.DialogNuevoPrestamo si te lo pide
            com.biblioteca.view.forms.DialogNuevoPrestamo dialog = new com.biblioteca.view.forms.DialogNuevoPrestamo(ventanaPadre);
            dialog.setVisible(true);

            // Aquí pondremos el cargarPrestamosDesdeBD() más adelante
        });

        btnDevolucion.addActionListener(e -> {
            int fila = tablaPrestamos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un préstamo de la tabla para registrar su devolución.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            } else {
                String estado = (String) modeloTabla.getValueAt(fila, 7);
                if ("Devuelto".equals(estado)) {
                    JOptionPane.showMessageDialog(this, "Este documento ya fue devuelto.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String titulo = (String) modeloTabla.getValueAt(fila, 4);
                JOptionPane.showMessageDialog(this, "Se abrirá la ventana para recibir: " + titulo + "\ny calcular si hay mora por retraso.");
            }
        });
    }
}