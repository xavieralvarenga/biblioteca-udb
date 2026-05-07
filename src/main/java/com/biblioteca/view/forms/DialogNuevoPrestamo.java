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

        btnBuscarUsuario.addActionListener(e -> {
            // 1. Abrimos el buscador modal
            DialogBuscarUsuario dialogBuscador = new DialogBuscarUsuario(this);
            dialogBuscador.setVisible(true); // El código se pausa aquí hasta que cierres el buscador

            // 2. Cuando se cierra, le preguntamos si seleccionó a alguien
            Usuario userElegido = dialogBuscador.getUsuarioSeleccionado();

            // 3. Si eligió a alguien, actualizamos la interfaz
            if (userElegido != null) {
                txtCarnet.setText(userElegido.getCarnet() + " - " + userElegido.getNombres());
                lblNombreUsuario.setText("Lector: " + userElegido.getNombres() + " " + userElegido.getApellidos());

                String textoMora = userElegido.getEstadoMora() ? "Sí" : "No";
                lblEstadoUsuario.setText("Rol: " + userElegido.getTipoUsuario().getNombreRol() + " | Mora: " + textoMora);

                if (userElegido.getEstadoMora()) {
                    lblEstadoUsuario.setForeground(new Color(220, 53, 69)); // Rojo
                    JOptionPane.showMessageDialog(this, "Atención: El lector tiene mora activa.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                } else {
                    lblEstadoUsuario.setForeground(new Color(41, 171, 135)); // Verde
                }
            }
        });

        btnBuscarEjemplar.addActionListener(e -> {
            DialogBuscarEjemplar dialogBuscador = new DialogBuscarEjemplar(this);
            dialogBuscador.setVisible(true); // Se pausa hasta que elijas algo

            String codBarras = dialogBuscador.getCodigoBarrasSeleccionado();

            // Si realmente seleccionó algo y no solo cerró la ventana
            if (codBarras != null) {
                String titulo = dialogBuscador.getTituloSeleccionado();
                String estado = dialogBuscador.getEstadoSeleccionado();

                txtCodigoBarras.setText(codBarras);
                lblTituloMaterial.setText("Título: " + titulo);
                lblEstadoMaterial.setText("Disponibilidad: " + estado);

                // Regla de Negocio: Solo podemos prestar si está "Disponible"
                if (!"Disponible".equalsIgnoreCase(estado)) {
                    lblEstadoMaterial.setForeground(new Color(220, 53, 69)); // Rojo
                    btnGuardar.setEnabled(false); // Bloqueamos el botón de guardar
                    JOptionPane.showMessageDialog(this, "Este material se encuentra '" + estado + "' y no puede ser prestado.", "Material No Disponible", JOptionPane.WARNING_MESSAGE);
                } else {
                    lblEstadoMaterial.setForeground(new Color(41, 171, 135)); // Verde

                    // Si el usuario también fue seleccionado y no tiene mora, activamos el botón Guardar
                    if (!txtCarnet.getText().isEmpty() && lblEstadoUsuario.getText().contains("Mora: No")) {
                        btnGuardar.setEnabled(true);
                    }
                }
            }
        });

        // Simulación: Guardar
        btnGuardar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "¡Préstamo registrado exitosamente en la Base de Datos!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        });
    }
}