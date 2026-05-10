package com.biblioteca.view;

import com.biblioteca.view.panels.PanelInventario;
import com.biblioteca.view.panels.PanelUsuarios;
import com.biblioteca.view.forms.DialogConfigurarMora; // Importamos el diálogo

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel panelCentral;
    private JButton btnConfigMora; // Ya estaba declarado, ahora lo usaremos

    public MainFrame() throws SQLException {
        setTitle("Sistema de Mediateca - Panel de Administración");
        setSize(1100, 750); // Un poco más de espacio para la nueva columna
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 2. RECUPERAMOS EL USUARIO DESDE LA BÓVEDA GLOBAL
        com.biblioteca.model.Usuario usuarioActivo = com.biblioteca.util.SessionManager.getInstance().getUsuarioLogueado();

        if (usuarioActivo == null) {
            JOptionPane.showMessageDialog(null, "Error: No hay una sesión activa.", "Error de Seguridad", JOptionPane.ERROR_MESSAGE);
            this.dispose();
            new LoginFrame().setVisible(true);
            return;
        }

        String nombreMostrado = usuarioActivo.getNombres();
        int rol = usuarioActivo.getTipoUsuario().getIdTipo();

        // --- SIDEBAR (Menú lateral) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(new Color(41, 50, 65));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel lblLogo = new JLabel("Menú Principal");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblLogo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // Inicialización de botones
        JButton btnInicio = crearBotonMenu("Inicio");
        JButton btnPrestamos = crearBotonMenu("Gestión de Préstamos");
        JButton btnInventario = crearBotonMenu("Inventario");
        JButton btnUsuarios = crearBotonMenu("Gestión de Usuarios");
        btnConfigMora = crearBotonMenu("Configurar Mora Anual"); // Nombre más descriptivo
        JButton btnSalir = crearBotonMenu("Cerrar Sesión");

        // --- LÓGICA DE VISIBILIDAD POR ROL ---
        sidebar.add(btnInicio);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnPrestamos);

        if (rol == 1 || rol == 2) {
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebar.add(btnInventario);
        }

        if (rol == 1) { // Solo administradores ven Usuarios y Configuración
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebar.add(btnUsuarios);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebar.add(btnConfigMora); // <--- AGREGADO AL SIDEBAR
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnSalir);

        // --- PANEL CENTRAL ---
        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);

        JPanel panelBienvenida = new JPanel(new BorderLayout());
        panelBienvenida.setBackground(Color.WHITE);
        JLabel lblBienvenida = new JLabel("¡Bienvenido, " + nombreMostrado + "!", SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 36));
        panelBienvenida.add(lblBienvenida, BorderLayout.CENTER);

        // El panel de préstamos (y catálogo) lo ven todos
        com.biblioteca.view.panels.PanelPrestamos panelPrestamos = new com.biblioteca.view.panels.PanelPrestamos();

        panelCentral.add(panelBienvenida, "INICIO");
        panelCentral.add(panelPrestamos, "PRESTAMOS");

        // Solo instanciamos el Inventario si es Admin(1) o Profesor(2)
        if (rol == 1 || rol == 2) {
            PanelInventario panelInventario = new PanelInventario();
            panelCentral.add(panelInventario, "INVENTARIO");
        }

        // Solo instanciamos Usuarios si es Admin(1)
        if (rol == 1) {
            PanelUsuarios panelUsuarios = new PanelUsuarios();
            panelCentral.add(panelUsuarios, "USUARIOS");
        }

        add(sidebar, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);

        // --- ACCIONES DE LOS BOTONES ---
        btnInicio.addActionListener(e -> cardLayout.show(panelCentral, "INICIO"));
        btnPrestamos.addActionListener(e -> cardLayout.show(panelCentral, "PRESTAMOS"));
        btnInventario.addActionListener(e -> cardLayout.show(panelCentral, "INVENTARIO"));
        btnUsuarios.addActionListener(e -> cardLayout.show(panelCentral, "USUARIOS"));

        // ACCIÓN PARA CONFIGURAR MORA
        btnConfigMora.addActionListener(e -> {
            DialogConfigurarMora dialog = new DialogConfigurarMora(this);
            dialog.setVisible(true);
            // Al regresar, si el usuario está viendo préstamos, los datos se actualizarán solos
            // la próxima vez que abra un detalle o devolución.
        });

        btnSalir.addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(
                    this, "¿Seguro que deseas cerrar sesión?", "Cerrar Sesión",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );
            if (confirmacion == JOptionPane.YES_OPTION) {
                com.biblioteca.util.SessionManager.getInstance().cerrarSesion();
                this.dispose();
                new LoginFrame().setVisible(true);
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
        btn.setMaximumSize(new Dimension(240, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto visual simple al pasar el mouse
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(80, 110, 150));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(61, 90, 128));
            }
        });

        return btn;
    }
}