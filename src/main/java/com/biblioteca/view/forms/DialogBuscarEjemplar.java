package com.biblioteca.view.forms;

import com.biblioteca.repository.impl.EjemplarDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DialogBuscarEjemplar extends JDialog {

    private JTable tablaEjemplares;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cbxTipoDoc;

    // Variables para guardar los datos del ejemplar seleccionado
    private int idEjemplarSeleccionado = -1;
    private String codigoBarrasSeleccionado = null;
    private String tituloSeleccionado = null;
    private String estadoSeleccionado = null;
    private String tipoSeleccionado = null;

    public DialogBuscarEjemplar(Window owner) {
        super(owner, "Seleccionar Material (Ejemplar)", ModalityType.APPLICATION_MODAL);
        setSize(650, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // --- BARRA DE BÚSQUEDA ---

        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelNorte.setBackground(Color.WHITE);
        panelNorte.add(new JLabel("Buscar (Título, Autor o Cód. Barras): "));
        txtBuscar = new JTextField(25);
        panelNorte.add(txtBuscar);

        panelNorte.add(new JLabel("Tipo:"));
        cbxTipoDoc = new JComboBox<>(new String[]{"Todos", "Libro", "Revista", "CD"});
        panelNorte.add(cbxTipoDoc);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setBackground(new Color(61, 90, 128));
        btnFiltrar.setForeground(Color.WHITE);
        panelNorte.add(btnFiltrar);

        mainPanel.add(panelNorte, BorderLayout.NORTH);

        // --- TABLA DE EJEMPLARES ---
        // Basado en tu ER: Ejemplar + Documento
        String[] columnas = {"ID Ejemplar", "Cód. Barras", "Título", "Autor","Tipo", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaEjemplares = new JTable(modeloTabla);
        tablaEjemplares.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Damos más espacio a la columna del Título
        tablaEjemplares.getColumnModel().getColumn(2).setPreferredWidth(200);

        mainPanel.add(new JScrollPane(tablaEjemplares), BorderLayout.CENTER);

        // --- BOTÓN DE SELECCIÓN ---
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.setBackground(Color.WHITE);
        JButton btnSeleccionar = new JButton("Seleccionar Material");
        btnSeleccionar.setBackground(new Color(41, 171, 135));
        btnSeleccionar.setForeground(Color.WHITE);
        panelSur.add(btnSeleccionar);
        mainPanel.add(panelSur, BorderLayout.SOUTH);

        add(mainPanel);

        // --- EVENTOS ---
        // Temporal hasta que hagamos el DAO de Ejemplares

        btnFiltrar.addActionListener(e -> filtrarDatos());
        txtBuscar.addActionListener(e -> filtrarDatos()); // Filtra al dar Enter

        // ¡Nuevo! Filtra automáticamente al cambiar el ComboBox (Libro, Revista, etc.)
        cbxTipoDoc.addActionListener(e -> filtrarDatos());

        btnSeleccionar.addActionListener(e -> confirmarSeleccion());

        tablaEjemplares.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) confirmarSeleccion();
            }
        });
        filtrarDatos();
    }

    // Datos simulados (Luego conectaremos esto a MySQL con un EjemplarDAO)
    private void filtrarDatos() {
        String texto = txtBuscar.getText().trim();
        String tipoStr = (String) cbxTipoDoc.getSelectedItem();

        // Mapeo de IDs según el script SQL que ejecutamos
        int idTipo = 0; // "Todos"
        if ("Libro".equals(tipoStr)) idTipo = 1;
        else if ("Revista".equals(tipoStr)) idTipo = 2;
        else if ("CD".equals(tipoStr)) idTipo = 3;

        modeloTabla.setRowCount(0); // Limpia la tabla antes de rellenar

        try {
            com.biblioteca.repository.impl.EjemplarDAO dao = new com.biblioteca.repository.impl.EjemplarDAO();
            java.util.List<Object[]> lista = dao.buscarEjemplares(texto, idTipo);

            for (Object[] fila : lista) {
                modeloTabla.addRow(fila);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de BD al cargar ejemplares: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void confirmarSeleccion() {
        int fila = tablaEjemplares.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor selecciona un material de la lista.");
            return;
        }

        // Extraemos los datos de la tabla
        idEjemplarSeleccionado = (int) modeloTabla.getValueAt(fila, 0);
        codigoBarrasSeleccionado = (String) modeloTabla.getValueAt(fila, 1);
        tituloSeleccionado = (String) modeloTabla.getValueAt(fila, 2);
        tipoSeleccionado = (String) modeloTabla.getValueAt(fila, 4);
        estadoSeleccionado = (String) modeloTabla.getValueAt(fila, 5);

        this.dispose(); // Cerramos el buscador
    }

    // Getters para que la ventana principal pueda leer qué elegimos
    public int getIdEjemplarSeleccionado() { return idEjemplarSeleccionado; }
    public String getCodigoBarrasSeleccionado() { return codigoBarrasSeleccionado; }
    public String getTituloSeleccionado() { return tituloSeleccionado; }
    public String getEstadoSeleccionado() { return estadoSeleccionado; }
    public String getTipoSeleccionado() { return tipoSeleccionado; }
}