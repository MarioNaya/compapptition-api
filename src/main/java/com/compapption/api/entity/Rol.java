package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private long id;

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
