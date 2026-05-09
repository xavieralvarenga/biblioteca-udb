package com.biblioteca.view.forms;

import com.biblioteca.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DialogNuevoPrestamo extends JDialog {

    // Componentes del Usuario
    private JTextField txtCarnet;
    private JButton btnBuscarUsuario;
    private JLabel lblNombreUsuario, lblEstadoUsuario;

    // Componentes del Material
    private JTextField txtCodigoBarras;
    private JButton btnBuscarEjemplar;
    private JLabel lblTituloMaterial, lblEstadoMaterial;

    // Variables para almacenar las selecciones para enviarlas a MySQL
    private com.biblioteca.model.Usuario usuarioFinal = null;
    private int idEjemplarFinal = -1;
    private LocalDate fechaPrestamoFinal;
    private LocalDate fechaLimiteFinal;
    // Componentes de Resumen
    private JLabel lblFechaPrestamo, lblFechaLimite;
    private JButton btnCancelar, btnGuardar;

    public DialogNuevoPrestamo(Window owner) {
        super(owner, "Registrar Nuevo Préstamo", ModalityType.APPLICATION_MODAL);
        setSize(550, 550);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        // --- 1. SECCIÓN: DATOS DEL LECTOR ---
        JPanel panelUsuario = crearPanelSeccion("1. Datos del Lector");

        JPanel panelBusquedaU = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusquedaU.setBackground(Color.WHITE);
        // ... (dentro de tu constructor, en la sección 1) ...
        panelBusquedaU.add(new JLabel("Lector Seleccionado:"));
        txtCarnet = new JTextField(20); // Lo hacemos más grande
        txtCarnet.setEditable(false); // NO SE PUEDE ESCRIBIR DIRECTO
        txtCarnet.setBackground(new Color(240, 240, 240)); // Gris claro para indicar que está bloqueado

        btnBuscarUsuario = new JButton("🔍 Buscar en Lista"); // Cambiamos el texto

        panelBusquedaU.add(txtCarnet);
        panelBusquedaU.add(btnBuscarUsuario);
        // ...

        JPanel panelInfoU = new JPanel(new GridLayout(2, 1, 5, 5));
        panelInfoU.setBackground(Color.WHITE);
        lblNombreUsuario = new JLabel("Lector: [Esperando búsqueda...]");
        lblEstadoUsuario = new JLabel("Estado: -");
        lblNombreUsuario.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelInfoU.add(lblNombreUsuario);
        panelInfoU.add(lblEstadoUsuario);

        panelUsuario.add(panelBusquedaU);
        panelUsuario.add(panelInfoU);
        mainPanel.add(panelUsuario);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Espaciador

        // --- 2. SECCIÓN: DATOS DEL MATERIAL ---
        JPanel panelMaterial = crearPanelSeccion("2. Datos del Material (Ejemplar)");

        JPanel panelBusquedaM = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusquedaM.setBackground(Color.WHITE);
        panelBusquedaM.add(new JLabel("Material Seleccionado:"));
        txtCodigoBarras = new JTextField(20);
        txtCodigoBarras.setEditable(false); // NO SE PUEDE ESCRIBIR DIRECTO
        txtCodigoBarras.setBackground(new Color(240, 240, 240));

        btnBuscarEjemplar = new JButton("🔍 Buscar en Lista");

        panelBusquedaM.add(txtCodigoBarras);
        panelBusquedaM.add(btnBuscarEjemplar);

        JPanel panelInfoM = new JPanel(new GridLayout(2, 1, 5, 5));
        panelInfoM.setBackground(Color.WHITE);
        lblTituloMaterial = new JLabel("Título: [Esperando búsqueda...]");
        lblEstadoMaterial = new JLabel("Disponibilidad: -");
        lblTituloMaterial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelInfoM.add(lblTituloMaterial);
        panelInfoM.add(lblEstadoMaterial);

        panelMaterial.add(panelBusquedaM);
        panelMaterial.add(panelInfoM);
        mainPanel.add(panelMaterial);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- 3. SECCIÓN: RESUMEN DEL PRÉSTAMO ---
        JPanel panelResumen = crearPanelSeccion("3. Resumen de Fechas");
        JPanel panelFechas = new JPanel(new GridLayout(2, 2, 10, 10));
        panelFechas.setBackground(Color.WHITE);

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaHoy = LocalDate.now().format(formato);

        panelFechas.add(new JLabel("Fecha de Préstamo:"));
        lblFechaPrestamo = new JLabel(fechaHoy);
        lblFechaPrestamo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFechaPrestamo.setForeground(new Color(61, 90, 128));

        panelFechas.add(new JLabel("Fecha Límite (Estimada):"));
        lblFechaLimite = new JLabel("Calculando...");
        lblFechaLimite.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFechaLimite.setForeground(new Color(220, 53, 69));

        panelFechas.add(lblFechaPrestamo);
        panelFechas.add(lblFechaLimite);
        panelResumen.add(panelFechas);
        mainPanel.add(panelResumen);

        add(mainPanel, BorderLayout.CENTER);

        // --- 4. BOTONES DE ACCIÓN (Sur) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBotones.setBackground(Color.WHITE);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(231, 111, 81));
        btnCancelar.setForeground(Color.WHITE);

        btnGuardar = new JButton("Autorizar Préstamo");
        btnGuardar.setBackground(new Color(41, 171, 135));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setEnabled(false); // Desactivado hasta que todo sea válido

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        configurarEventos();
    }

    private JPanel crearPanelSeccion(String titulo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                titulo, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(61, 90, 128)
        ));
        return panel;
    }

    private void configurarEventos() {
        btnCancelar.addActionListener(e -> this.dispose());

        // --- BÚSQUEDA Y CÁLCULO DE LECTOR ---
        btnBuscarUsuario.addActionListener(e -> {
            DialogBuscarUsuario dialogBuscador = new DialogBuscarUsuario(this);
            dialogBuscador.setVisible(true);

            usuarioFinal = dialogBuscador.getUsuarioSeleccionado();

            if (usuarioFinal != null) {
                // 1. Mostrar datos básicos
                txtCarnet.setText(usuarioFinal.getCarnet() + " - " + usuarioFinal.getNombres());
                lblNombreUsuario.setText("Lector: " + usuarioFinal.getNombres() + " " + usuarioFinal.getApellidos());

                // 2. Validación estricta de Mora
                if (usuarioFinal.getEstadoMora()) {
                    // BLOQUEO TOTAL
                    lblEstadoUsuario.setText("ESTADO: BLOQUEADO POR MORA");
                    lblEstadoUsuario.setForeground(new Color(220, 53, 69)); // Rojo peligro
                    btnGuardar.setEnabled(false); // Desactiva el botón de autorizar

                    JOptionPane.showMessageDialog(this,
                            "El usuario tiene deudas pendientes.\nDebe solventar su mora antes de solicitar nuevos materiales.",
                            "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                } else {
                    // LECTOR SOLVENTE Y AUTORIZADO
                    lblEstadoUsuario.setText("Rol: " + usuarioFinal.getTipoUsuario().getNombreRol() + " | Mora: No");
                    lblEstadoUsuario.setForeground(new Color(41, 171, 135)); // Verde éxito

                    // --- 3. CÁLCULO DE LAS FECHAS ---
                    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    fechaPrestamoFinal = LocalDate.now();

                    // Sumamos los días que le permite su rol (ej. 7 días para alumnos, 14 para profesores)
                    int diasPermitidos = usuarioFinal.getTipoUsuario().getMaxDiasPrestamo();
                    fechaLimiteFinal = fechaPrestamoFinal.plusDays(diasPermitidos);

                    lblFechaPrestamo.setText(fechaPrestamoFinal.format(formato));
                    lblFechaLimite.setText(fechaLimiteFinal.format(formato));
                    lblFechaLimite.setForeground(new Color(41, 171, 135)); // Pasa a verde porque ya es válido

                    // 4. Verificación final cruzada
                    // Si ya habíamos elegido un material válido previamente, habilitamos el botón de guardar
                    if (idEjemplarFinal != -1) {
                        btnGuardar.setEnabled(true);
                    }
                }
            }
        });

        // --- BÚSQUEDA DE MATERIAL ---
        btnBuscarEjemplar.addActionListener(e -> {
            DialogBuscarEjemplar dialogBuscador = new DialogBuscarEjemplar(this);
            dialogBuscador.setVisible(true);

            String codBarras = dialogBuscador.getCodigoBarrasSeleccionado();

            if (codBarras != null) {
                idEjemplarFinal = dialogBuscador.getIdEjemplarSeleccionado();
                String estado = dialogBuscador.getEstadoSeleccionado();

                txtCodigoBarras.setText(codBarras);
                lblTituloMaterial.setText("Título: " + dialogBuscador.getTituloSeleccionado());
                lblEstadoMaterial.setText("Disponibilidad: " + estado);

                if (!"Disponible".equalsIgnoreCase(estado)) {
                    lblEstadoMaterial.setForeground(new Color(220, 53, 69));
                    btnGuardar.setEnabled(false);
                    idEjemplarFinal = -1; // Invalidamos la selección
                    JOptionPane.showMessageDialog(this, "Este material está '" + estado + "'.", "No Disponible", JOptionPane.WARNING_MESSAGE);
                } else {
                    lblEstadoMaterial.setForeground(new Color(41, 171, 135));

                    // Si ya elegimos un lector válido (sin mora), habilitamos el botón de guardar
                    if (usuarioFinal != null && !usuarioFinal.getEstadoMora()) {
                        btnGuardar.setEnabled(true);
                    }
                }
            }
        });

        // --- GUARDADO FINAL DEL PRÉSTAMO ---
        btnGuardar.addActionListener(e -> {
            try {
                com.biblioteca.repository.impl.PrestamoDAO dao = new com.biblioteca.repository.impl.PrestamoDAO();

                // Ejecutamos la transacción SQL
                boolean exito = dao.registrarNuevoPrestamo(usuarioFinal.getIdUsuario(), idEjemplarFinal, fechaPrestamoFinal, fechaLimiteFinal);

                if(exito) {
                    JOptionPane.showMessageDialog(this, "¡Préstamo autorizado y guardado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose(); // Cerramos la ventana
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error crítico al guardar el préstamo:\n" + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}