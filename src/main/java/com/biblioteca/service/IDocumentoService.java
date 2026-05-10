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

    /**
     * Actualiza la información de un documento existente.
     * @param doc Objeto documento con los cambios realizados por el usuario.
     * @return true si la validación y la persistencia en base de datos fueron exitosas.
     */
    boolean actualizarDocumento(Documento doc);

    /**
     * Gestiona la eliminación de un documento del sistema.
     * @param id El identificador único del documento que se desea eliminar.
     * @return true si el documento pudo ser eliminado (siempre que no tenga restricciones como préstamos activos).
     */
    boolean darDeBajaDocumento(int id);
}
