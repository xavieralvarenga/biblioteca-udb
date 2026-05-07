package com.biblioteca.view.forms;

import com.biblioteca.model.*;
import com.biblioteca.service.IDocumentoService;
import com.biblioteca.service.impl.DocumentoServiceImpl;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;

public class DialogNuevoDocumento extends JDialog {
    // Campos Comunes
    private JTextField txtTitulo, txtAutor, txtUbicacion, txtCodigoBarras;
    private JComboBox<String> cbxTipoDocumento;

    // Campos Específicos (Guardamos referencias para poder leer el texto luego)
    private JTextField txtIsbn, txtEditorial, txtEdicion;
    private JTextField txtIssn, txtVolumen, txtMes;
    private JTextField txtDuracion, txtContenido;

    private CardLayout cardLayout;
    private JPanel panelDetallesDinamico;

    public DialogNuevoDocumento(Window owner) {
        super(owner, "Registrar Nuevo Documento", ModalityType.APPLICATION_MODAL);
        initComponents();
        configurarEventos();
    }

    private void initComponents() {
        setSize(500, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Sección General
        JPanel panelGeneral = new JPanel(new GridLayout(5, 2, 10, 15));
        panelGeneral.setBackground(Color.WHITE);
        panelGeneral.setBorder(BorderFactory.createTitledBorder("Datos Generales"));

        txtTitulo = crearCampo(panelGeneral, "Título *:");
        txtAutor = crearCampo(panelGeneral, "Autor *:");
        txtUbicacion = crearCampo(panelGeneral, "Ubicación:");
        txtCodigoBarras = crearCampo(panelGeneral, "Cód. Barras:");

        cbxTipoDocumento = new JComboBox<>(new String[]{"Libro", "Revista", "CD"});
        panelGeneral.add(new JLabel(" Tipo:"));
        panelGeneral.add(cbxTipoDocumento);
        mainPanel.add(panelGeneral);

        // Sección Dinámica
        cardLayout = new CardLayout();
        panelDetallesDinamico = new JPanel(cardLayout);

        // Paneles de detalles
        JPanel pLibro = new JPanel(new GridLayout(3, 2, 10, 15));
        txtIsbn = crearCampo(pLibro, "ISBN:");
        txtEditorial = crearCampo(pLibro, "Editorial:");
        txtEdicion = crearCampo(pLibro, "Edición:");

        JPanel pRevista = new JPanel(new GridLayout(3, 2, 10, 15));
        txtIssn = crearCampo(pRevista, "ISSN:");
        txtVolumen = crearCampo(pRevista, "Volumen:");
        txtMes = crearCampo(pRevista, "Mes Pub:");

        JPanel pCd = new JPanel(new GridLayout(2, 2, 10, 15));
        txtDuracion = crearCampo(pCd, "Duración (min):");
        txtContenido = crearCampo(pCd, "Contenido:");

        panelDetallesDinamico.add(pLibro, "Libro");
        panelDetallesDinamico.add(pRevista, "Revista");
        panelDetallesDinamico.add(pCd, "CD");
        mainPanel.add(panelDetallesDinamico);

        add(mainPanel, BorderLayout.CENTER);

        // Botones
        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> accionGuardar());
        pBotones.add(btnGuardar);
        add(pBotones, BorderLayout.SOUTH);
    }

    private JTextField crearCampo(JPanel p, String label) {
        p.add(new JLabel(label));
        JTextField t = new JTextField();
        p.add(t);
        return t;
    }

    private void configurarEventos() {
        cbxTipoDocumento.addActionListener(e -> cardLayout.show(panelDetallesDinamico, (String) cbxTipoDocumento.getSelectedItem()));
    }

    private void accionGuardar() {
        try {
            String tipo = (String) cbxTipoDocumento.getSelectedItem();
            Documento d;

            // Instanciación polimórfica según selección
            if (tipo.equals("Libro")) {
                Libro l = new Libro();
                l.setIsbn(txtIsbn.getText());
                l.setEditorial(txtEditorial.getText());
                l.setEdicion(txtEdicion.getText());
                l.setTipoDocumento(1);
                d = l;
            } else if (tipo.equals("Revista")) {
                Revista r = new Revista();
                r.setIssn(txtIssn.getText());
                r.setVolumen(txtVolumen.getText());
                r.setMesPublicacion(txtMes.getText());
                r.setTipoDocumento(2);
                d = r;
            } else {
                Cd c = new Cd();
                c.setDuracionMinutos(Integer.parseInt(txtDuracion.getText().isEmpty() ? "0" : txtDuracion.getText()));
                c.setTipoContenido(txtContenido.getText());
                c.setTipoDocumento(3);
                d = c;
            }

            // Datos comunes
            d.setTitulo(txtTitulo.getText());
            d.setAutor(txtAutor.getText());
            d.setUbicacionFisica(txtUbicacion.getText());
            d.setCodigoBarrasObra(txtCodigoBarras.getText());
            d.setEstado("Disponible");

            // Llamada al servicio manejando la excepción SQL
            IDocumentoService service = new DocumentoServiceImpl();
            if (service.registrarDocumento(d)) {
                JOptionPane.showMessageDialog(this, "¡Documento guardado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error: Título y Autor son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La duración debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}