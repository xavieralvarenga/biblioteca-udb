package com.biblioteca.view.forms;

import com.biblioteca.model.Usuario;
import com.biblioteca.service.UsuarioService;

import javax.swing.*;
import java.awt.*;

public class DialogEditarUsuario extends JDialog {

    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtCarnet;

    private JComboBox<String> cbxRol;
    private JComboBox<String> cbxEstado;

    private JButton btnGuardar;
    private JButton btnCancelar;

    private Usuario usuario;

    public DialogEditarUsuario(Window parent, Usuario usuario) {
        super(parent, "Editar Usuario", ModalityType.APPLICATION_MODAL);

        this.usuario = usuario;

        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10,10));

        initComponents();
        cargarDatos();

        setVisible(true);
    }

    private void initComponents() {

        JPanel panelForm = new JPanel(new GridLayout(6,2,10,10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        panelForm.add(new JLabel("Nombres:"));
        txtNombres = new JTextField();
        panelForm.add(txtNombres);

        panelForm.add(new JLabel("Apellidos:"));
        txtApellidos = new JTextField();
        panelForm.add(txtApellidos);

        panelForm.add(new JLabel("Carnet:"));
        txtCarnet = new JTextField();
        panelForm.add(txtCarnet);

        panelForm.add(new JLabel("Rol:"));
        cbxRol = new JComboBox<>(new String[]{
                "Administrador",
                "Profesor",
                "Alumno"
        });
        panelForm.add(cbxRol);

        panelForm.add(new JLabel("Estado:"));
        cbxEstado = new JComboBox<>(new String[]{
                "Activo",
                "Inactivo",
                "Suspendido"
        });
        panelForm.add(cbxEstado);

        add(panelForm, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnCancelar = new JButton("Cancelar");
        btnGuardar = new JButton("Guardar");

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        btnCancelar.addActionListener(e -> dispose());

        btnGuardar.addActionListener(e -> guardarCambios());
    }

    private void cargarDatos() {

        txtNombres.setText(usuario.getNombres());
        txtApellidos.setText(usuario.getApellidos());
        txtCarnet.setText(usuario.getCarnet());

        cbxRol.setSelectedItem(usuario.getTipoUsuario().getNombreRol());

        cbxEstado.setSelectedItem(usuario.getEstado());
    }

    private void guardarCambios() {

        try {

            usuario.setNombres(txtNombres.getText().trim());
            usuario.setApellidos(txtApellidos.getText().trim());
            usuario.setCarnet(txtCarnet.getText().trim());

            usuario.setEstado((String) cbxEstado.getSelectedItem());

            // Convertir rol a ID
            int idRol = switch ((String) cbxRol.getSelectedItem()) {
                case "Administrador" -> 1;
                case "Profesor" -> 2;
                default -> 3;
            };

            usuario.getTipoUsuario().setIdTipo(idRol);

            UsuarioService service = new UsuarioService();
            service.actualizarUsuario(usuario);

            JOptionPane.showMessageDialog(this,
                    "Usuario actualizado correctamente");

            dispose();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
