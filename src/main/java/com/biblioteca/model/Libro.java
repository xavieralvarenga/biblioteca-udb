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
}