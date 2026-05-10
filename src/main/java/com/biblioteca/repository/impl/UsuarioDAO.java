package com.biblioteca.repository.impl;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.model.TipoUsuario;
import com.biblioteca.model.Usuario;
import java.util.List;
import java.util.ArrayList;
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
    // Método para insertar un nuevo usuario en la Base de Datos
    public boolean registrarUsuario(Usuario usuario) {
        String sql = "INSERT INTO Usuarios (id_tipo, Nombres, Apellidos, carnet_docente_alumno, password_hash, estado_mora, Estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuario.getTipoUsuario().getIdTipo());
            ps.setString(2, usuario.getNombres());
            ps.setString(3, usuario.getApellidos());
            ps.setString(4, usuario.getCarnet());
            ps.setString(5, usuario.getPasswordHash()); // Recuerda: por ahora irá en texto plano
            ps.setBoolean(6, usuario.getEstadoMora());
            ps.setString(7, usuario.getEstado());

            // executeUpdate devuelve la cantidad de filas afectadas (debería ser 1 si tuvo éxito)
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar usuario en la BD: " + e.getMessage());
            return false;
        }
    }

    // Método para obtener TODOS los usuarios de la base de datos
    public List<Usuario> obtenerTodosLosUsuarios() {
        List<Usuario> listaUsuarios = new ArrayList<>();

        String sql = "SELECT u.ID_Usuario, u.Nombres, u.Apellidos, u.carnet_docente_alumno, " +
                "u.password_hash, u.estado_mora, u.Estado, " +
                "t.id_tipo, t.nombre_rol, t.max_libros_permitidos, t.max_dias_prestamo " +
                "FROM Usuarios u " +
                "INNER JOIN TipoUsuario t ON u.id_tipo = t.id_tipo " +
                "ORDER BY u.ID_Usuario ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // Al no tener parámetros (?), podemos ejecutarlo directo

            while (rs.next()) {

                TipoUsuario tipo = new TipoUsuario();
                tipo.setIdTipo(rs.getInt("id_tipo"));
                tipo.setNombreRol(rs.getString("nombre_rol"));
                tipo.setMaxLibrosPermitidos(rs.getInt("max_libros_permitidos"));
                tipo.setMaxDiasPrestamo(rs.getInt("max_dias_prestamo"));

                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("ID_Usuario"));
                usuario.setNombres(rs.getString("Nombres"));
                usuario.setApellidos(rs.getString("Apellidos"));
                usuario.setCarnet(rs.getString("carnet_docente_alumno"));
                usuario.setPasswordHash(rs.getString("password_hash"));
                usuario.setEstadoMora(rs.getBoolean("estado_mora"));
                usuario.setEstado(rs.getString("Estado"));
                usuario.setTipoUsuario(tipo);

                listaUsuarios.add(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return listaUsuarios;
    }
    // Método para restablecer la contraseña
    public boolean actualizarPassword(int idUsuario, String nuevaPassword) {
        String sql = "UPDATE Usuarios SET password_hash = ? WHERE ID_Usuario = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevaPassword);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
            return false;
        }
    }

    // Método para activar/desactivar un usuario
    public boolean cambiarEstado(int idUsuario, String nuevoEstado) {
        String sql = "UPDATE Usuarios SET Estado = ? WHERE ID_Usuario = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarUsuario(Usuario usuario) throws Exception {
        String sql = """
            UPDATE Usuarios SET
                nombres = ?,
                apellidos = ?,
                carnet_docente_alumno = ?,
                id_tipo = ?,
                estado = ?
            WHERE ID_Usuario = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombres());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCarnet());

            ps.setInt(4, usuario.getTipoUsuario().getIdTipo());

            ps.setString(5, usuario.getEstado());

            ps.setInt(6, usuario.getIdUsuario());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al editar usuario: " + e.getMessage());
            return false;
        }
    }

    public Usuario obtenerPorId(int idUsuario) throws Exception {
        String sql = """
            SELECT *
            FROM Usuarios u
            INNER JOIN TipoUsuario t
            ON u.id_tipo = t.id_tipo
            WHERE u.ID_Usuario = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            // CORRECCIÓN: Envolver el ResultSet en un try-with-resources anidado
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();

                    u.setIdUsuario(rs.getInt("ID_Usuario"));
                    u.setNombres(rs.getString("nombres"));
                    u.setApellidos(rs.getString("apellidos"));
                    u.setCarnet(rs.getString("carnet_docente_alumno"));
                    u.setEstado(rs.getString("estado"));

                    TipoUsuario tipo = new TipoUsuario();
                    tipo.setIdTipo(rs.getInt("id_tipo"));
                    tipo.setNombreRol(rs.getString("nombre_rol"));

                    u.setTipoUsuario(tipo);

                    return u;
                }
            }
            // Si el ResultSet termina y no encontró nada, lanza la excepción
            throw new Exception("Usuario no encontrado.");
        }
    }

    /**
     * Busca usuarios aplicando filtros avanzados.
     */
    public List<Usuario> buscarUsuariosConFiltro(String textoBusqueda, Integer idRol, Boolean mora, String estado) {
        List<Usuario> listaFiltrada = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT u.ID_Usuario, u.Nombres, u.Apellidos, u.carnet_docente_alumno, " +
                        "u.password_hash, u.estado_mora, u.Estado, " +
                        "t.id_tipo, t.nombre_rol, t.max_libros_permitidos, t.max_dias_prestamo " +
                        "FROM Usuarios u " +
                        "INNER JOIN TipoUsuario t ON u.id_tipo = t.id_tipo WHERE 1=1"
        );

        // 1. Filtro de Rol
        if (idRol != null && idRol > 0) {
            sql.append(" AND u.id_tipo = ?");
        }

        // 2. Filtro de Texto (Buscar)
        if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
            sql.append(" AND (u.Nombres LIKE ? OR u.Apellidos LIKE ? OR u.carnet_docente_alumno LIKE ?)");
        }

        // 3. Filtro de Mora
        if (mora != null) {
            sql.append(" AND u.estado_mora = ?");
        }

        // 4. Filtro de Estado (Activo/Inactivo)
        if (estado != null && !estado.equals("Todos")) {
            sql.append(" AND u.Estado = ?");
        }

        sql.append(" ORDER BY u.ID_Usuario ASC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;

            // Asignación dinámica de parámetros (El orden importa)
            if (idRol != null && idRol > 0) {
                ps.setInt(index++, idRol);
            }

            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                String keyword = "%" + textoBusqueda.trim() + "%";
                ps.setString(index++, keyword);
                ps.setString(index++, keyword);
                ps.setString(index++, keyword);
            }

            if (mora != null) {
                ps.setBoolean(index++, mora);
            }

            if (estado != null && !estado.equals("Todos")) {
                ps.setString(index++, estado);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Armamos la respuesta (igual que antes)
                    TipoUsuario tipo = new TipoUsuario();
                    tipo.setIdTipo(rs.getInt("id_tipo"));
                    tipo.setNombreRol(rs.getString("nombre_rol"));
                    tipo.setMaxLibrosPermitidos(rs.getInt("max_libros_permitidos"));
                    tipo.setMaxDiasPrestamo(rs.getInt("max_dias_prestamo"));

                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("ID_Usuario"));
                    usuario.setNombres(rs.getString("Nombres"));
                    usuario.setApellidos(rs.getString("Apellidos"));
                    usuario.setCarnet(rs.getString("carnet_docente_alumno"));
                    usuario.setPasswordHash(rs.getString("password_hash"));
                    usuario.setEstadoMora(rs.getBoolean("estado_mora"));
                    usuario.setEstado(rs.getString("Estado"));
                    usuario.setTipoUsuario(tipo);

                    listaFiltrada.add(usuario);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en búsqueda filtrada de usuarios: " + e.getMessage());
        }
        return listaFiltrada;
    }
}