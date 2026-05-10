package com.biblioteca.view.forms;

import com.biblioteca.repository.impl.PrestamoDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DialogDevolucion extends JDialog {

    private int idPrestamo;
    private int idEjemplar;
    private int idUsuario;
    private int diasRetraso;
    private double montoMora;
    private JTextField txtMontoPagado;

    private JTextArea txtObservaciones;

    public DialogDevolucion(Window owner, int idPrestamo,int idUsuario, String carnet, String titulo, LocalDate fechaLimite, int idEjemplar, double valorMoraDiaria) {
        super(owner, "Procesar Devolución", ModalityType.APPLICATION_MODAL);
        this.idPrestamo = idPrestamo;
        this.idEjemplar = idEjemplar;
        this.idUsuario = idUsuario;

        setSize(450, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- CÁLCULOS MATEMÁTICOS DE MORA ---
        LocalDate hoy = LocalDate.now();
        long dias = ChronoUnit.DAYS.between(fechaLimite, hoy);
        this.diasRetraso = Math.max(0, (int) dias); // Si devolvió antes, los días son 0
        this.montoMora = this.diasRetraso * valorMoraDiaria;

        // --- INTERFAZ VISUAL ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Resumen
        mainPanel.add(new JLabel("Lector: " + carnet));
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(new JLabel("Material: " + titulo));
        mainPanel.add(Box.createVerticalStrut(15));

        // Panel de Mora (Se pinta rojo si hay retraso)
        JPanel panelMora = new JPanel(new GridLayout(2, 1));
        panelMora.setBackground(this.diasRetraso > 0 ? new Color(255, 235, 238) : new Color(232, 245, 233));
        panelMora.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(this.diasRetraso > 0 ? Color.RED : new Color(41, 171, 135)),
                new EmptyBorder(10, 10, 10, 10)
        ));


        // ... (dentro del constructor, después del panel de Mora) ...
        JPanel panelPago = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPago.setBackground(Color.WHITE);
        panelPago.add(new JLabel("Monto que paga el usuario ($):"));

        txtMontoPagado = new JTextField(8);
        txtMontoPagado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Por defecto, sugerimos el pago total si hay mora
        txtMontoPagado.setText(String.format("%.2f", this.montoMora));
        panelPago.add(txtMontoPagado);



        JLabel lblDias = new JLabel("Días de retraso: " + this.diasRetraso);
        lblDias.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel lblMonto = new JLabel("Mora a pagar: $" + String.format("%.2f", this.montoMora));
        lblMonto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMonto.setForeground(this.montoMora > 0 ? Color.RED : new Color(41, 171, 135));

        panelMora.add(lblDias);
        panelMora.add(lblMonto);
        mainPanel.add(panelMora);
        mainPanel.add(panelPago);
        mainPanel.add(Box.createVerticalStrut(15));



        // Observaciones Físicas
        mainPanel.add(new JLabel("Observaciones sobre el estado físico:"));
        txtObservaciones = new JTextArea(3, 20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setText("Devuelto en buenas condiciones.");
        mainPanel.add(new JScrollPane(txtObservaciones));

        add(mainPanel, BorderLayout.CENTER);

        // --- BOTONES ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(Color.WHITE);
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnConfirmar = new JButton("Confirmar Devolución");
        btnConfirmar.setBackground(new Color(41, 171, 135));
        btnConfirmar.setForeground(Color.WHITE);

        panelBotones.add(btnCancelar);
        panelBotones.add(btnConfirmar);
        add(panelBotones, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnCancelar.addActionListener(e -> this.dispose());

        btnConfirmar.addActionListener(e -> {
            try {
                double pagoIngresado = Double.parseDouble(txtMontoPagado.getText().replace(",", "."));

                if (pagoIngresado < 0 || pagoIngresado > montoMora) {
                    JOptionPane.showMessageDialog(this, "El monto pagado no puede ser negativo ni mayor a la mora calculada.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                PrestamoDAO dao = new PrestamoDAO();
                // Necesitamos pasar también el idUsuario a este constructor para la transacción
                dao.registrarDevolucionConPago(idPrestamo, idEjemplar, idUsuario, diasRetraso, montoMora, pagoIngresado, txtObservaciones.getText());

                String mensaje = (pagoIngresado < montoMora && montoMora > 0)
                        ? "Devolución registrada. El usuario aún debe $" + String.format("%.2f", (montoMora - pagoIngresado)) + " y su cuenta seguirá bloqueada."
                        : "Devolución y pago registrados con éxito. Usuario solvente.";

                JOptionPane.showMessageDialog(this, mensaje, "Proceso Completado", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresa un monto numérico válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}