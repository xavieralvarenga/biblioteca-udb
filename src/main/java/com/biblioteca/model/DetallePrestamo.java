package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Detalle_Prestamo")
public class DetallePrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_prestamo", nullable = false)
    private Prestamo prestamo;

    @NotNull(message = "El ejemplar físico es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_ejemplar", nullable = false)
    private Ejemplar ejemplar;

    @NotNull(message = "La fecha límite es obligatoria")
    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    @Column(name = "estado_item", length = 20)
    private String estadoItem = "Activo";

    // Gestión financiera individual por ítem
    @Column(name = "dias_retraso")
    private Integer diasRetraso = 0;

    @DecimalMin(value = "0.0")
    @Column(name = "monto_mora")
    private BigDecimal montoMora = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    @Column(name = "monto_pagado")
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "estado_pago_mora")
    private String estadoPagoMora = "Sin Mora";
}