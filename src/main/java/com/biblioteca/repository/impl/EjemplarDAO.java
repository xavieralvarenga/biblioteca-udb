package com.biblioteca.repository.impl;

import com.biblioteca.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class EjemplarDAO {

    /**
     * Busca ejemplares en la base de datos con filtros de texto y tipo de documento.
     */
    public List<Object[]> buscarEjemplares(String texto, Integer idTipoDoc) {
        List<Object[]> resultados = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT e.id_ejemplar, e.codigo_de_barras, d.titulo, d.autor, td.Nombre as tipo, e.estado " +
                        "FROM Ejemplar e " +
                        "INNER JOIN Documento d ON e.id_documento = d.id_documento " +
                        "INNER JOIN TipoDocumento td ON d.id_tipo_doc = td.id_tipo_doc " +
                        "WHERE 1=1"
        );

        if (idTipoDoc != null && idTipoDoc > 0) {
            sql.append(" AND td.id_tipo_doc = ?");
        }
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (d.titulo LIKE ? OR d.autor LIKE ? OR e.codigo_de_barras LIKE ?)");
        }

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            if (idTipoDoc != null && idTipoDoc > 0) ps.setInt(idx++, idTipoDoc);
            if (texto != null && !texto.trim().isEmpty()) {
                String search = "%" + texto + "%";
                ps.setString(idx++, search);
                ps.setString(idx++, search);
                ps.setString(idx++, search);
            }

            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultados;
    }
}