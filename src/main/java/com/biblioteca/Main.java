package com.biblioteca;

import com.biblioteca.view.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Clase principal de entrada para la aplicación de la Biblioteca.
 * <p>
 * Se encarga de configurar el aspecto visual (Look and Feel) de la interfaz
 * y de lanzar la ventana inicial de inicio de sesión.
 * </p>
 *
 * @author TuNombre
 * @version 1.0
 */
public class Main {

    /**
     * Punto de entrada principal del sistema.
     * <p>
     * Inicializa el tema FlatLaf para mejorar la estética de Swing,
     * configura propiedades de redondeo en componentes y lanza el frame
     * de Login en el hilo de despacho de eventos de Swing (EDT).
     * </p>
     *
     * @param args Argumentos de la línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        try {
            // Configuración del Look and Feel moderno
            FlatLightLaf.setup();

            // Personalización de la curvatura de los bordes (UI Properties)
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
            System.err.println("Error al inicializar FlatLaf: " + ex.getMessage());
        }

        // Ejecución de la interfaz en el hilo de despacho de eventos
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}