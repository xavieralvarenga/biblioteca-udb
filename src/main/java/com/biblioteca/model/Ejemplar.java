package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Entity
@Table(name = "Ejemplar")
public class Ejemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEjemplar;

    @NotNull(message = "Debe estar asociado a un documento base")
    @ManyToOne
    @JoinColumn(name = "id_documento", nullable = false)
    private Documento documento;

    @NotBlank(message = "El código de barras del ejemplar es único y obligatorio")
    @Column(name = "codigo_de_barras", nullable = false, unique = true, length = 50)
    private String codigoBarrasUnico;

    @Column(length = 50)
    private String estado = "Disponible";
}