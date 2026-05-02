package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TipoDocumento")
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTipoDoc;

    @NotBlank(message = "El nombre del documento es obligatorio")
    @Column(name = "Nombre", nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El parámetro de mora es obligatorio")
    @Column(name = "Parametro_mora", nullable = false, length = 50)
    private String parametroMora;

    @NotNull(message = "El valor de la mora no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = true, message = "La mora no puede ser negativa") // Valida números decimales[cite: 1]
    @Column(name = "valor_mora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMora;
}
