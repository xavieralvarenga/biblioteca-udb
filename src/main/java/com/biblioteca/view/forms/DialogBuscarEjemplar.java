package com.biblioteca.view.forms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogBuscarEjemplar extends JDialog {

    private JTable tablaEjemplares;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;

    // Variables para guardar los datos del ejemplar seleccionado
    private int idEjemplarSeleccionado = -1;
    private String codigoBarrasSeleccionado = null;
    private String tituloSeleccionado = null;
    private String estadoSeleccionado = null;

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

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setBackground(new Color(61, 90, 128));
        btnFiltrar.setForeground(Color.WHITE);
        panelNorte.add(btnFiltrar);

        mainPanel.add(panelNorte, BorderLayout.NORTH);

        // --- TABLA DE EJEMPLARES ---
        // Basado en tu ER: Ejemplar + Documento
        String[] columnas = {"ID Ejemplar", "Cód. Barras", "Título", "Autor", "Estado"};
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
        cargarDatosDePrueba(); // Temporal hasta que hagamos el DAO de Ejemplares

        btnFiltrar.addActionListener(e -> JOptionPane.showMessageDialog(this, "Filtrando: " + txtBuscar.getText()));
        txtBuscar.addActionListener(e -> btnFiltrar.doClick());

        btnSeleccionar.addActionListener(e -> confirmarSeleccion());
        tablaEjemplares.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) confirmarSeleccion();
            }
        });
    }

    // Datos simulados (Luego conectaremos esto a MySQL con un EjemplarDAO)
    private void cargarDatosDePrueba() {
        modeloTabla.addRow(new Object[]{1, "EJ-001", "El Principito", "Antoine de Saint-Exupéry", "Disponible"});
        modeloTabla.addRow(new Object[]{2, "EJ-002", "Cálculo 1", "James Stewart", "Prestado"});
        modeloTabla.addRow(new Object[]{3, "EJ-003", "Física Universitaria", "Sears Zemansky", "Disponible"});
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
        estadoSeleccionado = (String) modeloTabla.getValueAt(fila, 4);

        this.dispose(); // Cerramos el buscador
    }

    // Getters para que la ventana principal pueda leer qué elegimos
    public int getIdEjemplarSeleccionado() { return idEjemplarSeleccionado; }
    public String getCodigoBarrasSeleccionado() { return codigoBarrasSeleccionado; }
    public String getTituloSeleccionado() { return tituloSeleccionado; }
    public String getEstadoSeleccionado() { return estadoSeleccionado; }
}