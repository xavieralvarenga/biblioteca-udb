package com.biblioteca.service;

import com.biblioteca.model.Ejemplar;

import java.util.List;

/**
 * Interfaz que define los servicios disponibles para la gestión de ejemplares físicos.
 * Actúa como el contrato que debe cumplir cualquier implementación de negocio.
 */
public interface IEjemplarService {

    /**
     * Filtra los ejemplares basándose en criterios de búsqueda.
     * @param texto Cadena que puede ser título, autor o código de barras.
     * @param idTipoDoc ID del tipo de documento para filtrar la búsqueda.
     * @return Una lista de arreglos de objetos lista para ser mostrada en la JTable.
     */
    List<Object[]> filtrarEjemplares(String texto, Integer idTipoDoc);

    boolean guardarEjemplar(Ejemplar ej);
}