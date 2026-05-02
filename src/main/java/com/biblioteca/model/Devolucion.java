package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Devolucion")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDevolucion;

    @NotNull(message = "La devolución debe estar vinculada a un préstamo")
    @OneToOne // Una devolución corresponde exactamente a un préstamo[cite: 1]
    @JoinColumn(name = "id_prestamo", nullable = false)
    private Prestamo prestamo;

    @NotNull(message = "La fecha de devolución es obligatoria")
    @PastOrPresent(message = "La fecha de devolución no puede ser futura") // Validación de lógica temporal[cite: 1]
    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDate fechaDevolucion;

    @Lob // Para textos largos (TEXT en SQL)
    @Column(name = "observaciones_estado_fisico")
    private String observacionesEstadoFisico;
}