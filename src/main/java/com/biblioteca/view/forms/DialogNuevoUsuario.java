package com.biblioteca.view.forms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DialogNuevoUsuario extends JDialog {

    private JTextField txtNombres, txtApellidos, txtCarnet;
    private JComboBox<String> cbxRol;
    private JPasswordField txtPassword, txtConfirmarPassword;

    public DialogNuevoUsuario(Window owner) {
        super(owner, "Registrar Nuevo Usuario", ModalityType.APPLICATION_MODAL);
        setSize(450, 500);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Contenedor principal con padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // --- SECCIÓN: DATOS DEL USUARIO ---
        JPanel panelFormulario = new JPanel(new GridLayout(6, 2, 10, 15));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Información de la Cuenta",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(61, 90, 128)
        ));

        // Nombres y Apellidos
        txtNombres = agregarCampoTexto(panelFormulario, "Nombres *:");
        txtApellidos = agregarCampoTexto(panelFormulario, "Apellidos *:");

        // Carnet
        txtCarnet = agregarCampoTexto(panelFormulario, "Carnet / Identificador *:");

        // Rol / Nivel de Acceso
        panelFormulario.add(new JLabel(" Tipo de Acceso *:"));
        cbxRol = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"});
        cbxRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFormulario.add(cbxRol);

        // Contraseñas
        panelFormulario.add(new JLabel(" Contraseña *:"));
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFormulario.add(txtPassword);

        panelFormulario.add(new JLabel(" Confirmar Contraseña *:"));
        txtConfirmarPassword = new JPasswordField();
        txtConfirmarPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFormulario.add(txtConfirmarPassword);

        mainPanel.add(panelFormulario);
        add(mainPanel, BorderLayout.CENTER);

        // --- SECCIÓN: BOTONES DE ACCIÓN (Sur) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBotones.setBackground(Color.WHITE);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setBackground(new Color(231, 111, 81)); // Rojo suave
        btnCancelar.setForeground(Color.WHITE);

        JButton btnGuardar = new JButton("Guardar Usuario");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setBackground(new Color(41, 171, 135)); // Verde Éxito
        btnGuardar.setForeground(Color.WHITE);

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnCancelar.addActionListener(e -> this.dispose());

        btnGuardar.addActionListener(e -> {
            String nombres = txtNombres.getText().trim();
            String apellidos = txtApellidos.getText().trim();
            String carnet = txtCarnet.getText().trim();
            String rolSeleccionado = (String) cbxRol.getSelectedItem();
            String pass1 = new String(txtPassword.getPassword());
            String pass2 = new String(txtConfirmarPassword.getPassword());

            // 1. Validaciones visuales
            if (nombres.isEmpty() || apellidos.isEmpty() || carnet.isEmpty() || pass1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos con asterisco (*) son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pass1.equals(pass2)) {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden. Verifícalas.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Mapear el texto del ComboBox al ID de tu tabla TipoUsuario
            com.biblioteca.model.TipoUsuario tipo = new com.biblioteca.model.TipoUsuario();
            if ("Administrador".equals(rolSeleccionado)) {
                tipo.setIdTipo(1);
            } else if ("Profesor".equals(rolSeleccionado)) {
                tipo.setIdTipo(2);
            } else if ("Alumno".equals(rolSeleccionado)) {
                tipo.setIdTipo(3);
            }

            // 3. Armar nuestro objeto Usuario
            com.biblioteca.model.Usuario nuevoUsuario = new com.biblioteca.model.Usuario();
            nuevoUsuario.setNombres(nombres);
            nuevoUsuario.setApellidos(apellidos);
            nuevoUsuario.setCarnet(carnet);
            nuevoUsuario.setTipoUsuario(tipo);
            nuevoUsuario.setPasswordHash(pass1); // Contraseña plana por el momento
            nuevoUsuario.setEstado("Activo"); // Por defecto entra activo
            nuevoUsuario.setEstadoMora(false); // Por defecto entra sin mora

            // 4. Mandarlo al servicio para guardar
            try {
                com.biblioteca.service.UsuarioService service = new com.biblioteca.service.UsuarioService();

                if (service.registrarUsuario(nuevoUsuario)) {
                    JOptionPane.showMessageDialog(this, "¡Usuario registrado con éxito en la base de datos!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose(); // Cierra el pop-up
                } else {
                    JOptionPane.showMessageDialog(this, "Ocurrió un error interno al guardar. Revisa la consola.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                // Captura el error si el carnet ya existe
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registro Denegado", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    // Método auxiliar para evitar código repetitivo
    private JTextField agregarCampoTexto(JPanel panel, String etiqueta) {
        JLabel label = new JLabel(" " + etiqueta);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(label);
        panel.add(textField);
        return textField;
    }
}