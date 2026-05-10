package com.biblioteca.service;

import com.biblioteca.repository.impl.MoraAnualDAO;
import java.sql.SQLException;
import java.util.List;

public class MoraAnualService {
    private final MoraAnualDAO dao = new MoraAnualDAO();

    public List<Object[]> listarTarifas() {
        return dao.obtenerTodasLasTarifas();
    }

    public void registrarTarifa(int anio, double tarifa) throws SQLException {
        if (anio < 2000 || anio > 2100) throw new IllegalArgumentException("Año fuera de rango válido.");
        if (tarifa < 0) throw new IllegalArgumentException("La tarifa no puede ser negativa.");
        dao.guardarOActualizar(anio, tarifa);
    }
}