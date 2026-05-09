package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true) // Incluye los campos del padre en equals/hashCode
@Entity
@Table(name = "Libro")
@PrimaryKeyJoinColumn(name = "id_documento") // Une esta tabla con el ID del padre
public class Libro extends Documento {
    @Size(max = 30)
    private String isbn;
    private String editorial;
    private String edicion;

    // 1. Agrega este constructor vacío
    public Libro() {
        super(); // Llama al constructor de Documento
    }

    // Constructor sin argumentos requerido por JPA
    public Libro(int idDocumento, String isbn, String editorial, String edicion) {
        super(idDocumento);
        this.isbn = isbn;
        this.editorial = editorial;
        this.edicion = edicion;
    }
}