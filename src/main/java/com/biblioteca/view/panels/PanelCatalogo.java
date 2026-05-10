package com.biblioteca.view.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelCatalogo extends JPanel {
    private JTable tablaDocs;
    private DefaultTableModel modelo;

    public PanelCatalogo() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("Catálogo de Documentos Disponibles");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(lblTitulo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"Título", "Autor", "Tipo", "Ubicación"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDocs = new JTable(modelo);
        add(new JScrollPane(tablaDocs), BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        // Aquí llamarías a un método de DocumentoDAO que solo traiga la info básica
        // Por ahora, puedes reutilizar la lógica de consulta de documentos que ya tienes
    }
}