package com.biblioteca.view;

import com.biblioteca.view.panels.PanelInventario;
import com.biblioteca.view.panels.PanelUsuarios;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel panelCentral;

    public MainFrame() throws SQLException {
        setTitle("Sistema de Mediateca - Panel de Administración");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 2. RECUPERAMOS EL USUARIO DESDE LA BÓVEDA GLOBAL
        com.biblioteca.model.Usuario usuarioActivo = com.biblioteca.util.SessionManager.getInstance().getUsuarioLogueado();

        // 3. Pequeño control de seguridad (por si alguien intenta abrir el MainFrame sin loguearse)
        if (usuarioActivo == null) {
            JOptionPane.showMessageDialog(null, "Error: No hay una sesión activa.", "Error de Seguridad", JOptionPane.ERROR_MESSAGE);
            this.dispose();
            new LoginFrame().setVisible(true);
            return; // Corta la ejecución
        }
        String nombreMostrado = usuarioActivo.getNombres();
        String rol = usuarioActivo.getTipoUsuario().getNombreRol();

        // --- SIDEBAR (Menú lateral) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(new Color(41, 50, 65));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel lblLogo = new JLabel("Menú Principal");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblLogo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        JButton btnInicio = crearBotonMenu("Inicio / Bienvenida");
        JButton btnPrestamos = crearBotonMenu("Gestión de Préstamos");
        JButton btnSalir = crearBotonMenu("Cerrar Sesión");
        JButton btnInventario = crearBotonMenu("Inventario");
        JButton btnUsuarios = crearBotonMenu("Gestión de Usuarios");

        sidebar.add(btnInicio);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnPrestamos);
        // Si es Admin o Profesor, puede ver Inventario. Si es alumno, tal vez no, o solo consulta.
        if (rol.equalsIgnoreCase("Administrador") || rol.equalsIgnoreCase("Profesor")) {
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebar.add(btnInventario);
        }
        // SOLO LOS ADMINISTRADORES pueden gestionar usuarios
        if (rol.equalsIgnoreCase("Administrador")) {
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebar.add(btnUsuarios);
        }
        sidebar.add(Box.createVerticalGlue()); // Empuja el botón salir hacia abajo
        sidebar.add(btnSalir);

        // --- PANEL CENTRAL (Contenido cambiante) ---
        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);

        JPanel panelBienvenida = new JPanel(new BorderLayout());
        panelBienvenida.setBackground(Color.WHITE);
        JLabel lblBienvenida = new JLabel("¡Bienvenido, " + nombreMostrado + "!", SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 36));
        panelBienvenida.add(lblBienvenida, BorderLayout.CENTER);

        com.biblioteca.view.panels.PanelPrestamos panelPrestamos = new com.biblioteca.view.panels.PanelPrestamos();
        // --- PANTALLA 3: INVENTARIO ---
        // Importa com.biblioteca.view.panels.PanelInventario si te lo pide IntelliJ
        PanelInventario panelInventario = new PanelInventario();
        PanelUsuarios panelUsuarios = new PanelUsuarios();

        // Añadimos las "Cartas" al panel central
        panelCentral.add(panelBienvenida, "INICIO");
        panelCentral.add(panelPrestamos, "PRESTAMOS");
        panelCentral.add(panelInventario, "INVENTARIO"); // <--- AGREGAMOS ESTO
        panelCentral.add(panelUsuarios, "USUARIOS");


        add(sidebar, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);

        // --- ACCIONES DE LOS BOTONES ---
        btnInicio.addActionListener(e -> cardLayout.show(panelCentral, "INICIO"));
        btnPrestamos.addActionListener(e -> cardLayout.show(panelCentral, "PRESTAMOS"));
        // AGREGAMOS LA ACCIÓN AL BOTÓN DE INVENTARIO
        btnInventario.addActionListener(e -> cardLayout.show(panelCentral, "INVENTARIO"));
        btnUsuarios.addActionListener(e -> cardLayout.show(panelCentral, "USUARIOS"));

        // 4. LÓGICA DE CIERRE DE SESIÓN (Garantizando que se borre)
        btnSalir.addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Seguro que deseas cerrar sesión?",
                    "Cerrar Sesión",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                // ¡DESTRUIMOS LA SESIÓN COMPLETAMENTE!
                com.biblioteca.util.SessionManager.getInstance().cerrarSesion();

                this.dispose(); // Destruye el MainFrame
                new LoginFrame().setVisible(true); // Vuelve a la pantalla de login
            }
        });
        cardLayout.show(panelCentral, "INICIO");
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(61, 90, 128));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(230, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}