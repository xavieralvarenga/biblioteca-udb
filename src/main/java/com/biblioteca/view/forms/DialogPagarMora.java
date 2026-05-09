package com.biblioteca.view.forms;

import com.biblioteca.repository.impl.PrestamoDAO;
import javax.swing.*;
import java.awt.*;

public class DialogPagarMora extends JDialog {
    private JTextField txtAbono;
    private double saldoPendiente;

    public DialogPagarMora(Window owner, int idPrestamo, int idUsuario, String lector, double total, double pagado) {
        super(owner, "Cobro de Mora Pendiente", ModalityType.APPLICATION_MODAL);
        this.saldoPendiente = total - pagado;

        setSize(350, 250);
        setLocationRelativeTo(owner);
        setLayout(new GridLayout(5, 1, 10, 10));

        add(new JLabel("  Lector: " + lector));
        add(new JLabel("  Saldo Pendiente: $" + String.format("%.2f", saldoPendiente)));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Monto a pagar: $"));
        txtAbono = new JTextField(10);
        txtAbono.setText(String.format("%.2f", saldoPendiente));
        p.add(txtAbono);
        add(p);

        JButton btnCobrar = new JButton("Registrar Pago");
        btnCobrar.setBackground(new Color(41, 171, 135));
        btnCobrar.setForeground(Color.WHITE);

        btnCobrar.addActionListener(e -> {
            try {
                double abono = Double.parseDouble(txtAbono.getText());
                if (abono <= 0 || abono > saldoPendiente + 0.01) {
                    JOptionPane.showMessageDialog(this, "Monto inválido.");
                    return;
                }

                new PrestamoDAO().abonarMora(idPrestamo, idUsuario, abono);
                JOptionPane.showMessageDialog(this, "Pago registrado. Solvencia actualizada.");
                this.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        add(btnCobrar);
    }
}