package com.biblioteca.repository.impl;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    /**
     * Obtiene todos los ítems prestados.
     * OJO: Ahora el "ID" de la tabla será el id_detalle, ya que las operaciones se hacen por ítem.
     */
    public List<Object[]> obtenerTodosLosPrestamos() {
        List<Object[]> lista = new ArrayList<>();

        String sql = "SELECT dp.id_detalle, u.carnet_docente_alumno, u.Nombres, u.Apellidos, " +
                "e.codigo_de_barras, d.titulo, p.fecha_prestamo, dp.fecha_limite, dp.estado_item, dp.estado_pago_mora " +
                "FROM Detalle_Prestamo dp " +
                "INNER JOIN Prestamo p ON dp.id_prestamo = p.id_prestamo " +
                "INNER JOIN Usuarios u ON p.id_usuario = u.ID_Usuario " +
                "INNER JOIN Ejemplar e ON dp.id_ejemplar = e.id_ejemplar " +
                "INNER JOIN Documento d ON e.id_documento = d.id_documento " +
                "ORDER BY dp.id_detalle DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombreCompleto = rs.getString("Nombres") + " " + rs.getString("Apellidos");
                lista.add(new Object[]{
                        rs.getInt("id_detalle"), // Usamos id_detalle en lugar de id_prestamo
                        rs.getString("carnet_docente_alumno"),
                        nombreCompleto,
                        rs.getString("codigo_de_barras"),
                        rs.getString("titulo"),
                        rs.getDate("fecha_prestamo"),
                        rs.getDate("fecha_limite"),
                        rs.getString("estado_item"),
                        rs.getString("estado_pago_mora")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los detalles de préstamos: " + e.getMessage());
        }
        return lista;
    }
    public List<Object[]> obtenerPrestamosCabecera() {
        List<Object[]> lista = new ArrayList<>();
        com.biblioteca.model.Usuario user = com.biblioteca.util.SessionManager.getInstance().getUsuarioLogueado();

        // Consulta base con TODAS las columnas necesarias
        StringBuilder sql = new StringBuilder(
                "SELECT p.id_prestamo, u.carnet_docente_alumno, u.Nombres, u.Apellidos, p.fecha_prestamo, p.estado_general, " +
                        "(SELECT COUNT(*) FROM Detalle_Prestamo dp WHERE dp.id_prestamo = p.id_prestamo) AS total_items " +
                        "FROM Prestamo p INNER JOIN Usuarios u ON p.id_usuario = u.ID_Usuario"
        );

        // Si NO es administrador (rol 1), agregamos el filtro para que solo vea sus tickets
        if (user != null && user.getTipoUsuario().getIdTipo() != 1) {
            sql.append(" WHERE p.id_usuario = ").append(user.getIdUsuario());
        }

        sql.append(" ORDER BY p.id_prestamo DESC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{
                        rs.getInt("id_prestamo"),
                        rs.getString("carnet_docente_alumno"),
                        rs.getString("Nombres") + " " + rs.getString("Apellidos"),
                        rs.getDate("fecha_prestamo"),
                        rs.getInt("total_items"),
                        rs.getString("estado_general")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error crítico al cargar cabeceras: " + e.getMessage());
        }
        return lista;
    }
    public List<Object[]> obtenerDetallesPorPrestamo(int idPrestamo) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT dp.id_detalle, e.codigo_de_barras, d.titulo, dp.fecha_limite, dp.estado_item, dp.estado_pago_mora " +
                "FROM Detalle_Prestamo dp INNER JOIN Ejemplar e ON dp.id_ejemplar = e.id_ejemplar " +
                "INNER JOIN Documento d ON e.id_documento = d.id_documento WHERE dp.id_prestamo = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getInt("id_detalle"), rs.getString("codigo_de_barras"), rs.getString("titulo"),
                            rs.getDate("fecha_limite"), rs.getString("estado_item"), rs.getString("estado_pago_mora")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }


    public boolean registrarNuevoPrestamo(int idUsuario, java.time.LocalDate fechaPrestamo,
                                          List<Integer> idsEjemplares, List<java.time.LocalDate> fechasLimites) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false); // 🔴 Iniciar Transacción

            // 1. Insertar Cabecera (Prestamo)
            String sqlCabecera = "INSERT INTO Prestamo (id_usuario, fecha_prestamo, estado_general) VALUES (?, ?, 'Activo')";
            int idPrestamoGenerado = 0;

            // Usamos RETURN_GENERATED_KEYS para atrapar el ID que MySQL le asigne a la cabecera
            try (PreparedStatement psCabecera = con.prepareStatement(sqlCabecera, Statement.RETURN_GENERATED_KEYS)) {
                psCabecera.setInt(1, idUsuario);
                psCabecera.setDate(2, java.sql.Date.valueOf(fechaPrestamo));
                psCabecera.executeUpdate();

                try (ResultSet rs = psCabecera.getGeneratedKeys()) {
                    if (rs.next()) {
                        idPrestamoGenerado = rs.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID del Préstamo generado.");
                    }
                }
            }

            // 2. Insertar Detalles y Actualizar Ejemplares
            String sqlDetalle = "INSERT INTO Detalle_Prestamo (id_prestamo, id_ejemplar, fecha_limite, estado_item) VALUES (?, ?, ?, 'Activo')";
            String sqlEjemplar = "UPDATE Ejemplar SET estado = 'Prestado' WHERE id_ejemplar = ?";

            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psEjemplar = con.prepareStatement(sqlEjemplar)) {

                for (int i = 0; i < idsEjemplares.size(); i++) {
                    // Insertar en Detalle_Prestamo
                    psDetalle.setInt(1, idPrestamoGenerado);
                    psDetalle.setInt(2, idsEjemplares.get(i));
                    psDetalle.setDate(3, java.sql.Date.valueOf(fechasLimites.get(i)));
                    psDetalle.executeUpdate();

                    // Actualizar Ejemplar
                    psEjemplar.setInt(1, idsEjemplares.get(i));
                    psEjemplar.executeUpdate();
                }
            }

            con.commit(); // 🟢 Confirmar todo el carrito
            return true;

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /**
     * Obtiene datos extra para la devolución, ahora basado en id_detalle.
     */
    public Object[] obtenerDatosParaDevolucion1(int idDetalle) throws SQLException {
        String sql = "SELECT dp.id_ejemplar, dp.fecha_limite, td.valor_mora, p.id_usuario " +
                "FROM Detalle_Prestamo dp " +
                "INNER JOIN Prestamo p ON dp.id_prestamo = p.id_prestamo " +
                "INNER JOIN Ejemplar e ON dp.id_ejemplar = e.id_ejemplar " +
                "INNER JOIN Documento d ON e.id_documento = d.id_documento " +
                "INNER JOIN TipoDocumento td ON d.id_tipo_doc = td.id_tipo_doc " +
                "WHERE dp.id_detalle = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("id_ejemplar"),
                            rs.getDate("fecha_limite").toLocalDate(),
                            rs.getDouble("valor_mora"),
                            rs.getInt("id_usuario")
                    };
                }
            }
        }
        throw new SQLException("El detalle " + idDetalle + " no existe.");
    }
    /**
     * Obtiene datos extra para la devolución, ahora con tarifa dinámica por año.
     */
    public Object[] obtenerDatosParaDevolucion(int idDetalle) throws SQLException {
        // Usamos COALESCE y una Subconsulta para obtener la mora del año actual.
        // Si no existe registro para el año actual, devuelve 0.50 por defecto.
        String sql = "SELECT dp.id_ejemplar, dp.fecha_limite, p.id_usuario, " +
                "(SELECT COALESCE(tarifa_diaria, 0.50) FROM Mora_Anual WHERE anio = YEAR(CURDATE())) as tarifa_anual " +
                "FROM Detalle_Prestamo dp " +
                "INNER JOIN Prestamo p ON dp.id_prestamo = p.id_prestamo " +
                "WHERE dp.id_detalle = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("id_ejemplar"),
                            rs.getDate("fecha_limite").toLocalDate(),
                            rs.getDouble("tarifa_anual"), // <-- Aquí viaja la mora del año
                            rs.getInt("id_usuario")
                    };
                }
            }
        }
        throw new SQLException("El detalle " + idDetalle + " no existe.");
    }

    /**
     * Registra la devolución de UN ítem específico y gestiona pagos.
     */
    public boolean registrarDevolucionConPago(int idDetalle, int idEjemplar, int idUsuario,
                                              int diasRetraso, double montoCalculado,
                                              double montoPagado, String observaciones) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            String estadoPago = (montoCalculado <= 0) ? "Sin Mora" : (montoPagado >= montoCalculado) ? "Pagado" : "Pendiente";

            // 1. Actualizar Detalle
            String sqlDetalle = "UPDATE Detalle_Prestamo SET estado_item = 'Devuelto', fecha_devolucion_real = CURDATE(), " +
                    "dias_retraso = ?, monto_mora = ?, monto_pagado = ?, estado_pago_mora = ? WHERE id_detalle = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, diasRetraso);
                ps.setDouble(2, montoCalculado);
                ps.setDouble(3, montoPagado);
                ps.setString(4, estadoPago);
                ps.setInt(5, idDetalle);
                ps.executeUpdate();
            }

            // 2. Liberar Ejemplar
            String sqlEjemplar = "UPDATE Ejemplar SET estado = 'Disponible' WHERE id_ejemplar = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlEjemplar)) {
                ps.setInt(1, idEjemplar);
                ps.executeUpdate();
            }

            // 3. Registrar Devolución
            String sqlDevolucion = "INSERT INTO Devolucion (id_detalle, fecha_devolucion, observaciones_estado_fisico) VALUES (?, NOW(), ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlDevolucion)) {
                ps.setInt(1, idDetalle);
                ps.setString(2, observaciones);
                ps.executeUpdate();
            }

            // 4. Actualizar Solvencia de Usuario
            String sqlCheckMora = "SELECT COUNT(*) FROM Detalle_Prestamo dp INNER JOIN Prestamo p ON dp.id_prestamo = p.id_prestamo " +
                    "WHERE p.id_usuario = ? AND dp.estado_pago_mora = 'Pendiente'";
            boolean sigueConMora = false;
            try (PreparedStatement ps = con.prepareStatement(sqlCheckMora)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) sigueConMora = rs.getInt(1) > 0;
                }
            }

            String sqlUpdateUsuario = "UPDATE Usuarios SET estado_mora = ? WHERE ID_Usuario = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateUsuario)) {
                ps.setBoolean(1, sigueConMora);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
            // 6. Actualizar estado de la Cabecera invocando al evaluador
            recalcularEstadoCabecera(con, idDetalle);
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /**
     * Búsqueda con filtros avanzados (Cabecera + Detalle).
     */
    public List<Object[]> buscarPrestamosConFiltro(String texto, String estado, String estadoPago) {
        List<Object[]> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT dp.id_detalle, u.carnet_docente_alumno, u.Nombres, u.Apellidos, " +
                        "e.codigo_de_barras, d.titulo, p.fecha_prestamo, dp.fecha_limite, dp.estado_item, dp.estado_pago_mora " +
                        "FROM Detalle_Prestamo dp " +
                        "INNER JOIN Prestamo p ON dp.id_prestamo = p.id_prestamo " +
                        "INNER JOIN Usuarios u ON p.id_usuario = u.ID_Usuario " +
                        "INNER JOIN Ejemplar e ON dp.id_ejemplar = e.id_ejemplar " +
                        "INNER JOIN Documento d ON e.id_documento = d.id_documento " +
                        "WHERE 1=1"
        );

        if (estado != null && !estado.equals("Todos")) {
            if (estado.equals("Activos")) {
                sql.append(" AND dp.estado_item = 'Activo' AND dp.fecha_limite >= CURDATE()");
            } else if (estado.equals("Vencidos")) {
                sql.append(" AND dp.estado_item = 'Activo' AND dp.fecha_limite < CURDATE()");
            } else if (estado.equals("Devueltos")) {
                sql.append(" AND dp.estado_item = 'Devuelto'");
            }
        }

        if (estadoPago != null && !estadoPago.equals("Todos")) {
            sql.append(" AND dp.estado_pago_mora = '").append(estadoPago).append("'");
        }

        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (u.carnet_docente_alumno LIKE ? OR e.codigo_de_barras LIKE ? OR d.titulo LIKE ?)");
        }

        sql.append(" ORDER BY dp.id_detalle DESC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (texto != null && !texto.trim().isEmpty()) {
                String search = "%" + texto.trim() + "%";
                ps.setString(1, search);
                ps.setString(2, search);
                ps.setString(3, search);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombreCompleto = rs.getString("Nombres") + " " + rs.getString("Apellidos");
                    String estadoReal = rs.getString("estado_item");
                    java.sql.Date fechaLimite = rs.getDate("fecha_limite");

                    if ("Activo".equals(estadoReal) && fechaLimite != null && fechaLimite.toLocalDate().isBefore(java.time.LocalDate.now())) {
                        estadoReal = "Vencido";
                    }

                    lista.add(new Object[]{
                            rs.getInt("id_detalle"),
                            rs.getString("carnet_docente_alumno"),
                            nombreCompleto,
                            rs.getString("codigo_de_barras"),
                            rs.getString("titulo"),
                            rs.getDate("fecha_prestamo"),
                            fechaLimite,
                            estadoReal,
                            rs.getString("estado_pago_mora")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar detalles: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Filtra los tickets (Cabeceras) por texto (carnet o nombre) y por estado general.
     */
    public List<Object[]> buscarPrestamosCabeceraConFiltro(String texto, String estado) {
        List<Object[]> lista = new ArrayList<>();
        com.biblioteca.model.Usuario user = com.biblioteca.util.SessionManager.getInstance().getUsuarioLogueado();

        StringBuilder sql = new StringBuilder(
                "SELECT p.id_prestamo, u.carnet_docente_alumno, u.Nombres, u.Apellidos, p.fecha_prestamo, p.estado_general, " +
                        "(SELECT COUNT(*) FROM Detalle_Prestamo dp WHERE dp.id_prestamo = p.id_prestamo) AS total_items " +
                        "FROM Prestamo p INNER JOIN Usuarios u ON p.id_usuario = u.ID_Usuario " +
                        "WHERE 1=1"
        );

        // Filtro de seguridad obligatorio: Si no es admin, solo busca entre sus propios préstamos
        if (user != null && user.getTipoUsuario().getIdTipo() != 1) {
            sql.append(" AND p.id_usuario = ").append(user.getIdUsuario());
        }

        // Filtro del ComboBox (Estado)
        if (estado != null && !estado.equals("Todos")) {
            sql.append(" AND p.estado_general = ?");
        }

        // Filtro de la caja de texto (Carnet o Nombre)
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (u.carnet_docente_alumno LIKE ? OR u.Nombres LIKE ? OR u.Apellidos LIKE ?)");
        }

        sql.append(" ORDER BY p.id_prestamo DESC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (estado != null && !estado.equals("Todos")) {
                ps.setString(paramIndex++, estado);
            }

            if (texto != null && !texto.trim().isEmpty()) {
                String search = "%" + texto.trim() + "%";
                ps.setString(paramIndex++, search);
                ps.setString(paramIndex++, search);
                ps.setString(paramIndex++, search);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                            rs.getInt("id_prestamo"),
                            rs.getString("carnet_docente_alumno"),
                            rs.getString("Nombres") + " " + rs.getString("Apellidos"),
                            rs.getDate("fecha_prestamo"),
                            rs.getInt("total_items"),
                            rs.getString("estado_general")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar cabeceras: " + e.getMessage());
        }
        return lista;
    }
    public boolean abonarMora(int idDetalle, int idUsuario, double montoAbono) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Actualizar el monto pagado en el detalle
            // ¡CORRECCIÓN! Primero evaluamos el estado (con el valor antiguo + abono), luego sumamos el dinero.
            String sqlPago = "UPDATE Detalle_Prestamo " +
                    "SET estado_pago_mora = IF(monto_pagado + ? >= monto_mora, 'Pagado', 'Pendiente'), " +
                    "monto_pagado = monto_pagado + ? " +
                    "WHERE id_detalle = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlPago)) {
                ps.setDouble(1, montoAbono);
                ps.setDouble(2, montoAbono);
                ps.setInt(3, idDetalle);
                ps.executeUpdate();
            }

            // 2. REGLA DE NEGOCIO: Recalcular si el usuario ya no debe NADA
            String sqlCheck = "SELECT COUNT(*) FROM Detalle_Prestamo dp INNER JOIN Prestamo p ON dp.id_prestamo = p.id_prestamo " +
                    "WHERE p.id_usuario = ? AND dp.estado_pago_mora = 'Pendiente'";
            boolean sigueDeudor = false;
            try (PreparedStatement ps = con.prepareStatement(sqlCheck)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) sigueDeudor = rs.getInt(1) > 0;
                }
            }

            // 3. Actualizar el estado global del usuario
            String sqlUser = "UPDATE Usuarios SET estado_mora = ? WHERE ID_Usuario = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUser)) {
                ps.setBoolean(1, sigueDeudor);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }

            // 4. Evaluar si la cabecera ya se puede cerrar
            recalcularEstadoCabecera(con, idDetalle);

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.close();
        }
    }
    /**
     * Método Auxiliar: Evalúa los libros devueltos y las deudas pendientes para decidir el estado del ticket.
     */
    private void recalcularEstadoCabecera(Connection con, int idDetalle) throws SQLException {
        // 1. Descubrir a qué préstamo (Cabecera) pertenece este detalle
        int idPrestamoCabecera = 0;
        try (PreparedStatement ps = con.prepareStatement("SELECT id_prestamo FROM Detalle_Prestamo WHERE id_detalle = ?")) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) idPrestamoCabecera = rs.getInt(1);
            }
        }

        if (idPrestamoCabecera > 0) {
            // 2. Contar totales, devueltos físicos y deudas pendientes
            String sqlCheckCabecera = "SELECT COUNT(*) AS total, " +
                    "SUM(IF(estado_item = 'Devuelto', 1, 0)) AS devueltos, " +
                    "SUM(IF(estado_pago_mora = 'Pendiente', 1, 0)) AS con_deuda " +
                    "FROM Detalle_Prestamo WHERE id_prestamo = ?";

            int totalItems = 0, devueltos = 0, conDeuda = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlCheckCabecera)) {
                ps.setInt(1, idPrestamoCabecera);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalItems = rs.getInt("total");
                        devueltos = rs.getInt("devueltos");
                        conDeuda = rs.getInt("con_deuda");
                    }
                }
            }

            // 3. Lógica de decisión de estado
            String nuevoEstadoCabecera = "Activo";
            if (devueltos > 0 && devueltos < totalItems) {
                nuevoEstadoCabecera = "Parcial";
            } else if (devueltos == totalItems) {
                if (conDeuda > 0) {
                    nuevoEstadoCabecera = "Con Deuda"; // <-- NUEVO ESTADO: Libros aquí, pero falta plata
                } else {
                    nuevoEstadoCabecera = "Finalizado"; // Todo limpio
                }
            }

            // 4. Actualizar la cabecera
            try (PreparedStatement ps = con.prepareStatement("UPDATE Prestamo SET estado_general = ? WHERE id_prestamo = ?")) {
                ps.setString(1, nuevoEstadoCabecera);
                ps.setInt(2, idPrestamoCabecera);
                ps.executeUpdate();
            }
        }
    }
    // 1. VALIDADOR DE FECHA (Verifica si el préstamo se hizo hoy)
    public boolean esPrestamoDeHoy(int idPrestamo) throws SQLException {
        String sql = "SELECT fecha_prestamo FROM Prestamo WHERE id_prestamo = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDate("fecha_prestamo").toLocalDate().isEqual(java.time.LocalDate.now());
                }
            }
        }
        return false;
    }

    // 2. CAMBIAR LECTOR (Cabecera)
    public boolean cambiarLector(int idPrestamo, int nuevoIdUsuario, int nuevosDiasPrestamo) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            // A. Cambiar usuario en la cabecera
            try (PreparedStatement ps = con.prepareStatement("UPDATE Prestamo SET id_usuario = ? WHERE id_prestamo = ?")) {
                ps.setInt(1, nuevoIdUsuario);
                ps.setInt(2, idPrestamo);
                ps.executeUpdate();
            }

            // B. Recalcular las fechas límite de TODOS los libros del carrito según el rol del nuevo usuario
            String sqlFechas = "UPDATE Detalle_Prestamo dp INNER JOIN Prestamo p ON dp.id_prestamo = p.id_prestamo " +
                    "SET dp.fecha_limite = DATE_ADD(p.fecha_prestamo, INTERVAL ? DAY) WHERE p.id_prestamo = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlFechas)) {
                ps.setInt(1, nuevosDiasPrestamo);
                ps.setInt(2, idPrestamo);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.close();
        }
    }

    // 3. CAMBIAR MATERIAL (Detalle)
    public boolean cambiarEjemplar(int idDetalle, int idEjemplarAntiguo, int idEjemplarNuevo) throws SQLException {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            // A. Liberar el libro que el bibliotecario había escaneado por error
            try (PreparedStatement ps = con.prepareStatement("UPDATE Ejemplar SET estado = 'Disponible' WHERE id_ejemplar = ?")) {
                ps.setInt(1, idEjemplarAntiguo);
                ps.executeUpdate();
            }

            // B. Marcar como prestado el nuevo libro correcto
            try (PreparedStatement ps = con.prepareStatement("UPDATE Ejemplar SET estado = 'Prestado' WHERE id_ejemplar = ?")) {
                ps.setInt(1, idEjemplarNuevo);
                ps.executeUpdate();
            }

            // C. Actualizar el detalle con el nuevo ID
            try (PreparedStatement ps = con.prepareStatement("UPDATE Detalle_Prestamo SET id_ejemplar = ? WHERE id_detalle = ?")) {
                ps.setInt(1, idEjemplarNuevo);
                ps.setInt(2, idDetalle);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.close();
        }
    }
}