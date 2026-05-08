package com.biblioteca.view.panels;

import com.biblioteca.view.forms.DialogEditarUsuario;
import com.biblioteca.view.forms.DialogNuevoUsuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelUsuarios extends JPanel {

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxFiltroRol;
    private JComboBox<String> cbxFiltroMora;   // <--- NUEVO
    private JComboBox<String> cbxFiltroEstado;
    private JButton btnBuscar, btnNuevo, btnEditar, btnEliminar, btnResetPassword;

    public PanelUsuarios() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        //Barra de busqueda
        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));
        panelNorte.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Gestión de Usuarios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel();
        panelBusqueda.setLayout(new BoxLayout(panelBusqueda, BoxLayout.Y_AXIS));
        panelBusqueda.setBackground(Color.WHITE);
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fila1.setBackground(Color.WHITE);

        fila1.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(30); // Lo hice un poco más largo para que ocupe bien el espacio
        txtBuscar.putClientProperty("JTextField.placeholderText", "Carnet, Nombres o Apellidos...");
        fila1.add(txtBuscar);

        // Filtros
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fila2.setBackground(Color.WHITE);

        fila2.add(new JLabel("Rol:"));
        cbxFiltroRol = new JComboBox<>(new String[]{"Todos", "Administrador", "Profesor", "Alumno"});
        fila2.add(cbxFiltroRol);

        fila2.add(new JLabel("Mora:"));
        cbxFiltroMora = new JComboBox<>(new String[]{"Todas", "Con Mora", "Sin Mora"});
        fila2.add(cbxFiltroMora);

        fila2.add(new JLabel("Estado:"));
        cbxFiltroEstado = new JComboBox<>(new String[]{"Todos", "Activo", "Inactivo"});
        fila2.add(cbxFiltroEstado);

        btnBuscar = new JButton("Filtrar Resultados");
        btnBuscar.setBackground(new Color(61, 90, 128));
        btnBuscar.setForeground(Color.WHITE);
        fila2.add(btnBuscar);

        panelBusqueda.add(fila1);
        panelBusqueda.add(fila2);

        panelNorte.add(panelBusqueda, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // Tabla de datos del usuario
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

        // Botones de acción
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
        cargarUsuariosDesdeBD();
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

    public void cargarUsuariosDesdeBD() {
        modeloTabla.setRowCount(0);

        try {
            com.biblioteca.service.UsuarioService service = new com.biblioteca.service.UsuarioService();
            java.util.List<com.biblioteca.model.Usuario> lista = service.obtenerTodos();

            //Recorrer la lista que vino de MySQL e insertarla fila por fila
            for (com.biblioteca.model.Usuario u : lista) {

                // Formateamos el booleano
                String textoMora = (u.getEstadoMora() != null && u.getEstadoMora()) ? "Sí (Mora)" : "No";

                modeloTabla.addRow(new Object[]{
                        u.getIdUsuario(),
                        u.getCarnet(),
                        u.getNombres(),
                        u.getApellidos(),
                        u.getTipoUsuario().getNombreRol(),
                        u.getEstado(),
                        textoMora
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la tabla: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> {
            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            com.biblioteca.view.forms.DialogNuevoUsuario dialog = new com.biblioteca.view.forms.DialogNuevoUsuario(ventanaPadre);
            dialog.setVisible(true);
            cargarUsuariosDesdeBD();
        });

        btnEditar.addActionListener(e -> {
            int fila = tablaUsuarios.getSelectedRow();

            if (fila == -1) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona un usuario primero.");
                return;
            }

            try {
                int idUsuario = (int) modeloTabla.getValueAt(fila, 0);
                com.biblioteca.service.UsuarioService service = new com.biblioteca.service.UsuarioService();
                com.biblioteca.model.Usuario usuario = service.obtenerPorId(idUsuario);
                Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
                new DialogEditarUsuario(ventanaPadre, usuario);

                cargarUsuariosDesdeBD();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });


        // Cambio de clave
        btnResetPassword.setText("Cambiar Clave");

        btnResetPassword.addActionListener(e -> {
            int fila = tablaUsuarios.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int idUsuario = (int) modeloTabla.getValueAt(fila, 0);
            String carnet = (String) modeloTabla.getValueAt(fila, 1);

            // 1. Creamos una mini-ventana personalizada (JDialog)
            Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
            JDialog dialogClave = new JDialog(ventanaPadre, "Cambiar Contraseña", Dialog.ModalityType.APPLICATION_MODAL);
            dialogClave.setSize(350, 250);
            dialogClave.setLocationRelativeTo(this); // Centra en la pantalla
            dialogClave.setLayout(new BorderLayout());

            // 2. Panel central con las cajas de texto
            JPanel panelPassword = new JPanel(new GridLayout(4, 1, 5, 5));
            panelPassword.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Un poco de margen

            panelPassword.add(new JLabel("Nueva contraseña para " + carnet + ":"));
            JPasswordField txtNuevaClave = new JPasswordField();
            txtNuevaClave.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panelPassword.add(txtNuevaClave);

            panelPassword.add(new JLabel("Confirmar nueva contraseña:"));
            JPasswordField txtConfirmarClave = new JPasswordField();
            txtConfirmarClave.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panelPassword.add(txtConfirmarClave);

            dialogClave.add(panelPassword, BorderLayout.CENTER);

            // 3. Panel inferior con los botones
            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnCancelar = new JButton("Cancelar");
            JButton btnGuardar = new JButton("Guardar");

            panelBotones.add(btnCancelar);
            panelBotones.add(btnGuardar);
            dialogClave.add(panelBotones, BorderLayout.SOUTH);

            // --- EVENTOS DE LA MINI-VENTANA ---

            // Si le da cancelar, destruimos solo esta ventanita
            btnCancelar.addActionListener(ev -> dialogClave.dispose());

            // Si le da guardar, hacemos las validaciones
            btnGuardar.addActionListener(ev -> {
                String pass1 = new String(txtNuevaClave.getPassword());
                String pass2 = new String(txtConfirmarClave.getPassword());

                // Validación 1: Vacías
                if (pass1.trim().isEmpty() || pass2.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialogClave, "Las contraseñas no pueden estar vacías.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // ¡El return detiene el proceso pero NO cierra la ventana!
                }

                // Validación 2: Coincidencia
                if (!pass1.equals(pass2)) {
                    JOptionPane.showMessageDialog(dialogClave, "Las contraseñas no coinciden. Revisa y vuelve a intentarlo.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // ¡Te lanza el error y te deja seguir corrigiendo ahí mismo!
                }

                // Si llegó hasta aquí, todo está perfecto
                try {
                    com.biblioteca.service.UsuarioService service = new com.biblioteca.service.UsuarioService();
                    service.restablecerPassword(idUsuario, pass1);
                    JOptionPane.showMessageDialog(dialogClave, "La contraseña de " + carnet + " ha sido actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    // ¡Solo cuando fue un éxito absoluto destruimos la ventana!
                    dialogClave.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogClave, ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Mostramos la ventana en pantalla
            dialogClave.setVisible(true);
        });

        // Activar o desactivar
        btnEliminar.addActionListener(e -> {
            int fila = tablaUsuarios.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int idUsuario = (int) modeloTabla.getValueAt(fila, 0);
            String estadoActual = (String) modeloTabla.getValueAt(fila, 5); // Columna 5 es Estado
            String accion = estadoActual.equalsIgnoreCase("Activo") ? "Desactivar" : "Activar";

            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que deseas " + accion.toLowerCase() + " a este usuario?",
                    "Confirmar Acción", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    com.biblioteca.service.UsuarioService service = new com.biblioteca.service.UsuarioService();
                    service.cambiarEstadoUsuario(idUsuario, estadoActual);

                    // Recargamos la tabla automáticamente para ver el cambio de color e inactividad
                    cargarUsuariosDesdeBD();

                    JOptionPane.showMessageDialog(this, "El usuario ha sido " + (accion.equals("Desactivar") ? "desactivado" : "activado") + " exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnBuscar.addActionListener(e -> {
            ejecutarFiltrado();
        });
        txtBuscar.addActionListener(e -> ejecutarFiltrado());

        // Evento que seleciona filas en la tabla
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() != -1) {
                String estado = (String) tablaUsuarios.getValueAt(tablaUsuarios.getSelectedRow(), 5);

                if ("Inactivo".equalsIgnoreCase(estado)) {
                    btnEliminar.setText("Activar");
                    btnEliminar.setBackground(new Color(41, 171, 135)); // Verde para activar
                } else {
                    btnEliminar.setText("Desactivar");
                    btnEliminar.setBackground(new Color(231, 111, 81)); // Rojo para desactivar
                }
            }
        });

    }
    // Método que recopila la info de la UI y llama al servicio
    private void ejecutarFiltrado() {
        String texto = txtBuscar.getText().trim();
        String rolSeleccionado = (String) cbxFiltroRol.getSelectedItem();
        String moraSeleccionada = (String) cbxFiltroMora.getSelectedItem();
        String estadoSeleccionado = (String) cbxFiltroEstado.getSelectedItem();

        Integer idRol = 0;
        if ("Administrador".equals(rolSeleccionado)) idRol = 1;
        else if ("Profesor".equals(rolSeleccionado)) idRol = 2;
        else if ("Alumno".equals(rolSeleccionado)) idRol = 3;

        Boolean mora = null;
        if ("Con Mora".equals(moraSeleccionada)) mora = true;
        else if ("Sin Mora".equals(moraSeleccionada)) mora = false;

        String estado = "Todos".equals(estadoSeleccionado) ? null : estadoSeleccionado;

        try {
            com.biblioteca.service.UsuarioService service = new com.biblioteca.service.UsuarioService();
            java.util.List<com.biblioteca.model.Usuario> listaFiltrada = service.buscarConFiltro(texto, idRol, mora, estado);

            modeloTabla.setRowCount(0); // Limpiar tabla

            for (com.biblioteca.model.Usuario u : listaFiltrada) {
                String textoMora = (u.getEstadoMora() != null && u.getEstadoMora()) ? "Sí (Mora)" : "No";
                modeloTabla.addRow(new Object[]{
                        u.getIdUsuario(),
                        u.getCarnet(),
                        u.getNombres(),
                        u.getApellidos(),
                        u.getTipoUsuario().getNombreRol(),
                        u.getEstado(),
                        textoMora
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar usuarios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}