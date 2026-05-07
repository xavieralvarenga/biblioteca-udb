package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Documento")
@Inheritance(strategy = InheritanceType.JOINED) // Crea tablas separadas unidas por ID[cite: 1]
public abstract class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDocumento;

    @NotNull(message = "El tipo de documento es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_tipo_doc", nullable = false)
    private int tipoDocumento;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Column(nullable = false, length = 150)
    private String autor;

    @Column(name = "ubicacion_fisica", length = 100)
    private String ubicacionFisica;

    @Column(name = "codigo_de_barras", unique = true, length = 50)
    private String codigoBarrasObra; // Código general de la obra intelectual

    @Column(length = 50)
    private String estado = "Disponible";

}