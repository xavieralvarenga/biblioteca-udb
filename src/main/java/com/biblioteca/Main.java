package com.biblioteca;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.view.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
            System.err.println("Error al inicializar FlatLaf: " + ex.getMessage());
        }

        // 2. Iniciar la aplicación mostrando la ventana de Login
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
            /*System.out.println("Iniciando prueba de conexión...");

            // Intentamos obtener una conexión de nuestra clase DatabaseConnection
            try (Connection conn = DatabaseConnection.getConnection()) {

                if (conn != null && !conn.isClosed()) {
                    System.out.println("¡ÉXITO! Conexión establecida con MySQL.");

                    // Prueba de fuego: Ejecutar una consulta interna de MySQL
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {

                        if (rs.next()) {
                            System.out.println("Versión del servidor MySQL: " + rs.getString(1));
                        }
                    }
                }

            } catch (SQLException e) {
                System.err.println("ERROR: No se pudo conectar a la base de datos.");
                System.err.println("Causa: " + e.getMessage());
                // Aquí es donde el manejo de SQLExceptions mencionado en image_0443cb.png es vital
            }*/
    }
}