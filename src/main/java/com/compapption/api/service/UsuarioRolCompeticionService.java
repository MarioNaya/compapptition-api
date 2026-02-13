package com.compapption.api.service;

import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Rol;
import com.compapption.api.entity.Usuario;
import com.compapption.api.entity.UsuarioRolCompeticion;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.repository.RolRepository;
import com.compapption.api.repository.UsuarioRolCompeticionRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioRolCompeticionService {

    RolRepository rolRepository;
    UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;

    // === ASIGNACIÓN ROL DE USUARIO EN COMPETICIÓN === //

    public void asignarRolAdminCompeticion(Usuario creador, Competicion competicion){
        Rol rolAdmin = rolRepository.findByNombre("ADMIN_COMPETICION")
                .orElseThrow(()-> new ResourceNotFoundException("Rol", "nombre", "ADMIN_COMPETICION"));

        UsuarioRolCompeticion urc = UsuarioRolCompeticion.builder()
                .usuario(creador)
                .competicion(competicion)
                .rol(rolAdmin)
                .build();

        usuarioRolCompeticionRepository.save(urc);
    }
}
