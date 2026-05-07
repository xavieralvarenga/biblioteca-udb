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
}