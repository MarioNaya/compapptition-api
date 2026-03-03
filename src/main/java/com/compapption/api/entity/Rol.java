package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa un rol del sistema con comportamiento similar a un enum persistido en base de datos.
 * Mapeada a la tabla {@code rol}, los valores posibles son: ADMIN_SISTEMA, ADMIN_COMPETICION,
 * ARBITRO, MANAGER_EQUIPO, JUGADOR e INVITADO.
 * Es referenciada por {@link UsuarioRolCompeticion} para asignar roles a usuarios en competiciones concretas.
 *
 * @author Mario
 */
@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RolNombre nombre;

    @Column
    private String descripcion;

    public enum RolNombre {
        ADMIN_SISTEMA,
        ADMIN_COMPETICION,
        ARBITRO,
        MANAGER_EQUIPO,
        JUGADOR,
        INVITADO
    }
}
