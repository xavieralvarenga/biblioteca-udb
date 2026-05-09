package com.biblioteca.repository.impl;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.model.*;
import com.biblioteca.repository.IDocumentoDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentoDAO implements IDocumentoDAO {
    private final Connection connection;

    public DocumentoDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public boolean insertar(Documento doc) {
        // SQL: id_documento NO se incluye porque es SERIAL/AUTO_INCREMENT
        String sql = "INSERT INTO Documento (id_tipo_doc, titulo, autor, ubicacion_fisica, codigo_de_barras, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            // Preparamos para recuperar el ID generado automáticamente
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // CORRECCIÓN: El primer parámetro es el TIPO (1, 2 o 3), no el ID del documento
            ps.setInt(1, doc.getTipoDocumento());
            ps.setString(2, doc.getTitulo());
            ps.setString(3, doc.getAutor());
            ps.setString(4, doc.getUbicacionFisica());
            ps.setString(5, doc.getCodigoBarrasObra());
            ps.setString(6, doc.getEstado());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    // Recuperamos el ID que la base de datos asignó
                    int generatedId = rs.getInt(1);
                    doc.setIdDocumento(generatedId);

                    // Insertamos en la tabla específica (Libro, Revista o CD)
                    insertarEnTablaHija(doc);

                    connection.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Error en DAO Insertar: " + e.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    private void insertarEnTablaHija(Documento doc) throws SQLException {
        if (doc instanceof Libro) {
            Libro l = (Libro) doc;
            String sql = "INSERT INTO Libro (id_documento, isbn, editorial, edicion) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, l.getIdDocumento()); // Aquí ya no es null
                ps.setString(2, l.getIsbn());
                ps.setString(3, l.getEditorial());
                ps.setString(4, l.getEdicion());
                ps.executeUpdate();
            }
        } else if (doc instanceof Revista) {
            Revista r = (Revista) doc;
            String sql = "INSERT INTO Revista (id_documento, issn, volumen, mes_publicacion) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, r.getIdDocumento());
                ps.setString(2, r.getIssn());
                ps.setString(3, r.getVolumen());
                ps.setString(4, r.getMesPublicacion());
                ps.executeUpdate();
            }
        } else if (doc instanceof Cd) {
            Cd c = (Cd) doc;
            String sql = "INSERT INTO CD (id_documento, duracion_minutos, tipo_contenido) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, c.getIdDocumento());
                ps.setInt(2, c.getDuracionMinutos());
                ps.setString(3, c.getTipoContenido());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public List<Documento> listarTodos() {
        List<Documento> lista = new ArrayList<>();
        String sql = "SELECT d.*, l.isbn, l.editorial, l.edicion, r.issn, r.volumen, r.mes_publicacion, c.duracion_minutos, c.tipo_contenido " +
                "FROM Documento d " +
                "LEFT JOIN Libro l ON d.id_documento = l.id_documento " +
                "LEFT JOIN Revista r ON d.id_documento = r.id_documento " +
                "LEFT JOIN CD c ON d.id_documento = c.id_documento";

        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int tipoDoc = rs.getInt("id_tipo_doc");
                Documento doc = null;

                if (tipoDoc == 1) {
                    doc = new Libro(rs.getInt("id_documento"), rs.getString("isbn"), rs.getString("editorial"), rs.getString("edicion"));
                } else if (tipoDoc == 2) {
                    doc = new Revista(rs.getInt("id_documento"), rs.getString("issn"), rs.getString("volumen"), rs.getString("mes_publicacion"));
                } else if (tipoDoc == 3) {
                    doc = new Cd(rs.getInt("id_documento"), rs.getInt("duracion_minutos"), rs.getString("tipo_contenido"));
                }

                if (doc != null) {
                    doc.setTitulo(rs.getString("titulo"));
                    doc.setAutor(rs.getString("autor"));
                    doc.setUbicacionFisica(rs.getString("ubicacion_fisica"));
                    doc.setCodigoBarrasObra(rs.getString("codigo_de_barras"));
                    doc.setEstado(rs.getString("estado"));
                    doc.setTipoDocumento(tipoDoc);
                    lista.add(doc);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * Actualiza la información de un documento en la base de datos.
     * Utiliza transacciones para modificar la tabla padre y la tabla hija.
     * * @param doc El objeto documento con los datos actualizados.
     * @return true si la operación fue exitosa, false de lo contrario.
     */
    @Override
    public boolean actualizar(Documento doc) {
        // SQL para la tabla principal
        String sqlPadre = "UPDATE Documento SET titulo = ?, autor = ?, ubicacion_fisica = ?, " +
                "codigo_de_barras = ?, estado = ? WHERE id_documento = ?";

        try {
            // Iniciamos transacción para asegurar consistencia
            connection.setAutoCommit(false);

            // 1. Actualizar tabla padre (Documento)
            try (PreparedStatement ps = connection.prepareStatement(sqlPadre)) {
                ps.setString(1, doc.getTitulo());
                ps.setString(2, doc.getAutor());
                ps.setString(3, doc.getUbicacionFisica());
                ps.setString(4, doc.getCodigoBarrasObra());
                ps.setString(5, doc.getEstado());
                ps.setInt(6, doc.getIdDocumento());

                ps.executeUpdate();
            }

            // 2. Actualizar tabla hija según la instancia del objeto
            actualizarTablaHija(doc);

            // Si ambas operaciones fueron exitosas, confirmamos los cambios
            connection.commit();
            return true;

        } catch (SQLException e) {
            // En caso de error, revertimos los cambios realizados en la transacción
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Error al actualizar documento: " + e.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * Método auxiliar privado para actualizar los campos específicos de cada tipo de documento.
     * @param doc Objeto documento a persistir.
     * @throws SQLException Si ocurre un error en la ejecución SQL.
     */
    private void actualizarTablaHija(Documento doc) throws SQLException {
        String sql = "";

        if (doc instanceof Libro) {
            Libro l = (Libro) doc;
            sql = "UPDATE Libro SET isbn = ?, editorial = ?, edicion = ? WHERE id_documento = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, l.getIsbn());
                ps.setString(2, l.getEditorial());
                ps.setString(3, l.getEdicion());
                ps.setInt(4, l.getIdDocumento());
                ps.executeUpdate();
            }
        } else if (doc instanceof Revista) {
            Revista r = (Revista) doc;
            sql = "UPDATE Revista SET issn = ?, volumen = ?, mes_publicacion = ? WHERE id_documento = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, r.getIssn());
                ps.setString(2, r.getVolumen());
                ps.setString(3, r.getMesPublicacion());
                ps.setInt(4, r.getIdDocumento());
                ps.executeUpdate();
            }
        } else if (doc instanceof Cd) {
            Cd c = (Cd) doc;
            sql = "UPDATE CD SET duracion_minutos = ?, tipo_contenido = ? WHERE id_documento = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, c.getDuracionMinutos());
                ps.setString(2, c.getTipoContenido());
                ps.setInt(3, c.getIdDocumento());
                ps.executeUpdate();
            }
        }
    }

    /**
     * Elimina un documento de la base de datos de forma permanente.
     * Realiza un borrado en cascada manual dentro de una transacción.
     * * @param id El identificador único del documento a eliminar.
     * @return true si el borrado fue exitoso en todas las tablas involucradas.
     */
    @Override
    public boolean eliminar(int id) {
        // Queries para limpiar tablas hijas y luego la tabla padre
        String sqlHijoLibro = "DELETE FROM Libro WHERE id_documento = ?";
        String sqlHijoRevista = "DELETE FROM Revista WHERE id_documento = ?";
        String sqlHijoCD = "DELETE FROM CD WHERE id_documento = ?";
        String sqlPadre = "DELETE FROM Documento WHERE id_documento = ?";

        try {
            connection.setAutoCommit(false);

            // Intentamos borrar de todas las posibles tablas hijas (solo una tendrá éxito)
            try (PreparedStatement psL = connection.prepareStatement(sqlHijoLibro);
                 PreparedStatement psR = connection.prepareStatement(sqlHijoRevista);
                 PreparedStatement psC = connection.prepareStatement(sqlHijoCD)) {

                psL.setInt(1, id);
                psL.executeUpdate();

                psR.setInt(1, id);
                psR.executeUpdate();

                psC.setInt(1, id);
                psC.executeUpdate();
            }

            // Finalmente borramos el registro padre
            try (PreparedStatement psP = connection.prepareStatement(sqlPadre)) {
                psP.setInt(1, id);
                int filasAfectadas = psP.executeUpdate();

                if (filasAfectadas > 0) {
                    connection.commit();
                    return true;
                }
            }

            connection.rollback();
            return false;

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Error al eliminar documento: " + e.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}