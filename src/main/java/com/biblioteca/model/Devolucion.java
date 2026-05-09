package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime; // Cambiamos a LocalDateTime para mayor precisión

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Devolucion")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Integer idDevolucion;

    @NotNull(message = "La devolución debe estar vinculada a un ítem específico del préstamo")
    @OneToOne // Una devolución corresponde exactamente a un detalle de ítem
    @JoinColumn(name = "id_detalle", nullable = false, unique = true)
    private DetallePrestamo detallePrestamo;

    @NotNull(message = "La fecha de devolución es obligatoria")
    @PastOrPresent(message = "La fecha de devolución no puede ser futura")
    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fechaDevolucion = LocalDateTime.now();

    @Lob // Para textos largos (TEXT en MySQL)
    @Column(name = "observaciones_estado_fisico", columnDefinition = "TEXT")
    private String observacionesEstadoFisico;
}