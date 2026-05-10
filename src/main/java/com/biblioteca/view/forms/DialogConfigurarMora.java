package com.biblioteca.view.forms;

import com.biblioteca.service.MoraAnualService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DialogConfigurarMora extends JDialog {
    private JTable tablaMoras;
    private DefaultTableModel modelo;
    private JTextField txtAnio, txtTarifa;
    private JButton btnGuardar;
    private final MoraAnualService service = new MoraAnualService();

    public DialogConfigurarMora(Window owner) {
        super(owner, "Configuración de Mora Anual", ModalityType.APPLICATION_MODAL);
        setSize(400, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // --- Panel de Formulario (Norte) ---
        JPanel pnlForm = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlForm.setBorder(new EmptyBorder(20, 20, 10, 20));
        pnlForm.setBackground(Color.WHITE);

        pnlForm.add(new JLabel("Año:"));
        txtAnio = new JTextField(String.valueOf(java.time.Year.now().getValue()));
        pnlForm.add(txtAnio);

        pnlForm.add(new JLabel("Tarifa Diaria ($):"));
        txtTarifa = new JTextField();
        pnlForm.add(txtTarifa);
        add(pnlForm, BorderLayout.NORTH);

        // --- Tabla (Centro) ---
        modelo = new DefaultTableModel(new String[]{"Año", "Tarifa Diaria"}, 0);
        tablaMoras = new JTable(modelo);
        add(new JScrollPane(tablaMoras), BorderLayout.CENTER);

        // --- Botones (Sur) ---
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotones.setBackground(Color.WHITE);
        btnGuardar = new JButton("Guardar / Actualizar");
        btnGuardar.setBackground(new Color(41, 171, 135));
        btnGuardar.setForeground(Color.WHITE);
        pnlBotones.add(btnGuardar);
        add(pnlBotones, BorderLayout.SOUTH);

        cargarDatos();
        configurarEventos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        service.listarTarifas().forEach(modelo::addRow);
    }

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> {
            try {
                int anio = Integer.parseInt(txtAnio.getText());
                double tarifa = Double.parseDouble(txtTarifa.getText());

                service.registrarTarifa(anio, tarifa);
                JOptionPane.showMessageDialog(this, "Tarifa configurada para el año " + anio);
                cargarDatos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: Verifique que el año y la tarifa sean números válidos.");
            }
        });

        // Al tocar la tabla, cargar los datos en las cajas de texto para editar
        tablaMoras.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaMoras.getSelectedRow();
            if (fila != -1) {
                txtAnio.setText(modelo.getValueAt(fila, 0).toString());
                txtTarifa.setText(modelo.getValueAt(fila, 1).toString());
            }
        });
    }
}