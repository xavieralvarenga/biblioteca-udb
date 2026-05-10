package com.biblioteca.view.forms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DialogDetalleDocumento extends JDialog {

    private JTextField txtTitulo, txtAutor, txtUbicacion, txtCodigoBarras, txtEstado;
    private JComboBox<String> cbxTipoDocumento;
    private JPanel panelDetallesDinamico;
    private CardLayout cardLayout;

    // Recibimos los datos básicos desde la tabla para simular que cargamos de la BD
    public DialogDetalleDocumento(Window owner, String id, String tipo, String titulo, String autor, String ubicacion, String estado) {
        super(owner, "Detalle y Edición de Documento - ID: " + id, ModalityType.APPLICATION_MODAL);
        setSize(500, 650); // Un poco más alto para incluir el Estado
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // 1. DATOS GENERALES
        JPanel panelGeneral = new JPanel(new GridLayout(6, 2, 10, 15)); // 6 filas ahora
        panelGeneral.setBackground(Color.WHITE);
        panelGeneral.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Información General",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(61, 90, 128)
        ));

        // Rellenamos con los datos que vienen de la tabla
        txtTitulo = agregarCampoFormulario(panelGeneral, "Título de la Obra *:", titulo);
        txtAutor = agregarCampoFormulario(panelGeneral, "Autor / Creador *:", autor);
        txtUbicacion = agregarCampoFormulario(panelGeneral, "Ubicación Física:", ubicacion);
        txtCodigoBarras = agregarCampoFormulario(panelGeneral, "Código de Barras (Obra):", "OBR-" + id + "000"); // Simulado
        txtEstado = agregarCampoFormulario(panelGeneral, "Estado Actual:", estado);
        txtEstado.setEditable(false); // El estado no se edita a mano, cambia con préstamos

        panelGeneral.add(new JLabel(" Tipo de Documento:"));
        cbxTipoDocumento = new JComboBox<>(new String[]{"Libro", "Revista", "CD"});
        cbxTipoDocumento.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbxTipoDocumento.setSelectedItem(tipo);
        cbxTipoDocumento.setEnabled(false); // BLOQUEADO: No puedes convertir un libro en un CD
        panelGeneral.add(cbxTipoDocumento);

        mainPanel.add(panelGeneral);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. DATOS ESPECÍFICOS (Cargamos la carta según el tipo)
        cardLayout = new CardLayout();
        panelDetallesDinamico = new JPanel(cardLayout);
        panelDetallesDinamico.setBackground(Color.WHITE);
        panelDetallesDinamico.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Detalles Específicos (" + tipo + ")",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(244, 162, 97) // Color naranja para edición
        ));

        // Paneles simulados con datos dummy dependiendo de lo que elijas
        JPanel panelLibro = new JPanel(new GridLayout(3, 2, 10, 15));
        panelLibro.setBackground(Color.WHITE);
        agregarCampoFormulario(panelLibro, "ISBN:", tipo.equals("Libro") ? "978-3-16-148410-0" : "");
        agregarCampoFormulario(panelLibro, "Editorial:", tipo.equals("Libro") ? "Editorial Planeta" : "");
        agregarCampoFormulario(panelLibro, "Edición:", tipo.equals("Libro") ? "3ra Edición" : "");

        JPanel panelRevista = new JPanel(new GridLayout(3, 2, 10, 15));
        panelRevista.setBackground(Color.WHITE);
        agregarCampoFormulario(panelRevista, "ISSN:", tipo.equals("Revista") ? "2049-3630" : "");
        agregarCampoFormulario(panelRevista, "Volumen:", tipo.equals("Revista") ? "Vol. 45" : "");
        agregarCampoFormulario(panelRevista, "Mes de Publicación:", tipo.equals("Revista") ? "Mayo 2026" : "");

        JPanel panelCD = new JPanel(new GridLayout(2, 2, 10, 15));
        panelCD.setBackground(Color.WHITE);
        agregarCampoFormulario(panelCD, "Duración (Minutos):", tipo.equals("CD") ? "120 min" : "");
        agregarCampoFormulario(panelCD, "Tipo de Contenido:", tipo.equals("CD") ? "Música Clásica" : "");

        panelDetallesDinamico.add(panelLibro, "Libro");
        panelDetallesDinamico.add(panelRevista, "Revista");
        panelDetallesDinamico.add(panelCD, "CD");

        // Mostramos la carta correcta
        cardLayout.show(panelDetallesDinamico, tipo);

        mainPanel.add(panelDetallesDinamico);
        add(mainPanel, BorderLayout.CENTER);

        // 3. BOTONES DE ACCIÓN
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBotones.setBackground(Color.WHITE);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setBackground(Color.GRAY);
        btnCancelar.setForeground(Color.WHITE);

        JButton btnGuardar = new JButton("Actualizar Datos");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setBackground(new Color(41, 171, 135)); // Verde para actualizar
        btnGuardar.setForeground(Color.WHITE);

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnCancelar.addActionListener(e -> this.dispose());

        btnGuardar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Datos de '" + txtTitulo.getText() + "' actualizados correctamente.", "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        });
    }

    // Método auxiliar modificado para que acepte un valor por defecto (pre-llenado)
    private JTextField agregarCampoFormulario(JPanel panel, String textoEtiqueta, String valorInicial) {
        JLabel label = new JLabel(" " + textoEtiqueta);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField textField = new JTextField(valorInicial);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(label);
        panel.add(textField);
        return textField;
    }
}