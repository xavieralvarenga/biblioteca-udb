package com.biblioteca.service.impl;

import com.biblioteca.model.Documento;
import com.biblioteca.repository.IDocumentoDAO;
import com.biblioteca.repository.impl.DocumentoDAO;
import com.biblioteca.service.IDocumentoService;
import java.sql.SQLException;
import java.util.List;

public class DocumentoServiceImpl implements IDocumentoService {
    private final IDocumentoDAO documentoDAO;

    public DocumentoServiceImpl() throws SQLException {
        this.documentoDAO = new DocumentoDAO();
    }

    @Override
    public boolean registrarDocumento(Documento doc) {
        if (doc.getTitulo() == null || doc.getTitulo().trim().isEmpty()) return false;
        if (doc.getAutor() == null || doc.getAutor().trim().isEmpty()) return false;
        return documentoDAO.insertar(doc);
    }

    @Override
    public List<Documento> listarInventario() {
        return documentoDAO.listarTodos();
    }

    /**
     * Ejecuta la lógica de negocio para actualizar un documento.
     * Valida que los campos obligatorios no estén vacíos antes de proceder.
     * * @param doc El documento con los cambios aplicados en la vista.
     * @return true si se validó y actualizó correctamente.
     */
    @Override
    public boolean actualizarDocumento(Documento doc) {
        // Regla de Negocio: No permitir campos críticos vacíos
        if (doc.getTitulo() == null || doc.getTitulo().trim().isEmpty()) {
            System.err.println("Validación: El título no puede estar vacío.");
            return false;
        }

        if (doc.getAutor() == null || doc.getAutor().trim().isEmpty()) {
            System.err.println("Validación: El autor es obligatorio.");
            return false;
        }

        // Regla de Negocio: Validar que el ID sea válido (mayor a 0)
        if (doc.getIdDocumento() == null || doc.getIdDocumento() <= 0) {
            System.err.println("Validación: ID de documento no válido para actualización.");
            return false;
        }

        // Si pasa las validaciones, delegamos la persistencia al DAO
        return documentoDAO.actualizar(doc);
    }
}