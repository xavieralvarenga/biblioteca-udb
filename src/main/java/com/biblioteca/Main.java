package com.biblioteca;

import com.biblioteca.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
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
<<<<<<< HEAD

}
=======
>>>>>>> 8e2e68b (feat(db): agregar Hikari pool y db.properties)
