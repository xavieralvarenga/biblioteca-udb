package com.biblioteca.repository.impl;

import com.biblioteca.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    /**
     * Obtiene todos los préstamos uniendo la información del Usuario, el Ejemplar y el Documento.
     */
    public List<Object[]> obtenerTodosLosPrestamos() {
        List<Object[]> lista = new ArrayList<>();

        // Consulta SQL con INNER JOIN basada estrictamente en tu diagrama ER
        String sql = "SELECT p.id_prestamo, u.carnet_docente_alumno, u.Nombres, u.Apellidos, " +
                "e.codigo_de_barras, d.titulo, p.fecha_prestamo, p.fecha_limite, p.estado_prestamo, p.estado_pago " +
                "FROM Prestamo p " +
                "INNER JOIN Usuarios u ON p.id_usuario = u.ID_Usuario " +
                "INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar " +
                "INNER JOIN Documento d ON e.id_documento = d.id_documento " +
                "ORDER BY p.id_prestamo DESC"; // Los más recientes primero

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Combinamos Nombres y Apellidos para la columna "Lector"
                String nombreCompleto = rs.getString("Nombres") + " " + rs.getString("Apellidos");

                lista.add(new Object[]{
                        rs.getInt("id_prestamo"),
                        rs.getString("carnet_docente_alumno"),
                        nombreCompleto,
                        rs.getString("codigo_de_barras"),
                        rs.getString("titulo"),
                        rs.getDate("fecha_prestamo"),
                        rs.getDate("fecha_limite"),
                        rs.getString("estado_prestamo"),
                        rs.getString("estado_pago")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los préstamos: " + e.getMessage());
        }
        return lista;
    }
    /**
     * Registra un nuevo préstamo y actualiza el estado del ejemplar en una sola transacción.
     */
    public boolean registrarNuevoPrestamo(int idUsuario, int idEjemplar, java.time.LocalDate fechaPrestamo, java.time.LocalDate fechaLimite) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false); // 🔴 Iniciar Transacción Segura

            // 1. Insertar el préstamo en la tabla Prestamo
            String sqlInsert = "INSERT INTO Prestamo (id_usuario, id_ejemplar, fecha_prestamo, fecha_limite, estado_prestamo) VALUES (?, ?, ?, ?, 'Activo')";
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                psInsert.setInt(1, idUsuario);
                psInsert.setInt(2, idEjemplar);
                psInsert.setDate(3, java.sql.Date.valueOf(fechaPrestamo));
                psInsert.setDate(4, java.sql.Date.valueOf(fechaLimite));
                psInsert.executeUpdate();
            }

            // 2. Actualizar el estado del ejemplar físico a 'Prestado'
            String sqlUpdate = "UPDATE Ejemplar SET estado = 'Prestado' WHERE id_ejemplar = ?";
            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                psUpdate.setInt(1, idEjemplar);
                psUpdate.executeUpdate();
            }

            con.commit(); // 🟢 Confirmar Transacción (Ambas operaciones fueron exitosas)
            return true;

        } catch (SQLException e) {
            if (con != null) {
                con.rollback(); // ↩️ Si algo falla, revertimos todos los cambios para no corromper datos
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }
    /**
     * Obtiene datos extra necesarios para procesar la devolución (ID Ejemplar y Valor de Mora).
     */
    /**
     * Obtiene datos extra necesarios para procesar la devolución.
     * Ahora lanza excepciones para que la interfaz pueda mostrar el error exacto.
     */
    public Object[] obtenerDatosParaDevolucion(int idPrestamo) throws SQLException {
        String sql = "SELECT p.id_ejemplar, p.fecha_limite, td.valor_mora, p.id_usuario " +
                "FROM Prestamo p " +
                "INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar " +
                "INNER JOIN Documento d ON e.id_documento = d.id_documento " +
                "INNER JOIN TipoDocumento td ON d.id_tipo_doc = td.id_tipo_doc " +
                "WHERE p.id_prestamo = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("id_ejemplar"),
                            rs.getDate("fecha_limite").toLocalDate(),
                            rs.getDouble("valor_mora"),
                            rs.getInt("id_usuario") // Aseguramos que viaja el id_usuario
                    };
                }
            }
        }

        // Si el código llega hasta aquí, significa que la consulta fue exitosa pero no encontró el préstamo.
        throw new SQLException("El ID de préstamo " + idPrestamo + " no devolvió resultados al unir las tablas. Verifica las llaves foráneas.");
    }

    /**
     * Registra la devolución y libera el ejemplar en una sola transacción.
     */
    public boolean registrarDevolucion(int idPrestamo, int idEjemplar, int diasRetraso, double montoCalculado, String observaciones) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false); // 🔴 Iniciamos Transacción

            // 1. Actualizar el Préstamo
            String sqlPrestamo = "UPDATE Prestamo SET estado_prestamo = 'Devuelto', dias_retraso = ?, monto_calculado = ? WHERE id_prestamo = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlPrestamo)) {
                ps.setInt(1, diasRetraso);
                ps.setDouble(2, montoCalculado);
                ps.setInt(3, idPrestamo);
                ps.executeUpdate();
            }

            // 2. Insertar el registro físico en la tabla Devolucion
            String sqlDevolucion = "INSERT INTO Devolucion (id_prestamo, fecha_devolucion, observaciones_estado_fisico) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlDevolucion)) {
                ps.setInt(1, idPrestamo);
                ps.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
                ps.setString(3, observaciones);
                ps.executeUpdate();
            }

            // 3. Liberar el Ejemplar
            String sqlEjemplar = "UPDATE Ejemplar SET estado = 'Disponible' WHERE id_ejemplar = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlEjemplar)) {
                ps.setInt(1, idEjemplar);
                ps.executeUpdate();
            }

            con.commit(); // 🟢 Confirmar cambios
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback(); // ↩️ Revertir si hay error
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }
    /**
     * Registra la devolución, el pago de mora y actualiza el estado global del usuario.
     */
    /**
     * Registra la devolución, el pago de mora (total o abono) y actualiza el estado global del usuario.
     */
    public boolean registrarDevolucionConPago(int idPrestamo, int idEjemplar, int idUsuario,
                                              int diasRetraso, double montoCalculado,
                                              double montoPagado, String observaciones) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false); // Iniciamos Transacción

            // 1. Determinar el estado del pago
            String estadoPago = (montoCalculado <= 0) ? "N/A" : (montoPagado >= montoCalculado) ? "Pagado" : "Pendiente";

            // 2. Actualizar el Préstamo (¡AQUÍ ESTÁ EL CAMBIO! Agregamos monto_pagado = ?)
            String sqlPrestamo = "UPDATE Prestamo SET estado_prestamo = 'Devuelto', dias_retraso = ?, " +
                    "monto_calculado = ?, monto_pagado = ?, estado_pago = ? WHERE id_prestamo = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlPrestamo)) {
                ps.setInt(1, diasRetraso);
                ps.setDouble(2, montoCalculado);
                ps.setDouble(3, montoPagado); // <-- Guardamos el abono que hizo en la ventana
                ps.setString(4, estadoPago);
                ps.setInt(5, idPrestamo);
                ps.executeUpdate();
            }

            // 3. Liberar el Ejemplar
            String sqlEjemplar = "UPDATE Ejemplar SET estado = 'Disponible' WHERE id_ejemplar = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlEjemplar)) {
                ps.setInt(1, idEjemplar);
                ps.executeUpdate();
            }

            // 4. Insertar en tabla Devolucion
            String sqlDevolucion = "INSERT INTO Devolucion (id_prestamo, fecha_devolucion, observaciones_estado_fisico) VALUES (?, CURDATE(), ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlDevolucion)) {
                ps.setInt(1, idPrestamo);
                ps.setString(2, observaciones);
                ps.executeUpdate();
            }

            // 5. Actualizar estado_mora del Usuario
            String sqlCheckMora = "SELECT COUNT(*) FROM Prestamo WHERE id_usuario = ? AND estado_pago = 'Pendiente'";
            boolean sigueConMora = false;
            try (PreparedStatement ps = con.prepareStatement(sqlCheckMora)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) sigueConMora = rs.getInt(1) > 0;
                }
            }

            String sqlUpdateUsuario = "UPDATE Usuarios SET estado_mora = ? WHERE ID_Usuario = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateUsuario)) {
                ps.setBoolean(1, sigueConMora);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }
    /**
     * Busca préstamos aplicando filtros de texto y estado.
     */
    public List<Object[]> buscarPrestamosConFiltro(String texto, String estado, String estadoPago) {
        List<Object[]> lista = new ArrayList<>();

        // Añadimos p.estado_pago al SELECT
        StringBuilder sql = new StringBuilder(
                "SELECT p.id_prestamo, u.carnet_docente_alumno, u.Nombres, u.Apellidos, " +
                        "e.codigo_de_barras, d.titulo, p.fecha_prestamo, p.fecha_limite, p.estado_prestamo, p.estado_pago " +
                        "FROM Prestamo p " +
                        "INNER JOIN Usuarios u ON p.id_usuario = u.ID_Usuario " +
                        "INNER JOIN Ejemplar e ON p.id_ejemplar = e.id_ejemplar " +
                        "INNER JOIN Documento d ON e.id_documento = d.id_documento " +
                        "WHERE 1=1"
        );

        // Filtro de Estado Físico
        if (estado != null && !estado.equals("Todos")) {
            if (estado.equals("Activos")) {
                sql.append(" AND p.estado_prestamo = 'Activo' AND p.fecha_limite >= CURDATE()");
            } else if (estado.equals("Vencidos")) {
                sql.append(" AND p.estado_prestamo = 'Activo' AND p.fecha_limite < CURDATE()");
            } else if (estado.equals("Devueltos")) {
                sql.append(" AND p.estado_prestamo = 'Devuelto'");
            }
        }

        // NUEVO: Filtro de Estado de Pago
        if (estadoPago != null && !estadoPago.equals("Todos")) {
            sql.append(" AND p.estado_pago = '").append(estadoPago).append("'");
        }

        // Filtro de Texto
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (u.carnet_docente_alumno LIKE ? OR e.codigo_de_barras LIKE ? OR d.titulo LIKE ?)");
        }

        sql.append(" ORDER BY p.id_prestamo DESC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (texto != null && !texto.trim().isEmpty()) {
                String search = "%" + texto.trim() + "%";
                ps.setString(1, search);
                ps.setString(2, search);
                ps.setString(3, search);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombreCompleto = rs.getString("Nombres") + " " + rs.getString("Apellidos");
                    String estadoReal = rs.getString("estado_prestamo");
                    java.sql.Date fechaLimite = rs.getDate("fecha_limite");

                    if ("Activo".equals(estadoReal) && fechaLimite != null && fechaLimite.toLocalDate().isBefore(java.time.LocalDate.now())) {
                        estadoReal = "Vencido";
                    }

                    lista.add(new Object[]{
                            rs.getInt("id_prestamo"),
                            rs.getString("carnet_docente_alumno"),
                            nombreCompleto,
                            rs.getString("codigo_de_barras"),
                            rs.getString("titulo"),
                            rs.getDate("fecha_prestamo"),
                            fechaLimite,
                            estadoReal,
                            rs.getString("estado_pago") // Añadimos el pago aquí también
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar los préstamos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registra un pago a una deuda pendiente y actualiza la solvencia del usuario.
     */
    public boolean abonarMora(int idPrestamo, int idUsuario, double montoAbono) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Actualizar el monto pagado en el préstamo
            String sqlPago = "UPDATE Prestamo SET monto_pagado = monto_pagado + ?, " +
                    "estado_pago = IF(monto_pagado + ? >= monto_calculado, 'Pagado', 'Pendiente') " +
                    "WHERE id_prestamo = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlPago)) {
                ps.setDouble(1, montoAbono);
                ps.setDouble(2, montoAbono);
                ps.setInt(3, idPrestamo);
                ps.executeUpdate();
            }

            // 2. REGLA DE NEGOCIO: Recalcular si el usuario ya no debe NADA en ningún otro préstamo
            String sqlCheck = "SELECT COUNT(*) FROM Prestamo WHERE id_usuario = ? AND estado_pago = 'Pendiente'";
            boolean sigueDeudor = false;
            try (PreparedStatement ps = con.prepareStatement(sqlCheck)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) sigueDeudor = rs.getInt(1) > 0;
                }
            }

            // 3. Actualizar el estado global del usuario
            String sqlUser = "UPDATE Usuarios SET estado_mora = ? WHERE ID_Usuario = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUser)) {
                ps.setBoolean(1, sigueDeudor);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.close();
        }
    }
}