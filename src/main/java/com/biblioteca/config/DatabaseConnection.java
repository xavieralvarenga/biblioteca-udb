package com.biblioteca.config;

import com.zaxxer.hikari.HikariConfig; // Configuración para el pool de Hikari.
import com.zaxxer.hikari.HikariDataSource; // La fuente de datos que gestiona el pool.
import java.io.IOException; // Manejo de errores de entrada/salida.
import java.io.InputStream; // Para leer el archivo de propiedades.
import java.sql.Connection; // Interfaz estándar de conexión JDBC.
import java.sql.SQLException; // Manejo de errores de SQL.
import java.util.Properties; // Clase para manejar archivos .properties.

/**
 * Clase encargada de gestionar la conexión a la base de datos
 * utilizando el pool de conexiones HikariCP.
 */
public class DatabaseConnection {

    // Instancia única del pool de conexiones (Patrón Singleton implícito)
    private static HikariDataSource dataSource;

    // El bloque static asegura que la configuración se cargue al iniciar la clase
    static {
        // InputStream para leer el archivo de configuración externa
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            Properties props = new Properties();

            // Verificación de existencia del archivo properties
            if (input == null) {
                throw new RuntimeException("Error: No se encontró el archivo 'db.properties' en el classpath");
            }

            // Cargar los pares clave-valor desde el archivo
            props.load(input);

            // Objeto de configuración principal de Hikari
            HikariConfig config = new HikariConfig();

            // 1. Configuración de credenciales y URL JDBC
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));

            // 2. Configuración de capacidad del Pool
            // Máximo de conexiones que el pool mantendrá abiertas
            // Máximo de conexiones que el pool mantendrá abiertas
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.maxPoolSize")));

            // Tiempo máximo de espera para obtener una conexión (en milisegundos)
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.timeout")));

            // 3. Optimizaciones específicas para rendimiento (especialmente en MySQL)
            // Habilita el caché de sentencias preparadas para mejorar la velocidad
            config.addDataSourceProperty("cachePrepStmts", "true");
            // Define cuántas sentencias SQL se mantendrán en el caché
            config.addDataSourceProperty("prepStmtCacheSize", "250");

            // Inicialización del DataSource con la configuración proporcionada
            dataSource = new HikariDataSource(config);

        } catch (IOException e) {
            // Error al intentar leer el flujo del archivo de propiedades
            throw new RuntimeException("Error crítico: No se pudo cargar la configuración de la BD", e);
        } catch (NumberFormatException e) {
            // Error si los valores numéricos en el .properties no son válidos
            throw new RuntimeException("Error: Formato numérico inválido en db.properties", e);
        }
    }

    /**
     * Proporciona una conexión activa desde el pool.
     * @return Connection objeto de conexión JDBC.
     * @throws SQLException si ocurre un error al obtener la conexión.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
