package com.biblioteca.service.impl;

import com.biblioteca.model.Ejemplar;
import com.biblioteca.repository.impl.EjemplarDAO;
import com.biblioteca.service.IEjemplarService;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de los servicios de gestión de ejemplares.
 * Coordina la comunicación entre la vista y la capa de datos.
 */
@Log
public class EjemplarServiceImpl implements IEjemplarService {

    private final EjemplarDAO ejemplarDAO;

    /**
     * Constructor que inicializa el acceso a datos.
     */
    public EjemplarServiceImpl() {
        this.ejemplarDAO = new EjemplarDAO();
    }




    /**
     * Implementa la lógica de filtrado de ejemplares.
     * Realiza una limpieza básica de los parámetros antes de consultar al DAO.
     */
    @Override
    public List<Object[]> filtrarEjemplares(String texto, Integer idTipoDoc) {
        try {
            // Regla de Negocio: Si el texto solo tiene espacios, lo tratamos como vacío
            String textoLimpio = (texto != null) ? texto.trim() : "";

            // Si el idTipoDoc es "0" (Todos), lo enviamos como null para el DAO
            Integer filtroTipo = (idTipoDoc != null && idTipoDoc > 0) ? idTipoDoc : null;

            return ejemplarDAO.buscarEjemplares(textoLimpio, filtroTipo);

        } catch (Exception e) {
            log.severe("Error en el servicio al filtrar ejemplares: " + e.getMessage());
            return new ArrayList<>(); // Retornamos lista vacía para evitar NullPointerException en la vista
        }
    }


    @Override
    public boolean guardarEjemplar(Ejemplar ej) {
        // Extraemos el ID del documento base para la persistencia en la tabla Ejemplar
        Integer idDoc = ej.getDocumento().getIdDocumento();
        return ejemplarDAO.insertar(ej, idDoc);
    }

    @Override
    public List<Object[]> obtenerEjemplaresPorDocumento(Integer idDocumento) {
        try {
            return ejemplarDAO.obtenerEjemplaresPorDocumento(idDocumento);
        } catch (Exception e) {
            log.severe("Error al obtener ejemplares por ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }


}