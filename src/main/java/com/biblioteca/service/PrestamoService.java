package com.biblioteca.service;

import com.biblioteca.repository.impl.PrestamoDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PrestamoService {

    private final PrestamoDAO prestamoDAO;

    public PrestamoService() {
        this.prestamoDAO = new PrestamoDAO();
    }

    // ========================================================================
    // NUEVOS MÉTODOS PARA LA ARQUITECTURA MAESTRO-DETALLE
    // ========================================================================

    /**
     * Obtiene los "Tickets" principales (Cabecera) para llenar el Panel general.
     */
    public List<Object[]> obtenerPrestamosCabecera() {
        return prestamoDAO.obtenerPrestamosCabecera();
    }

    /**
     * Obtiene los materiales (Detalles) de un ticket específico.
     */
    public List<Object[]> obtenerDetallesPorPrestamo(int idPrestamo) {
        return prestamoDAO.obtenerDetallesPorPrestamo(idPrestamo);
    }

    // ========================================================================
    // MÉTODOS EXISTENTES MANTENIDOS PARA EL FLUJO
    // ========================================================================

    public List<Object[]> obtenerTodosLosPrestamos() {
        return prestamoDAO.obtenerTodosLosPrestamos();
    }

    public List<Object[]> buscarPrestamosConFiltro(String texto, String estado, String estadoPago) {
        return prestamoDAO.buscarPrestamosConFiltro(texto, estado, estadoPago);
    }

    // Fíjate que aquí recibimos Listas para el "Carrito"
    public boolean registrarNuevoPrestamo(int idUsuario, LocalDate fechaPrestamo,
                                          List<Integer> idsEjemplares, List<LocalDate> fechasLimites) throws SQLException {
        if (idsEjemplares == null || idsEjemplares.isEmpty()) {
            throw new IllegalArgumentException("El carrito de préstamos no puede estar vacío.");
        }
        return prestamoDAO.registrarNuevoPrestamo(idUsuario, fechaPrestamo, idsEjemplares, fechasLimites);
    }

    public Object[] obtenerDatosParaDevolucion(int idDetalle) throws SQLException {
        return prestamoDAO.obtenerDatosParaDevolucion(idDetalle);
    }

    public boolean registrarDevolucionConPago(int idDetalle, int idEjemplar, int idUsuario,
                                              int diasRetraso, double montoCalculado,
                                              double montoPagado, String observaciones) throws SQLException {
        return prestamoDAO.registrarDevolucionConPago(idDetalle, idEjemplar, idUsuario, diasRetraso, montoCalculado, montoPagado, observaciones);
    }

    public boolean abonarMora(int idDetalle, int idUsuario, double montoAbono) throws SQLException {
        if (montoAbono <= 0) {
            throw new IllegalArgumentException("El abono debe ser mayor a cero.");
        }
        return prestamoDAO.abonarMora(idDetalle, idUsuario, montoAbono);
    }

    public boolean esPrestamoDeHoy(int idPrestamo) throws SQLException {
        return prestamoDAO.esPrestamoDeHoy(idPrestamo);
    }

    public boolean cambiarLector(int idPrestamo, int nuevoIdUsuario, int nuevosDiasPrestamo) throws SQLException {
        return prestamoDAO.cambiarLector(idPrestamo, nuevoIdUsuario, nuevosDiasPrestamo);
    }

    public boolean cambiarEjemplar(int idDetalle, int idEjemplarAntiguo, int idEjemplarNuevo) throws SQLException {
        return prestamoDAO.cambiarEjemplar(idDetalle, idEjemplarAntiguo, idEjemplarNuevo);
    }
}