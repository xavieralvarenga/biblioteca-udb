package com.biblioteca.view.forms;

import com.biblioteca.model.Usuario;
import com.biblioteca.service.PrestamoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DialogNuevoPrestamo extends JDialog {

    private Usuario usuarioFinal = null;

    // Listas en memoria para nuestro "Carrito"
    private List<Integer> idsEjemplaresCarrito = new ArrayList<>();
    private List<LocalDate> fechasLimitesCarrito = new ArrayList<>();

    private JTextField txtCarnet;
    private JLabel lblNombreUsuario, lblEstadoUsuario, lblFechaPrestamo;
    private JButton btnBuscarUsuario, btnBuscarEjemplar, btnQuitarEjemplar, btnGuardar, btnCancelar;

    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;

    private final PrestamoService prestamoService;

    public DialogNuevoPrestamo(Window owner) {
        super(owner, "Registrar Nuevo Préstamo (Múltiples Materiales)", ModalityType.APPLICATION_MODAL);
        this.prestamoService = new PrestamoService();

        setSize(750, 600); // Ventana un poco más grande para acomodar la tabla
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // --- 1. SECCIÓN: LECTOR ---
        JPanel panelLector = new JPanel(new BorderLayout(5, 5));
        panelLector.setBackground(Color.WHITE);
        panelLector.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "1. Datos del Lector", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(61, 90, 128)));

        JPanel pnlBuscadorUser = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBuscadorUser.setBackground(Color.WHITE);
        pnlBuscadorUser.add(new JLabel("Lector Seleccionado: "));
        txtCarnet = new JTextField(20);
        txtCarnet.setEditable(false);
        pnlBuscadorUser.add(txtCarnet);

        btnBuscarUsuario = new JButton("Buscar Lector");
        pnlBuscadorUser.add(btnBuscarUsuario);
        panelLector.add(pnlBuscadorUser, BorderLayout.NORTH);

        JPanel pnlInfoUser = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlInfoUser.setBackground(Color.WHITE);
        pnlInfoUser.setBorder(new EmptyBorder(5, 10, 5, 10));
        lblNombreUsuario = new JLabel("Lector: [Esperando selección]");
        lblNombreUsuario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoUsuario = new JLabel("Rol: - | Mora: - | Libros Permitidos: -");
        pnlInfoUser.add(lblNombreUsuario);
        pnlInfoUser.add(lblEstadoUsuario);
        panelLector.add(pnlInfoUser, BorderLayout.CENTER);

        mainPanel.add(panelLector);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- 2. SECCIÓN: CARRITO DE MATERIALES ---
        JPanel panelMateriales = new JPanel(new BorderLayout(5, 5));
        panelMateriales.setBackground(Color.WHITE);
        panelMateriales.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "2. Materiales a Prestar (Carrito)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(61, 90, 128)));

        JPanel pnlBotonesCarrito = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBotonesCarrito.setBackground(Color.WHITE);
        btnBuscarEjemplar = new JButton("Agregar Material");
        btnBuscarEjemplar.setEnabled(false); // Se habilita al elegir lector
        btnQuitarEjemplar = new JButton("Quitar Seleccionado");
        btnQuitarEjemplar.setEnabled(false);
        pnlBotonesCarrito.add(btnBuscarEjemplar);
        pnlBotonesCarrito.add(btnQuitarEjemplar);
        panelMateriales.add(pnlBotonesCarrito, BorderLayout.NORTH);

        String[] columnas = {"ID", "Cód. Barras", "Título", "Estado Físico", "Devolución Estimada"};
        modeloCarrito = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.setPreferredSize(new Dimension(700, 150));
        panelMateriales.add(scrollCarrito, BorderLayout.CENTER);

        mainPanel.add(panelMateriales);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- 3. SECCIÓN: RESUMEN Y BOTONES ---
        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.setBackground(Color.WHITE);

        JPanel pnlFecha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFecha.setBackground(Color.WHITE);
        lblFechaPrestamo = new JLabel("Fecha de Operación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblFechaPrestamo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlFecha.add(lblFechaPrestamo);
        panelSur.add(pnlFecha, BorderLayout.WEST);

        JPanel pnlBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotonesAccion.setBackground(Color.WHITE);
        btnCancelar = new JButton("Cancelar");
        btnGuardar = new JButton("Autorizar Préstamo");
        btnGuardar.setBackground(new Color(41, 171, 135));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setEnabled(false); // Se habilita si hay items en el carrito

        pnlBotonesAccion.add(btnCancelar);
        pnlBotonesAccion.add(btnGuardar);
        panelSur.add(pnlBotonesAccion, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(panelSur, BorderLayout.SOUTH);

        configurarEventos();
    }

    private void configurarEventos() {
        btnCancelar.addActionListener(e -> this.dispose());

        // --- SELECCIONAR LECTOR ---
        btnBuscarUsuario.addActionListener(e -> {
            DialogBuscarUsuario dialog = new DialogBuscarUsuario(this);
            dialog.setVisible(true);

            Usuario tempUser = dialog.getUsuarioSeleccionado();
            if (tempUser != null) {
                if (tempUser.getEstadoMora()) {
                    JOptionPane.showMessageDialog(this, "El lector tiene mora activa. No puede realizar préstamos.", "Bloqueado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Si cambiamos de usuario, limpiamos el carrito por seguridad
                if (usuarioFinal != null && usuarioFinal.getIdUsuario() != tempUser.getIdUsuario()) {
                    modeloCarrito.setRowCount(0);
                    idsEjemplaresCarrito.clear();
                    fechasLimitesCarrito.clear();
                    btnGuardar.setEnabled(false);
                }

                usuarioFinal = tempUser;
                txtCarnet.setText(usuarioFinal.getCarnet() + " - " + usuarioFinal.getNombres());
                lblNombreUsuario.setText("Lector: " + usuarioFinal.getNombres() + " " + usuarioFinal.getApellidos());
                lblEstadoUsuario.setText("Rol: " + usuarioFinal.getTipoUsuario().getNombreRol() +
                        " | Libros Permitidos: " + usuarioFinal.getTipoUsuario().getMaxLibrosPermitidos());
                lblEstadoUsuario.setForeground(new Color(41, 171, 135));

                btnBuscarEjemplar.setEnabled(true);
            }
        });

        // --- AGREGAR AL CARRITO ---
        btnBuscarEjemplar.addActionListener(e -> {
            // Validación de límite
            if (idsEjemplaresCarrito.size() >= usuarioFinal.getTipoUsuario().getMaxLibrosPermitidos()) {
                JOptionPane.showMessageDialog(this, "El usuario ya alcanzó el límite máximo de libros permitidos (" + usuarioFinal.getTipoUsuario().getMaxLibrosPermitidos() + ").", "Límite Alcanzado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DialogBuscarEjemplar dialog = new DialogBuscarEjemplar(this);
            dialog.setVisible(true);

            int idEjemplar = dialog.getIdEjemplarSeleccionado();
            if (idEjemplar != -1) {
                String estadoFisico = dialog.getEstadoSeleccionado();

                if (!"Disponible".equalsIgnoreCase(estadoFisico)) {
                    JOptionPane.showMessageDialog(this, "El material seleccionado está: " + estadoFisico, "No Disponible", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (idsEjemplaresCarrito.contains(idEjemplar)) {
                    JOptionPane.showMessageDialog(this, "Este material ya está en el carrito.", "Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Calcular fecha límite para este ítem específico
                int diasPermitidos = usuarioFinal.getTipoUsuario().getMaxDiasPrestamo();
                LocalDate fechaLimite = LocalDate.now().plusDays(diasPermitidos);

                // Agregar a memoria
                idsEjemplaresCarrito.add(idEjemplar);
                fechasLimitesCarrito.add(fechaLimite);

                // Agregar a la tabla visual
                modeloCarrito.addRow(new Object[]{
                        idEjemplar,
                        dialog.getCodigoBarrasSeleccionado(),
                        dialog.getTituloSeleccionado(),
                        estadoFisico,
                        fechaLimite.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                });

                btnGuardar.setEnabled(true);
            }
        });

        // --- ACTIVAR BOTÓN QUITAR ---
        tablaCarrito.getSelectionModel().addListSelectionListener(e -> {
            btnQuitarEjemplar.setEnabled(tablaCarrito.getSelectedRow() != -1);
        });

        // --- QUITAR DEL CARRITO ---
        btnQuitarEjemplar.addActionListener(e -> {
            int fila = tablaCarrito.getSelectedRow();
            if (fila != -1) {
                idsEjemplaresCarrito.remove(fila);
                fechasLimitesCarrito.remove(fila);
                modeloCarrito.removeRow(fila);

                if (idsEjemplaresCarrito.isEmpty()) {
                    btnGuardar.setEnabled(false);
                }
            }
        });

        // --- PROCESAR PRÉSTAMO MÚLTIPLE ---
        btnGuardar.addActionListener(e -> {
            try {
                boolean exito = prestamoService.registrarNuevoPrestamo(
                        usuarioFinal.getIdUsuario(),
                        LocalDate.now(),
                        idsEjemplaresCarrito,
                        fechasLimitesCarrito
                );

                if(exito) {
                    JOptionPane.showMessageDialog(this, "¡Préstamo autorizado para " + idsEjemplaresCarrito.size() + " material(es)!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}