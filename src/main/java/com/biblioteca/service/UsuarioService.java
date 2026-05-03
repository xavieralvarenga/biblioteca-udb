package com.biblioteca.service;

import com.biblioteca.model.Usuario;
import com.biblioteca.repository.impl.UsuarioDAO;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Valida las credenciales de un usuario.
     * @return El objeto Usuario si el login es exitoso, o null si falla.
     * @throws Exception Si la cuenta está inactiva.
     */
    public List<Usuario> obtenerTodos() {
        return usuarioDAO.obtenerTodosLosUsuarios();
    }
    public Usuario login(String carnet, String passwordPlana) throws Exception {
        // 1. Buscamos al usuario en la BD
        Usuario usuario = usuarioDAO.buscarPorCarnet(carnet);

        // Si la base de datos devuelve null, el carnet no está registrado
        if (usuario == null) {
            throw new Exception("El usuario ingresado no existe.");
        }

        // 2. Verificamos si su estado es "Activo"
        if (!"Activo".equalsIgnoreCase(usuario.getEstado())) {
            throw new Exception("Usuario inactivo. Por favor, comunícate con tu administrador.");
        }

        // 3. COMPARACIÓN TEMPORAL EN TEXTO PLANO
        if (passwordPlana.equals(usuario.getPasswordHash())) {
            return usuario; // ¡Contraseña correcta!
        }

        // Si llegamos a esta línea, el usuario existe y está activo, pero la clave no coincide
        throw new Exception("Contraseña incorrecta. Inténtalo de nuevo.");
    }
    /**
     * Registra un nuevo usuario validando que el carnet no exista previamente.
     */
    public boolean registrarUsuario(Usuario nuevoUsuario) throws Exception {
        // 1. Validamos si el carnet ya está en uso
        Usuario usuarioExistente = usuarioDAO.buscarPorCarnet(nuevoUsuario.getCarnet());

        if (usuarioExistente != null) {
            throw new Exception("El carnet '" + nuevoUsuario.getCarnet() + "' ya se encuentra registrado.");
        }

        // 2. Aquí es donde en el futuro encriptaremos la contraseña con BCrypt.
        // Por ahora, respetando tu configuración de pruebas, la guardamos tal cual viene.

        // 3. Mandamos a guardar a la BD
        return usuarioDAO.registrarUsuario(nuevoUsuario);
    }
    /**
     * Restablece la contraseña del usuario asignándole su mismo carnet.
     */
    public boolean restablecerPassword(int idUsuario, String nuevaPassword) throws Exception {
        // En el futuro, aquí encriptaremos 'nuevaPassword' con BCrypt antes de enviarlo al DAO.
        // Por ahora, se guarda en texto plano según la configuración de pruebas.
        boolean exito = usuarioDAO.actualizarPassword(idUsuario, nuevaPassword);

        if (!exito) {
            throw new Exception("No se pudo actualizar la contraseña en la base de datos.");
        }
        return true;
    }

    /**
     * Alterna el estado del usuario entre "Activo" e "Inactivo".
     */
    public boolean cambiarEstadoUsuario(int idUsuario, String estadoActual) throws Exception {
        // Operador ternario: Si es Activo, el nuevo estado es Inactivo. Sino, es Activo.
        String nuevoEstado = estadoActual.equalsIgnoreCase("Activo") ? "Inactivo" : "Activo";

        boolean exito = usuarioDAO.cambiarEstado(idUsuario, nuevoEstado);
        if (!exito) {
            throw new Exception("No se pudo actualizar el estado del usuario.");
        }
        return true;
    }
    /**
     * Ejecuta una búsqueda avanzada de usuarios basándose en criterios de texto y rol.
     */
    public List<Usuario> buscarConFiltro(String textoBusqueda, Integer idRol, Boolean mora, String estado) {
        return usuarioDAO.buscarUsuariosConFiltro(textoBusqueda, idRol, mora, estado);
    }

}