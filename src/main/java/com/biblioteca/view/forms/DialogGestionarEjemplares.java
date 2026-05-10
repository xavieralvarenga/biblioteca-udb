package com.biblioteca.view.forms;

import com.biblioteca.model.Documento;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.service.IEjemplarService;
import com.biblioteca.service.impl.EjemplarServiceImpl;
import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Diálogo para la gestión de ejemplares físicos vinculados a un documento.
 * Permite visualizar el inventario físico y registrar nuevas copias.
 */
@Log
public class DialogGestionarEjemplares extends JDialog {
    private final Integer idDocumento;
    private final String tituloDoc;
    private final IEjemplarService ejemplarService;

    private JTable tablaEjemplares;
    private DefaultTableModel modelo;

    public DialogGestionarEjemplares(Window owner, Integer idDocumento, String tituloDoc) {
        super(owner, "Ejemplares de: " + tituloDoc, ModalityType.APPLICATION_MODAL);
        this.idDocumento = idDocumento;
        this.tituloDoc = tituloDoc;
        this.ejemplarService = new EjemplarServiceImpl();

        initComponentes();
        cargarEjemplares();
    }

    private void initComponentes() {
        setSize(600, 450);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(15, 15));

        // --- Panel Norte: Información ---
        JPanel pnlInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlInfo.setBorder(BorderFactory.createTitledBorder("Información de Registro"));
        JLabel lblId = new JLabel("Documento ID: " + idDocumento + " | Título: " + tituloDoc);
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlInfo.add(lblId);
        add(pnlInfo, BorderLayout.NORTH);

        // --- Centro: Tabla de Ejemplares ---
        String[] columnas = {"ID Ejemplar", "Código de Barras", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaEjemplares = new JTable(modelo);
        tablaEjemplares.setRowHeight(25);
        add(new JScrollPane(tablaEjemplares), BorderLayout.CENTER);

        // --- Sur: Botones de Acción ---
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnNuevo = new JButton("Registrar Nueva Copia");
        btnNuevo.setBackground(new Color(41, 171, 135)); // Verde esmeralda para registro
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnCerrar = new JButton("Cerrar");

        pnlAcciones.add(btnNuevo);
        pnlAcciones.add(btnCerrar);
        add(pnlAcciones, BorderLayout.SOUTH);

        // --- Eventos ---
        btnCerrar.addActionListener(e -> dispose());
        btnNuevo.addActionListener(e -> registrarNuevoEjemplar());
    }

    /**
     * Carga los ejemplares asociados al documento actual en la tabla.
     */
    private void cargarEjemplares() {
        modelo.setRowCount(0);
        try {
            // ¡CORRECCIÓN! Llamamos al método específico pasando el ID del documento
            List<Object[]> datos = ejemplarService.obtenerEjemplaresPorDocumento(this.idDocumento);

            for (Object[] fila : datos) {
                // Ahora el mapeo es directo porque el DAO nos devuelve exactamente
                // [0] ID Ejemplar, [1] Código de Barras, [2] Estado
                modelo.addRow(new Object[]{fila[0], fila[1], fila[2]});
            }
        } catch (Exception e) {
            log.severe("Error al cargar ejemplares en el diálogo: " + e.getMessage());
        }
    }

    /**
     * Lógica para capturar datos y persistir un nuevo ejemplar en la base de datos.
     */
    private void registrarNuevoEjemplar() {
        String codigo = JOptionPane.showInputDialog(this,
                "Escanee o ingrese el Código de Barras único para este ejemplar:",
                "Registro de Ejemplar", JOptionPane.QUESTION_MESSAGE);

        if (codigo != null && !codigo.trim().isEmpty()) {
            try {
                // SOLUCIÓN AL ERROR DE CLASE ABSTRACTA:
                // Creamos una instancia anónima de Documento solo para transportar el ID
                Documento docBase = new Documento() {};
                docBase.setIdDocumento(this.idDocumento);

                // Construcción del objeto Ejemplar con Lombok @Builder
                Ejemplar nuevo = Ejemplar.builder()
                        .documento(docBase)
                        .codigoBarrasUnico(codigo.trim())
                        .estado("Disponible")
                        .build();

                // Persistencia mediante la capa Service
                if (ejemplarService.guardarEjemplar(nuevo)) {
                    JOptionPane.showMessageDialog(this, "Ejemplar registrado con éxito.");
                    cargarEjemplares(); // Refrescar la lista local
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo registrar. Verifique si el código de barras ya existe.",
                            "Error de Duplicidad", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                log.severe("Error crítico al registrar ejemplar: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error interno al procesar el registro.");
            }
        }
    }
}