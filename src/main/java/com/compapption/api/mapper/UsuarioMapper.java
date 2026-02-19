package com.compapption.api.mapper;

import com.compapption.api.dto.usuario.UsuarioDTO;
import com.compapption.api.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {

    UsuarioDTO toDTO(Usuario usuario);

    List<UsuarioDTO> toDTOList(List<Usuario> usuarios);
}
