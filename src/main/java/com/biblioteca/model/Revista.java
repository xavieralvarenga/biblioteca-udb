package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // Importante: incluye los campos de Documento en el equals/hashCode
@Entity
@Table(name = "Revista")
@PrimaryKeyJoinColumn(name = "id_documento") // Vincula esta tabla con la PK de la tabla Documento
public class Revista extends Documento {

    @NotBlank(message = "El ISSN es obligatorio para las revistas")
    @Size(max = 30, message = "El ISSN no debe exceder los 30 caracteres")
    @Column(length = 30)
    private String issn;

    @NotBlank(message = "El volumen es obligatorio")
    @Size(max = 50)
    @Column(length = 50)
    private String volumen;

    @NotBlank(message = "El mes de publicación es obligatorio")
    @Size(max = 50)
    @Column(name = "mes_publicacion", length = 50)
    private String mesPublicacion;

    public Revista(int idDocumento, String issn, String volumen, String mesPublicacion) {
        super();
        this.issn = issn;
        this.volumen = volumen;
        this.mesPublicacion = mesPublicacion;
    }
}