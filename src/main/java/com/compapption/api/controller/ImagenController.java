package com.compapption.api.controller;

import com.compapption.api.exception.BadRequestException;
import com.compapption.api.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

/**
 * Controlador REST para la subida de imágenes a Cloudinary. Expone endpoints bajo la ruta base
 * {@code /imagenes}.
 *
 * <p>El endpoint {@code POST /imagenes/upload} recibe un {@code MultipartFile} del frontend,
 * delega en {@link CloudinaryService} la validación, firma y envío a Cloudinary, y responde con
 * la URL segura (HTTPS) que el frontend colocará en el formulario de creación/edición de la
 * entidad correspondiente (Equipo, Usuario, Deporte, etc.).</p>
 *
 * <p>La protección de autenticación se aplica por defecto vía {@code SecurityConfig}: el path
 * {@code /imagenes/**} no está en la lista {@code permitAll}, por lo que requiere un JWT válido.</p>
 *
 * @author Mario
 */
@RestController
@RequestMapping("/imagenes")
@RequiredArgsConstructor
public class ImagenController {

    /** Carpetas lógicas permitidas para el parámetro {@code folder}. */
    private static final Set<String> FOLDERS_PERMITIDOS = Set.of(
            "escudos",
            "fotos",
            "iconos",
            "misc"
    );

    private final CloudinaryService cloudinaryService;

    /**
     * POST /imagenes/upload — sube una imagen a Cloudinary y devuelve la URL segura resultante.
     *
     * <p>Valida que el parámetro {@code folder} sea uno de los permitidos
     * ({@code escudos}, {@code fotos}, {@code iconos}, {@code misc}); en caso contrario
     * lanza {@link BadRequestException}.</p>
     *
     * @param file archivo de imagen a subir (multipart/form-data, campo {@code file})
     * @param folder subcarpeta lógica destino dentro del bucket (por defecto {@code misc})
     * @return ResponseEntity 200 OK con un mapa {@code {"url": "<secure_url>"}}
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "misc") String folder) {

        if (!FOLDERS_PERMITIDOS.contains(folder)) {
            throw new BadRequestException(
                    "Carpeta no permitida. Permitidas: escudos, fotos, iconos, misc");
        }

        String url = cloudinaryService.upload(file, folder);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
