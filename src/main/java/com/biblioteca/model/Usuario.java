package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Usuario")
    private Integer idUsuario;

    @NotNull(message = "El tipo de usuario es obligatorio")
    @ManyToOne // Relación: Muchos usuarios pertenecen a un TipoUsuario[cite: 1]
    @JoinColumn(name = "id_tipo", nullable = false) // Define la columna de la llave foránea (FK)[cite: 1]
    private TipoUsuario tipoUsuario;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100)
    @Column(name = "Nombres", nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    @Column(name = "Apellidos", nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El carnet es obligatorio")
    @Column(name = "carnet_docente_alumno", nullable = false, unique = true, length = 20)
    private String carnet;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "estado_mora")
    private Boolean estadoMora = false; // Por defecto sin mora (0 en SQL)[cite: 1]

    @Column(name = "Estado", length = 20)
    private String estado = "Activo";
}