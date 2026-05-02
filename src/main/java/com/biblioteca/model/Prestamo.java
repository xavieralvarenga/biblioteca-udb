package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Prestamo")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPrestamo;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @NotNull(message = "El ejemplar es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_ejemplar", nullable = false)
    private Ejemplar ejemplar;

    @NotNull(message = "La fecha de préstamo no puede ser nula")
    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @NotNull(message = "La fecha límite no puede ser nula")
    @FutureOrPresent(message = "La fecha límite debe ser hoy o a futuro") // Validación de tiempo
    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(name = "estado_prestamo", length = 20)
    private String estadoPrestamo = "Activo";

    // Campos de Mora (Pueden ser nulos al inicio)
    private Integer diasRetraso;

    @DecimalMin(value = "0.0")
    private BigDecimal montoCalculado;

    private String estadoPago;
}