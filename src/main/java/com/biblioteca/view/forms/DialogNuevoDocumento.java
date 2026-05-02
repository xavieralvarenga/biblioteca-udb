package com.biblioteca.view.forms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DialogNuevoDocumento extends JDialog {

    // Componentes Comunes
    private JTextField txtTitulo, txtAutor, txtUbicacion, txtCodigoBarras;
    private JComboBox<String> cbxTipoDocumento;

    // Magia para el cambio dinámico
    private JPanel panelDetallesDinamico;
    private CardLayout cardLayout;

    public DialogNuevoDocumento(Window owner) {
        super(owner, "Registrar Nuevo Documento", ModalityType.APPLICATION_MODAL);
        setSize(500, 600);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Contenedor principal con padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // 1. SECCIÓN: DATOS GENERALES (Común para todos)
        JPanel panelGeneral = new JPanel(new GridLayout(5, 2, 10, 15));
        panelGeneral.setBackground(Color.WHITE);
        panelGeneral.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Datos Generales del Documento",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(61, 90, 128)
        ));

        // Agregamos campos comunes
        txtTitulo = agregarCampoFormulario(panelGeneral, "Título de la Obra *:");
        txtAutor = agregarCampoFormulario(panelGeneral, "Autor / Creador *:");
        txtUbicacion = agregarCampoFormulario(panelGeneral, "Ubicación Física:");
        txtCodigoBarras = agregarCampoFormulario(panelGeneral, "Código de Barras (Obra):");

        // El selector que hará la magia
        panelGeneral.add(new JLabel(" Tipo de Documento *:"));
        cbxTipoDocumento = new JComboBox<>(new String[]{"Libro", "Revista", "CD"});
        cbxTipoDocumento.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelGeneral.add(cbxTipoDocumento);

        mainPanel.add(panelGeneral);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. SECCIÓN: DATOS ESPECÍFICOS (El CardLayout dinámico)
        cardLayout = new CardLayout();
        panelDetallesDinamico = new JPanel(cardLayout);
        panelDetallesDinamico.setBackground(Color.WHITE);
        panelDetallesDinamico.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Detalles Específicos",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(41, 171, 135)
        ));

        // --- Panel para LIBRO ---
        JPanel panelLibro = new JPanel(new GridLayout(3, 2, 10, 15));
        panelLibro.setBackground(Color.WHITE);
        agregarCampoFormulario(panelLibro, "ISBN:");
        agregarCampoFormulario(panelLibro, "Editorial:");
        agregarCampoFormulario(panelLibro, "Edición:");

        // --- Panel para REVISTA ---
        JPanel panelRevista = new JPanel(new GridLayout(3, 2, 10, 15));
        panelRevista.setBackground(Color.WHITE);
        agregarCampoFormulario(panelRevista, "ISSN:");
        agregarCampoFormulario(panelRevista, "Volumen:");
        agregarCampoFormulario(panelRevista, "Mes de Publicación:");

        // --- Panel para CD ---
        JPanel panelCD = new JPanel(new GridLayout(2, 2, 10, 15));
        panelCD.setBackground(Color.WHITE);
        agregarCampoFormulario(panelCD, "Duración (Minutos):");
        agregarCampoFormulario(panelCD, "Tipo de Contenido:");

        // Añadimos las cartas al panel dinámico
        panelDetallesDinamico.add(panelLibro, "Libro");
        panelDetallesDinamico.add(panelRevista, "Revista");
        panelDetallesDinamico.add(panelCD, "CD");

        mainPanel.add(panelDetallesDinamico);

        add(mainPanel, BorderLayout.CENTER);

        // 3. SECCIÓN: BOTONES DE ACCIÓN (Sur)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBotones.setBackground(Color.WHITE);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setBackground(new Color(231, 111, 81));
        btnCancelar.setForeground(Color.WHITE);

        JButton btnGuardar = new JButton("Guardar Documento");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setBackground(new Color(61, 90, 128));
        btnGuardar.setForeground(Color.WHITE);

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        add(panelBotones, BorderLayout.SOUTH);

        // --- EVENTOS ---

        // Evento que escucha el menú desplegable y cambia la "carta"
        cbxTipoDocumento.addActionListener(e -> {
            String tipoSeleccionado = (String) cbxTipoDocumento.getSelectedItem();
            cardLayout.show(panelDetallesDinamico, tipoSeleccionado);
        });

        // Evento para cerrar la ventana
        btnCancelar.addActionListener(e -> this.dispose());

        // Evento de guardado (Solo visual por ahora)
        btnGuardar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Simulando el guardado en la base de datos...", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        });
    }

    // Método auxiliar para no repetir código creando etiquetas y campos de texto
    private JTextField agregarCampoFormulario(JPanel panel, String textoEtiqueta) {
        JLabel label = new JLabel(" " + textoEtiqueta);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(label);
        panel.add(textField);
        return textField;
    }
}