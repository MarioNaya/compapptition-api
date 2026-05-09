package com.compapptition.api.mapper;

import com.compapptition.api.dto.usuario.UsuarioDTO;
import com.compapptition.api.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper MapStruct para convertir entre entidades Usuario y su DTO.
 *
 * @author Mario
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {

    /**
     * Convierte una entidad Usuario a su DTO.
     *
     * @param usuario entidad de origen
     * @return DTO con los datos del usuario
     */
    UsuarioDTO toDTO(Usuario usuario);

    /**
     * Convierte una lista de entidades Usuario a una lista de DTOs.
     *
     * @param usuarios lista de entidades de origen
     * @return lista de DTOs de usuario
     */
    List<UsuarioDTO> toDTOList(List<Usuario> usuarios);
}
