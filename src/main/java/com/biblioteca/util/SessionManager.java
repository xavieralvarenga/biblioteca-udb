package com.biblioteca.util;

import com.biblioteca.model.Usuario;

public class SessionManager {
    // La única instancia global de esta clase (Singleton)
    private static SessionManager instancia;

    // Aquí guardamos al usuario que está logueado actualmente
    private Usuario usuarioLogueado;

    // Constructor privado para que nadie más pueda crear un "nuevo" SessionManager
    private SessionManager() {}

    // Método para obtener la bóveda (si no existe, la crea; si ya existe, te da la misma)
    public static SessionManager getInstance() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }

    // Guardar el usuario al iniciar sesión
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
    }

    // Obtener el usuario desde CUALQUIER PARTE del sistema
    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    // Destruir la sesión por completo al salir
    public void cerrarSesion() {
        this.usuarioLogueado = null;
    }
}