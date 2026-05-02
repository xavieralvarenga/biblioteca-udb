package com.biblioteca.view.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelUsuarios extends JPanel {

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxFiltroRol;
    private JButton btnNuevo, btnEditar, btnEliminar, btnResetPassword;

    public PanelUsuarios() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. TÍTULO Y BARRA DE BÚSQUEDA (Norte)
        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));
        panelNorte.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Gestión de Usuarios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);

        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(20);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Carnet, Nombres o Apellidos...");
        panelBusqueda.add(txtBuscar);

        panelBusqueda.add(new JLabel("Rol:"));
        // Aquí incluimos los roles que pediste
        cbxFiltroRol = new JComboBox<>(new String[]{"Todos", "Administrador", "Profesor", "Alumno"});
        panelBusqueda.add(cbxFiltroRol);

        JButton btnBuscar = new JButton("Filtrar");
        btnBuscar.setBackground(new Color(61, 90, 128));
        btnBuscar.setForeground(Color.WHITE);
        panelBusqueda.add(btnBuscar);

        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // 2. TABLA DE DATOS (Centro)
        String[] columnas = {"ID", "Carnet", "Nombres", "Apellidos", "Rol", "Estado", "Mora"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setRowHeight(30);
        tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setShowVerticalLines(false);

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        add(scrollPane, BorderLayout.CENTER);

        // 3. BOTONES DE ACCIÓN (Sur)
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelSur.setBackground(Color.WHITE);

        // Botón especial para administradores: resetear contraseña de un usuario
        btnResetPassword = crearBoton("Restablecer Contraseña", new Color(108, 117, 125));
        btnNuevo = crearBoton("Nuevo Usuario", new Color(61, 90, 128));
        btnEditar = crearBoton("Editar", new Color(244, 162, 97));
        btnEliminar = crearBoton("Desactivar", new Color(231, 111, 81)); // En vez de borrar, es mejor "Desactivar"

        panelSur.add(btnResetPassword);
        panelSur.add(btnNuevo);
        panelSur.add(btnEditar);
        panelSur.add(btnEliminar);

        add(panelSur, BorderLayout.SOUTH);

        // --- DATOS DE PRUEBA (MOCK DATA) ---
        cargarDatosDePrueba();

        // --- EVENTOS BÁSICOS ---
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

    private void cargarDatosDePrueba() {
        modeloTabla.addRow(new Object[]{"1", "ADM-001", "Carlos", "Martínez", "Administrador", "Activo", "No"});
        modeloTabla.addRow(new Object[]{"2", "PRF-089", "Ana", "López", "Profesor", "Activo", "No"});
        modeloTabla.addRow(new Object[]{"3", "ALU-2023", "Luis", "Pérez", "Alumno", "Activo", "Sí (Mora)"});
        modeloTabla.addRow(new Object[]{"4", "ALU-2024", "María", "Gómez", "Alumno", "Inactivo", "No"});
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Aquí abriremos el formulario para agregar un nuevo usuario.");
        });

        btnEditar.addActionListener(e -> {
            int fila = tablaUsuarios.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            } else {
                String nombre = (String) modeloTabla.getValueAt(fila, 2);
                JOptionPane.showMessageDialog(this, "Editando usuario: " + nombre);
            }
        });

        btnResetPassword.addActionListener(e -> {
            int fila = tablaUsuarios.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario para restablecer su contraseña.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            } else {
                String carnet = (String) modeloTabla.getValueAt(fila, 1);
                JOptionPane.showMessageDialog(this, "Se restablecerá la contraseña del carnet: " + carnet + "\nSe asignará el carnet como contraseña temporal.");
            }
        });

    }

}