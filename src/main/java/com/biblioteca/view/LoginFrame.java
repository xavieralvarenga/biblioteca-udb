package com.biblioteca.view;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JLabel lblNotificacion;

    public LoginFrame() {
        setTitle("Colegio Amigos De Don Bosco - Acceso");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana
        setResizable(false);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setBounds(0, 40, 400, 40);
        mainPanel.add(lblTitulo);

        // --- BANNER DE NOTIFICACIONES (Oculto por defecto) ---
        lblNotificacion = new JLabel("", SwingConstants.CENTER);
        lblNotificacion.setBounds(50, 85, 300, 25);
        lblNotificacion.setOpaque(true); // Para poder pintarle el fondo
        lblNotificacion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNotificacion.setVisible(false); // No se ve hasta que haya un error
        mainPanel.add(lblNotificacion);

        JLabel lblUsuario = new JLabel("Carnet / Usuario:");
        lblUsuario.setBounds(50, 110, 300, 25);
        mainPanel.add(lblUsuario);

        txtUsuario = new JTextField();
        txtUsuario.setBounds(50, 140, 300, 40);
        mainPanel.add(txtUsuario);

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setBounds(50, 200, 300, 25);
        mainPanel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(50, 230, 300, 40);
        mainPanel.add(txtPassword);

        btnIngresar = new JButton("Ingresar al Sistema");
        btnIngresar.setBounds(50, 320, 300, 45);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnIngresar);

        add(mainPanel);

        // --- LÓGICA DE NAVEGACIÓN ---
        // --- LÓGICA DE NAVEGACIÓN EN LoginFrame.java ---
        // --- LÓGICA DE NAVEGACIÓN ---
        btnIngresar.addActionListener(e -> {
            String carnet = txtUsuario.getText().trim();
            String password = new String(txtPassword.getPassword());

            if (carnet.isEmpty() || password.isEmpty()) {
                mostrarNotificacion("⚠ Completa todos los campos.", new Color(255, 193, 7), Color.BLACK); // Amarillo advertencia
                return;
            }

            try {
                com.biblioteca.service.UsuarioService authService = new com.biblioteca.service.UsuarioService();
                com.biblioteca.model.Usuario usuarioLogueado = authService.login(carnet, password);

                // ¡GUARDAMOS LA SESIÓN GLOBALMENTE!
                com.biblioteca.util.SessionManager.getInstance().setUsuarioLogueado(usuarioLogueado);

                this.dispose(); // Destruye la ventana del Login
                new MainFrame().setVisible(true);

            } catch (Exception ex) {
                // Si el error es de inactividad, lo pintamos naranja. Si es otro, rojo oscuro.
                Color colorFondo = ex.getMessage().contains("inactivo")
                        ? new Color(253, 126, 20)  // Naranja
                        : new Color(220, 53, 69);  // Rojo (Peligro)

                mostrarNotificacion("❌ " + ex.getMessage(), colorFondo, Color.WHITE);
            }
        });
    }
    // Método para mostrar mensajes estilizados que desaparecen solos
    private void mostrarNotificacion(String mensaje, Color colorFondo, Color colorTexto) {
        lblNotificacion.setText(mensaje);
        lblNotificacion.setBackground(colorFondo);
        lblNotificacion.setForeground(colorTexto);
        lblNotificacion.setVisible(true);

        // Importa javax.swing.Timer si IntelliJ te lo pide
        Timer timer = new Timer(3500, e -> lblNotificacion.setVisible(false));
        timer.setRepeats(false); // Solo se ejecuta una vez
        timer.start();
    }
}