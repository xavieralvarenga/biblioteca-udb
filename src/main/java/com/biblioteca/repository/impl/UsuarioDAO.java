package com.biblioteca.repository.impl;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.model.TipoUsuario;
import com.biblioteca.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    // Método para buscar un usuario por su carnet uniendo la tabla TipoUsuario
    // Método para buscar un usuario por su carnet uniendo la tabla TipoUsuario
    public Usuario buscarPorCarnet(String carnet) {
        Usuario usuario = null;
        // Consulta SQL CORREGIDA: Cambiamos t.idTipo por t.id_tipo
        String sql = "SELECT u.ID_Usuario, u.Nombres, u.Apellidos, u.carnet_docente_alumno, " +
                "u.password_hash, u.estado_mora, u.Estado, " +
                "t.id_tipo, t.nombre_rol, t.max_libros_permitidos, t.max_dias_prestamo " +
                "FROM Usuarios u " +
                "INNER JOIN TipoUsuario t ON u.id_tipo = t.id_tipo " +
                "WHERE u.carnet_docente_alumno = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carnet);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. Armamos el objeto TipoUsuario
                    TipoUsuario tipo = new TipoUsuario();
                    tipo.setIdTipo(rs.getInt("id_tipo")); // Corregido aquí también
                    tipo.setNombreRol(rs.getString("nombre_rol"));
                    tipo.setMaxLibrosPermitidos(rs.getInt("max_libros_permitidos"));
                    tipo.setMaxDiasPrestamo(rs.getInt("max_dias_prestamo"));

                    // 2. Armamos el objeto Usuario
                    usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("ID_Usuario"));
                    usuario.setNombres(rs.getString("Nombres"));
                    usuario.setApellidos(rs.getString("Apellidos"));
                    usuario.setCarnet(rs.getString("carnet_docente_alumno"));
                    usuario.setPasswordHash(rs.getString("password_hash"));
                    usuario.setEstadoMora(rs.getBoolean("estado_mora"));
                    usuario.setEstado(rs.getString("Estado"));
                    usuario.setTipoUsuario(tipo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por carnet: " + e.getMessage());
        }
        return usuario;
    }
}