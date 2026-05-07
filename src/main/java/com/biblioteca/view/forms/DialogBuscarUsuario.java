package com.biblioteca.view.forms;

import com.biblioteca.model.Usuario;
import com.biblioteca.service.UsuarioService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DialogBuscarUsuario extends JDialog {

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;

    // Variable para guardar el usuario que el administrador elija
    private Usuario usuarioSeleccionado = null;

    public DialogBuscarUsuario(Window owner) {
        super(owner, "Seleccionar Lector", ModalityType.APPLICATION_MODAL);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // --- BARRA DE BÚSQUEDA ---
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelNorte.setBackground(Color.WHITE);
        panelNorte.add(new JLabel("Buscar (Nombre o Carnet): "));
        txtBuscar = new JTextField(25);
        panelNorte.add(txtBuscar);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setBackground(new Color(61, 90, 128));
        btnFiltrar.setForeground(Color.WHITE);
        panelNorte.add(btnFiltrar);

        mainPanel.add(panelNorte, BorderLayout.NORTH);

        // --- TABLA DE USUARIOS ---
        String[] columnas = {"ID", "Carnet", "Nombres", "Apellidos", "Rol", "Mora"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mainPanel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);

        // --- BOTÓN DE SELECCIÓN ---
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.setBackground(Color.WHITE);
        JButton btnSeleccionar = new JButton("Seleccionar Lector");
        btnSeleccionar.setBackground(new Color(41, 171, 135));
        btnSeleccionar.setForeground(Color.WHITE);
        panelSur.add(btnSeleccionar);
        mainPanel.add(panelSur, BorderLayout.SOUTH);

        add(mainPanel);

        // --- EVENTOS ---
        cargarTodosLosUsuarios(); // Carga inicial

        btnFiltrar.addActionListener(e -> filtrarUsuarios());
        txtBuscar.addActionListener(e -> filtrarUsuarios()); // Permite filtrar con tecla Enter

        // Evento principal: Confirmar selección
        btnSeleccionar.addActionListener(e -> confirmarSeleccion());

        // Permitir seleccionar con Doble Clic en la tabla
        tablaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    confirmarSeleccion();
                }
            }
        });
    }

    private void cargarTodosLosUsuarios() {
        try {
            UsuarioService service = new UsuarioService();
            llenarTabla(service.obtenerTodos());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filtrarUsuarios() {
        String texto = txtBuscar.getText().trim();
        try {
            UsuarioService service = new UsuarioService();
            // Usamos el método de búsqueda que ya habías creado en clases anteriores
            // Pasamos null a Rol, Mora y Estado para buscar en todos
            llenarTabla(service.buscarConFiltro(texto, 0, null, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void llenarTabla(List<Usuario> lista) {
        modeloTabla.setRowCount(0);
        for (Usuario u : lista) {
            String textoMora = (u.getEstadoMora() != null && u.getEstadoMora()) ? "Sí" : "No";
            modeloTabla.addRow(new Object[]{ u.getIdUsuario(), u.getCarnet(), u.getNombres(), u.getApellidos(), u.getTipoUsuario().getNombreRol(), textoMora });
        }
    }

    private void confirmarSeleccion() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor selecciona un usuario de la lista.");
            return;
        }

        // Recuperamos los datos de la fila seleccionada
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String carnet = (String) modeloTabla.getValueAt(fila, 1);
        String nombres = (String) modeloTabla.getValueAt(fila, 2);
        String apellidos = (String) modeloTabla.getValueAt(fila, 3);
        String rol = (String) modeloTabla.getValueAt(fila, 4);
        boolean tieneMora = "Sí".equals(modeloTabla.getValueAt(fila, 5));

        // Construimos el objeto para devolverlo a la ventana principal
        usuarioSeleccionado = new Usuario();
        usuarioSeleccionado.setIdUsuario(id);
        usuarioSeleccionado.setCarnet(carnet);
        usuarioSeleccionado.setNombres(nombres);
        usuarioSeleccionado.setApellidos(apellidos);
        usuarioSeleccionado.setEstadoMora(tieneMora);

        com.biblioteca.model.TipoUsuario tipo = new com.biblioteca.model.TipoUsuario();
        tipo.setNombreRol(rol);
        // Podrías agregar max_dias_prestamo aquí si lo extraes de la BD
        usuarioSeleccionado.setTipoUsuario(tipo);

        this.dispose(); // Cerramos el buscador
    }

    // Método que usará tu formulario principal para obtener el resultado
    public Usuario getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }
}