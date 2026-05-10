package com.biblioteca.repository.impl;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.model.Ejemplar;
import lombok.Cleanup;
import lombok.extern.java.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de acceso a datos para la gestión de Ejemplares físicos.
 * Utiliza JDBC tradicional para consultas dinámicas y filtrado.
 */
@Log
public class EjemplarDAO {

    /**
     * Realiza una búsqueda avanzada de ejemplares en la base de datos.
     * Une las tablas Ejemplar, Documento y TipoDocumento para obtener una vista completa.
     *
     * @param texto      Cadena de búsqueda (Título, Autor o Código de Barras).
     * @param idTipoDoc  Identificador del tipo de documento (0 o null para omitir filtro).
     * @return Lista de arreglos de objetos, cada uno representando una fila para la JTable.
     */
    public List<Object[]> buscarEjemplares(String texto, Integer idTipoDoc) {
        List<Object[]> resultados = new ArrayList<>();

        // 1. Construcción de la consulta con StringBuilder para mayor eficiencia
        StringBuilder sql = new StringBuilder(
                "SELECT e.id_ejemplar, e.codigo_de_barras, d.titulo, d.autor, td.Nombre as tipo, e.estado " +
                        "FROM Ejemplar e " +
                        "INNER JOIN Documento d ON e.id_documento = d.id_documento " +
                        "INNER JOIN TipoDocumento td ON d.id_tipo_doc = td.id_tipo_doc " +
                        "WHERE 1=1"
        );

        // 2. Aplicación de filtros dinámicos
        if (idTipoDoc != null && idTipoDoc > 0) {
            sql.append(" AND td.id_tipo_doc = ?");
        }
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (d.titulo LIKE ? OR d.autor LIKE ? OR e.codigo_de_barras LIKE ?)");
        }

        try {
            // 3. Obtención de conexión y preparación de sentencia con Lombok @Cleanup
            @Cleanup Connection con = DatabaseConnection.getConnection();
            @Cleanup PreparedStatement ps = con.prepareStatement(sql.toString());

            // 4. Asignación de parámetros según los filtros activos
            int idx = 1;
            if (idTipoDoc != null && idTipoDoc > 0) {
                ps.setInt(idx++, idTipoDoc);
            }
            if (texto != null && !texto.trim().isEmpty()) {
                String search = "%" + texto.trim() + "%";
                ps.setString(idx++, search); // para título
                ps.setString(idx++, search); // para autor
                ps.setString(idx++, search); // para código de barras
            }

            // 5. Ejecución y mapeo de resultados
            @Cleanup ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resultados.add(new Object[]{
                        rs.getInt("id_ejemplar"),
                        rs.getString("codigo_de_barras"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("tipo"),
                        rs.getString("estado")
                });
            }

        } catch (SQLException e) {
            log.severe("Error al ejecutar búsqueda de ejemplares: " + e.getMessage());
        }

        return resultados;
    }

    /**
     * Obtiene los ejemplares que pertenecen estrictamente a un documento específico.
     */
    public List<Object[]> obtenerEjemplaresPorDocumento(Integer idDocumento) {
        List<Object[]> resultados = new ArrayList<>();
        String sql = "SELECT id_ejemplar, codigo_de_barras, estado FROM Ejemplar WHERE id_documento = ?";

        try {
            @Cleanup Connection con = DatabaseConnection.getConnection();
            @Cleanup PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, idDocumento);

            @Cleanup ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Devolvemos exactamente las 3 columnas que necesita la tabla del Diálogo
                resultados.add(new Object[]{
                        rs.getInt("id_ejemplar"),
                        rs.getString("codigo_de_barras"),
                        rs.getString("estado")
                });
            }
        } catch (SQLException e) {
            log.severe("Error al obtener ejemplares del documento " + idDocumento + ": " + e.getMessage());
        }

        return resultados;
    }

    /**
     * Inserta un nuevo ejemplar físico en la base de datos.
     * @param ej Objeto con los datos del ejemplar.
     * @param idDoc ID del documento padre al que se vincula.
     * @return true si la inserción fue exitosa.
     */
    public boolean insertar(Ejemplar ej, Integer idDoc) {
        String sql = "INSERT INTO Ejemplar (id_documento, codigo_de_barras, estado) VALUES (?, ?, ?)";

        try {
            @Cleanup Connection con = DatabaseConnection.getConnection();
            @Cleanup PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, idDoc);
            ps.setString(2, ej.getCodigoBarrasUnico());
            ps.setString(3, ej.getEstado());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al insertar ejemplar: " + e.getMessage());
            return false;
        }
    }
}