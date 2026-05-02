package com.biblioteca.service;

import com.biblioteca.model.Usuario;
import com.biblioteca.repository.impl.UsuarioDAO;

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
}