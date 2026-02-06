package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipo_manager", uniqueConstraints = @UniqueConstraint(columnNames = {"equipo_id", "competicion_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;


}