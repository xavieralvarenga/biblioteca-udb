package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Representa la unidad física (copia) de un documento bibliográfico.
 * Cada ejemplar tiene su propio estado de disponibilidad y código de barras único.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Ejemplar")
public class Ejemplar {

    /** Identificador único autoincremental del ejemplar físico */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejemplar")
    private Integer idEjemplar;

    /** * Documento base al que pertenece este ejemplar.
     * Relación Muchos a Uno: varios ejemplares pertenecen a un mismo título.
     */
    @NotNull(message = "Debe estar asociado a un documento base")
    @ManyToOne(fetch = FetchType.LAZY) // Lazy para optimizar el rendimiento de carga
    @JoinColumn(name = "id_documento", nullable = false)
    private Documento documento;

    /** Código de barras pegado físicamente al ejemplar (etiqueta patrimonial) */
    @NotBlank(message = "El código de barras del ejemplar es único y obligatorio")
    @Column(name = "codigo_de_barras", nullable = false, unique = true, length = 50)
    private String codigoBarrasUnico;

    /** * Estado actual del ejemplar.
     * Valores sugeridos: 'Disponible', 'Prestado', 'Dañado', 'Extraviado'.
     */
    @Column(length = 50, nullable = false)
    @Builder.Default // Asegura que Lombok Builder respete el valor por defecto
    private String estado = "Disponible";
}