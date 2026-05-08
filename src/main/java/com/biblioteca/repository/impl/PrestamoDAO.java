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
                "e.codigo_de_barras, d.titulo, p.fecha_prestamo, p.fecha_limite, p.estado_prestamo " +
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
                        rs.getString("estado_prestamo")
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
}