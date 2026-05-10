package com.biblioteca.repository.impl;

import com.biblioteca.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoraAnualDAO {

    public List<Object[]> obtenerTodasLasTarifas() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT anio, tarifa_diaria FROM Mora_Anual ORDER BY anio DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Object[]{rs.getInt("anio"), rs.getDouble("tarifa_diaria")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean guardarOActualizar(int anio, double tarifa) throws SQLException {
        // Usamos ON DUPLICATE KEY UPDATE para que si el año ya existe, solo actualice el precio
        String sql = "INSERT INTO Mora_Anual (anio, tarifa_diaria) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE tarifa_diaria = VALUES(tarifa_diaria)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setDouble(2, tarifa);
            return ps.executeUpdate() > 0;
        }
    }
}