package com.biblioteca.service;

import com.biblioteca.model.Documento;
import java.util.List;

public interface IDocumentoService {
    /**
     * Registra un nuevo documento validando reglas de negocio.
     */
    boolean registrarDocumento(Documento doc);

    /**
     * Retorna la lista completa de documentos (Libros, Revistas, CDs).
     */
    List<Documento> listarInventario();

    // Aquí irán más adelante: actualizarDocumento y eliminarDocumento
}
