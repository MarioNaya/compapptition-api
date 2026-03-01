package com.compapption.api.service;

import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorDetalleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EquipoMapper;
import com.compapption.api.mapper.JugadorMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.equipo.EquipoCreateRequest;
import com.compapption.api.request.equipo.EquipoUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.entity.LogModificacion;
import com.compapption.api.service.log.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;
    private final EquipoManagerRepository equipoManagerRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoMapper equipoMapper;
    private final JugadorMapper jugadorMapper;
    private final LogService logService;

    // === CONSULTAS EQUIPO === //

    // Obtener todos por jerarquía detalle

    @Transactional(readOnly = true)
    public List<EquipoDetalleDTO> obtenerTodosDetalle() {
        return equipoRepository.findAll()
                .stream()
                .map(equipoMapper::toDetalleDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipoSimpleDTO> obtenerTodosSimple() {
        return equipoRepository.findAll()
                .stream()
                .map(equipoMapper::toSimpleDTO)
                .toList();
    }

    // Obtener deporte específico por jerarquía detalle

    @Transactional(readOnly = true)
    public EquipoDetalleDTO obtenerPorIdDetalle(Long id) {
        Equipo equipo = equipoRepository.findByIdWithJugadores(id)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", id));
        return equipoMapper.toDetalleDTO(equipo);
    }

    @Transactional(readOnly = true)
    public EquipoSimpleDTO obtenerPorIdSimple(Long id) {
        Equipo equipo = equipoRepository.findByIdWithJugadores(id)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", id));
        return equipoMapper.toSimpleDTO(equipo);
    }

    // Busqueda de equipo paginada por nombre

    @Transactional(readOnly = true)
    public PageResponse<EquipoSimpleDTO> buscar(String nombre, Pageable pageable){
        Page<Equipo> page = equipoRepository.searchByNombre(nombre, pageable);
        return PageResponse.<EquipoSimpleDTO>builder()
                .content(page.getContent().stream().map(equipoMapper::toSimpleDTO).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    // Obtener lista de equipos por manager

    @Transactional(readOnly = true)
    public List<EquipoSimpleDTO> obtenerPorManager(Long usuarioId){
        return equipoRepository.findByManagersId(usuarioId)
                .stream()
                .map(equipoMapper::toSimpleDTO)
                .toList();
    }

    // Obtener lista de equipos por jugador

    @Transactional(readOnly = true)
    public List<EquipoSimpleDTO> obtenerPorJugador(Long usuarioId){
        return equipoRepository.findByJugadoresId(usuarioId)
                .stream()
                .map(equipoMapper::toSimpleDTO)
                .toList();
    }

    // === CREAR, ELIMINAR Y ACTUALIZAR EQUIPO === //

    @Transactional
    public EquipoDetalleDTO crear(EquipoCreateRequest request) {
        Equipo equipo = Equipo.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .escudo(request.getEscudo())
                .build();

        equipo = equipoRepository.save(equipo);
        logService.registrar("Equipo", equipo.getId(), LogModificacion.AccionLog.CREAR, null, null, null);
        return equipoMapper.toDetalleDTO(equipo);
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", id));

        if (!equipo.getCompeticiones().isEmpty()) {
            throw new BadRequestException("No se puede eliminar un equipo inscrito en competiciones");
        }

        logService.registrar("Equipo", id, LogModificacion.AccionLog.ELIMINAR, null, null, null);
        equipoRepository.delete(equipo);
    }

    @Transactional
    public EquipoSimpleDTO actualizar(Long id, EquipoUpdateRequest request) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", id));

        if (request.getNombre()!=null){
            equipo.setNombre(request.getNombre());
        }
        if (request.getDescripcion()!=null){
            equipo.setDescripcion(request.getDescripcion());
        }
        if (request.getEscudo()!=null){
            equipo.setEscudo(request.getEscudo());
        }

        equipo = equipoRepository.save(equipo);
        logService.registrar("Equipo", equipo.getId(), LogModificacion.AccionLog.EDITAR, null, null, null);
        return equipoMapper.toSimpleDTO(equipo);
    }

    // === GESTIÓN DE USUARIOS === //

    @Transactional
    public void agregarJugador(Long equipoId, Long jugadorId, Integer dorsal) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", equipoId));

        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", jugadorId));

        if (equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(equipoId, jugadorId)) {
            throw new BadRequestException("El jugador ya pertenece a este equipo");
        }

        if (equipoJugadorRepository.findByEquipoIdAndDorsalEquipo(equipoId, dorsal).isPresent()){
            throw new BadRequestException("El dorsal " + dorsal + " ya está asignado");
        }

        EquipoJugador equipoJugador = EquipoJugador.builder()
                .equipo(equipo)
                .jugador(jugador)
                .dorsalEquipo(dorsal)
                .build();

        equipoJugadorRepository.save(equipoJugador);
        logService.registrar("EquipoJugador", jugadorId, LogModificacion.AccionLog.CREAR, null, null, null);
    }

    @Transactional
    public void quitarJugador(Long equipoId, Long jugadorId) {
        EquipoJugador equipoJugador = equipoJugadorRepository
                .findByEquipoIdAndJugadorId(equipoId, jugadorId)
                .orElseThrow(()-> new ResourceNotFoundException("El jugador no pertenece a este equipo"));

        equipoJugador.setActivo(false);
        equipoJugador.setFechaBaja(LocalDateTime.now());
        equipoJugadorRepository.save(equipoJugador);
        logService.registrar("EquipoJugador", jugadorId, LogModificacion.AccionLog.EDITAR, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<JugadorSimpleDTO> obtenerJugadoresSimple(long equipoId){
        if (!equipoRepository.existsById(equipoId)){
            throw new ResourceNotFoundException("Equipo","id", equipoId);
        }

        return jugadorRepository.findByEquipoId(equipoId)
                .stream()
                .map(jugadorMapper::toSimpleDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JugadorDetalleDTO> obtenerJugadoresDetalle(long equipoId){
        if (!equipoRepository.existsById(equipoId)){
            throw new ResourceNotFoundException("Equipo","id", equipoId);
        }

        return jugadorRepository.findByEquipoId(equipoId)
                .stream()
                .map(jugadorMapper::toDetalleDTO)
                .toList();
    }

    // Asignación de manager

    @Transactional
    public void asignarManager(long equipoId, long competicionId, long usuarioId) {
        if (!equipoRepository.existsById(equipoId)) {
            throw new ResourceNotFoundException("Equipo", "id", usuarioId);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", usuarioId));

        if (equipoManagerRepository.existsByEquipoIdAndCompeticionIdAndUsuarioId(equipoId, competicionId, usuarioId)) {
            throw new BadRequestException("El usuario ya es manager de este equipo en esta competición");
        }

        Equipo equipo = equipoRepository.getReferenceById(equipoId);
        Competicion competicion = new Competicion();
        competicion.setId(competicionId);

        EquipoManager manager = EquipoManager.builder()
                .equipo(equipo)
                .competicion(competicion)
                .usuario(usuario)
                .build();

        equipoManagerRepository.save(manager);
    }
}
