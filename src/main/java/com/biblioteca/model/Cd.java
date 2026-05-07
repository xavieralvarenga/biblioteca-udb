package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // Hereda la lógica de comparación de la clase padre
@Entity
@Table(name = "CD")
@PrimaryKeyJoinColumn(name = "id_documento") // Mapeo de herencia JOINED[cite: 1]
public class Cd extends Documento {

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos de 1 minuto")
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @NotBlank(message = "El tipo de contenido es obligatorio (ej: Software, Audio, Video)")
    @Size(max = 100)
    @Column(name = "tipo_contenido", length = 100)
    private String tipoContenido;

    public Cd(int idDocumento, int duracionMinutos, String tipoContenido) {
        super();
        this.duracionMinutos = duracionMinutos;
        this.tipoContenido = tipoContenido;
    }
}