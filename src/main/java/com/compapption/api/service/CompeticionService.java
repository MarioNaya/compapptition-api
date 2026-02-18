package com.compapption.api.service;

import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.dto.competicionDTO.CompeticionInfoDTO;
import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.mapper.CompeticionMapper;
import com.compapption.api.mapper.EquipoMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.competicion.CompeticionCreateRequest;
import com.compapption.api.request.competicion.CompeticionUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CompeticionService {

    private final CompeticionRepository competicionRepository;
    private final DeporteRepository deporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompeticionEquipoRepository competicionEquipoRepository;
    private final EquipoRepository equipoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    private final CompeticionMapper competicionMapper;
    private final EquipoMapper equipoMapper;
    private final ClasificacionService clasificacionService;
    private final ConfiguracionCompeticionService configuracionCompeticionService;
    private final UsuarioRolCompeticionService usuarioRolCompeticionService;

    // === CRUD COMPETICIONES === //

    // Consultas //

    @Transactional(readOnly = true)
    public PageResponse<CompeticionSimpleDTO> obtenerPublicas(Pageable pageable){
        Page<Competicion> page = competicionRepository.findByPublicasActivas(pageable);
        return toPageResponseSimple(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<CompeticionSimpleDTO> buscarPublicas(String search, Pageable pageable){
        Page<Competicion> page = competicionRepository.searchPublicas(search, pageable);
        return toPageResponseSimple(page);
    }

    @Transactional(readOnly = true)
    public CompeticionDetalleDTO obtenerPorIdDetalle(Long id){
        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", id));
        return competicionMapper.toDetalleDTO(competicion);
    }

    @Transactional(readOnly = true)
    public CompeticionInfoDTO obtenerPorIdInfo(Long id){
        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", id));
        return competicionMapper.toInfoDTO(competicion);
    }

    @Transactional(readOnly = true)
    public CompeticionSimpleDTO obtenerPorIdSimple(Long id){
        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", id));
        return competicionMapper.toSimpleDTO(competicion);
    }

    @Transactional(readOnly = true)
    public List<CompeticionSimpleDTO> obtenerPorParticipante(Long usuarioId){
        List<Competicion> competiciones = competicionRepository.findByUsuarioParticipante(usuarioId);
        return competicionMapper.toSimpleDTOList(competiciones);
    }

    @Transactional(readOnly = true)
    public List<CompeticionSimpleDTO> obtenerPorCreador(Long usuarioId){
        List<Competicion> competiciones = competicionRepository.findByCreadorId(usuarioId);
        return competicionMapper.toSimpleDTOList(competiciones);
    }

    // Creación //

    @Transactional
    public CompeticionDetalleDTO crear(CompeticionCreateRequest request, Long creadorId){
        Deporte deporte = deporteRepository.findById(request.getDeporteId())
                .orElseThrow(()-> new ResourceNotFoundException("Deporte", "id", request.getDeporteId()));

        Usuario creador = usuarioRepository.findById(creadorId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", creadorId));

        Competicion competicion = Competicion.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .deporte(deporte)
                .creador(creador)
                .publica(request.isPublica())
                .inscripcionAbierta(request.isInscripcionAbierta())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(Competicion.EstadoCompeticion.BORRADOR)
                .build();

        competicion = competicionRepository.save(competicion);

        // Crear configuración
        ConfiguracionCompeticion configuracion = configuracionCompeticionService.crear(
                competicion,
                request.getConfiguracion());
        competicion.setConfiguracion(configuracion);

        // Asignar rol de administrador
        usuarioRolCompeticionService.asignarRolAdminCompeticion(creador, competicion);

        return competicionMapper.toDetalleDTO(competicion);
    }

    // Actualización //

    @Transactional
    public CompeticionDetalleDTO actualizar(
            Long id,
            CompeticionUpdateRequest request,
            Long usuarioId) throws BadRequestException {

        Competicion competicion = competicionRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", id));

        validarPermisoEdicion(competicion, usuarioId);

        if (request.getNombre() != null){
            competicion.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null){
            competicion.setDescripcion(request.getDescripcion());
        }
        competicion.setPublica(request.isPublica());
        competicion.setInscripcionAbierta(request.isInscripcionAbierta());
        competicion.setEstadisticasActivas(request.isEstadisticasActivas());
        if (request.getFechaInicio() != null){
            competicion.setFechaInicio(request.getFechaInicio());
        }
        if (request.getFechaFin() != null){
            competicion.setFechaFin(request.getFechaFin());
        }
        if (request.getEstado() != null){
            validarCambioEstado(competicion, request.getEstado());
            competicion.setEstado(request.getEstado());
        }
        if (request.getConfiguracion() != null){
            configuracionCompeticionService.actualizar(
                    competicion.getConfiguracion(),
                    request.getConfiguracion());
        }

        competicion = competicionRepository.save(competicion);
        return competicionMapper.toDetalleDTO(competicion);
    }

    // Eliminación

    @Transactional
    public void eliminar(long id, long usuarioId) {
        Competicion competicion = competicionRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", id));

        validarPermisoEdicion(competicion, usuarioId);

        if (competicion.getEstado() == Competicion.EstadoCompeticion.ACTIVA) {
            throw new BadRequestException("No se puede eliminar una competición activa");
        }

        competicionRepository.delete(competicion);
    }

    // Cambio de temporada

    @Transactional
    public CompeticionDetalleDTO cambiarTemporada(Long competicionId, Integer nuevaTemporada, Long usuarioId) {
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competicion", "id", competicionId));

        validarPermisoEdicion(competicion, usuarioId);

        if (nuevaTemporada <= competicion.getTemporadaActual()) {
            throw new BadRequestException("La nueva temporada debe ser mayor a la actual");
        }

        competicion.setTemporadaActual(nuevaTemporada);
        competicion = competicionRepository.save(competicion);

        // Inicializar clasificaciones para la nueva temporada
        Competicion finalCompeticion = competicion;
        competicionEquipoRepository.findActivosByCompeticionId(competicionId)
                .forEach(ce -> clasificacionService
                        .inicializarClasificacionEquipo(finalCompeticion, ce.getEquipo()));

        // TODO: generación de calendario, gestión de inscripciones, etc.

        return competicionMapper.toDetalleDTO(competicion);
    }

    // Gestión de equipos

    @Transactional
    public void altaEquipo(long competicionId, long equipoId, long usuarioId) {
        Competicion competicion = competicionRepository.findByIdWithDetails(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competición", "id", competicionId));

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", equipoId));

        if (competicionEquipoRepository.existsByCompeticionIdAndEquipoId(competicionId, equipoId)) {
            throw new BadRequestException("El equipo ya está inscrito en esta competición");
        }

        validarPermisoEdicion(competicion, usuarioId);

        if (!competicion.isInscripcionAbierta()) {
            throw new BadRequestException("Las inscripciones están cerradas");
        }

        CompeticionEquipo inscripcion = CompeticionEquipo.builder()
                .competicion(competicion)
                .equipo(equipo)
                .activo(true)
                .build();

        competicionEquipoRepository.save(inscripcion);

        // Inicializar clasificación para el nuevo equipo
        clasificacionService.inicializarClasificacionEquipo(competicion, equipo);
    }

    @Transactional
    public void bajaEquipo(long competicionId, long equipoId, long usuarioId){
        Competicion competicion = competicionRepository.findById(competicionId)
                .orElseThrow(()-> new ResourceNotFoundException("Competicion", "id", competicionId));

        validarPermisoEdicion(competicion, usuarioId);

        CompeticionEquipo inscripcion = competicionEquipoRepository
                .findByCompeticionIdAndEquipoId(competicionId, equipoId)
                .orElseThrow(()-> new ResourceNotFoundException("Inscripción no encontrada"));

        inscripcion.setActivo(false);
        competicionEquipoRepository.save(inscripcion);
    }

    @Transactional
    public List<EquipoSimpleDTO> obtenerInscritosSimple(long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }

        return competicionEquipoRepository.findActivosByCompeticionId(competicionId)
                .stream()
                .map(ce -> equipoMapper.toSimpleDTO(ce.getEquipo()))
                .toList();
    }

    @Transactional
    public List<EquipoDetalleDTO> obtenerInscritosDetalle(long competicionId){
        if (!competicionRepository.existsById(competicionId)){
            throw new ResourceNotFoundException("Competición", "id", competicionId);
        }

        return competicionEquipoRepository.findActivosByCompeticionId(competicionId)
                .stream()
                .map(ce -> equipoMapper.toDetalleDTO(ce.getEquipo()))
                .toList();
    }

    // === CONVERSIÓN DTOS A PAGE === //

    private PageResponse<CompeticionSimpleDTO> toPageResponseSimple(Page<Competicion> page) {
        return PageResponse.<CompeticionSimpleDTO>builder()
                .content(competicionMapper.toSimpleDTOList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private PageResponse<CompeticionDetalleDTO> toPageResponseDetalle(Page<Competicion> page) {
        return PageResponse.<CompeticionDetalleDTO>builder()
                .content(competicionMapper.toDetalleDTOList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    // === VALIDACIONES ESPECÍFICAS DE COMPETICIONES === //

    private void validarCambioEstado(Competicion competicion, Competicion.EstadoCompeticion nuevoEstado) {
        if (nuevoEstado == Competicion.EstadoCompeticion.ACTIVA) {
            long numEquipos = competicionEquipoRepository.countActivosByCompeticionId(competicion.getId());
            ConfiguracionCompeticion config = competicion.getConfiguracion();
            ConfiguracionCompeticion.FormatoCompeticion formato = config != null
                    ? config.getFormato()
                    : ConfiguracionCompeticion.FormatoCompeticion.LIGA;

            if (formato == ConfiguracionCompeticion.FormatoCompeticion.EVENTO_UNICO) {
                if (numEquipos < 2) {
                    throw new BadRequestException("Un evento único necesita al menos 2 equipos");
                }
            } else if (formato == ConfiguracionCompeticion.FormatoCompeticion.PLAYOFF) {
                if (numEquipos < 2) {
                    throw new BadRequestException("Un playoff necesita al menos 2 equipos");
                }
            } else {
                // Ligas y formatos con fase de liga requieren mínimo 3
                if (numEquipos < 3) {
                    throw new BadRequestException("Una liga necesita al menos 3 equipos para activarse");
                }
            }
        }
    }

    // === PERMISOS DE EDICIÓN === //

    private void validarPermisoEdicion(Competicion competicion, long usuarioId) {
        if (!Objects.equals(competicion.getCreador().getId(), usuarioId) &&
                !usuarioRolCompeticionRepository.existsByUsuarioIdAndCompeticionIdAndRolNombre(
                        usuarioId, competicion.getId(), "ADMIN_COMPETICION")) {
            throw new BadRequestException("No tienes permisos para editar esta competición");
        }
    }
}
