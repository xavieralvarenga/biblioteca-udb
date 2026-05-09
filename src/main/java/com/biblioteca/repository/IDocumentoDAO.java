package com.biblioteca.repository;

import com.biblioteca.model.Documento;
import java.util.List;

/**
 * Interfaz que define las operaciones CRUD para documentos.
 */
public interface IDocumentoDAO {

    // Usamos Documento como tipo genérico para aprovechar el polimorfismo
    boolean insertar(Documento doc);
    List<Documento> listarTodos();
    /**
     * Actualiza los datos de un documento en las tablas correspondientes.
     * @param doc Objeto con los datos a persistir.
     * @return true si la transacción en la BD fue exitosa.
     */
    boolean actualizar(Documento doc);
    boolean eliminar(int id);
}