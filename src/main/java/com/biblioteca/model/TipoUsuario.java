package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Importa las validaciones (NotNull, Min, etc.)
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TipoUsuario")
public class TipoUsuario {

    @Id // Marca este campo como la llave primaria[cite: 1]
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Indica que el ID es autoincrementable en SQL[cite: 1]
    private Integer idTipo;

    @NotBlank(message = "El nombre del rol no puede estar vacío") // Valida que no sea nulo ni solo espacios[cite: 1]
    @Size(max = 50, message = "El nombre del rol no debe exceder los 50 caracteres") // Límite de caracteres[cite: 1]
    @Column(name = "nombre_rol", nullable = false, length = 50)
    private String nombreRol;

    @NotNull(message = "Debe especificar el máximo de libros") // Valida que el campo no sea nulo[cite: 1]
    @Min(value = 1, message = "El máximo de libros debe ser al menos 1") // No permite números menores a 1[cite: 1]
    @Column(name = "max_libros_permitidos", nullable = false)
    private Integer maxLibrosPermitidos;

    @NotNull(message = "Debe especificar el máximo de días")
    @Min(value = 1, message = "El máximo de días debe ser al menos 1")
    @Column(name = "max_dias_prestamo", nullable = false)
    private Integer maxDiasPrestamo;
}