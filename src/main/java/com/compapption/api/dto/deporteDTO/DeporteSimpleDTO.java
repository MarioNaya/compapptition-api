package com.compapption.api.dto.deporteDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeporteSimpleDTO {

    private long id;
    private String nombre;
    private boolean activo;
}
