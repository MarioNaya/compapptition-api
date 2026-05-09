package com.compapptition.api.controller;

import com.compapptition.api.exception.BadRequestException;
import com.compapptition.api.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    /** Tamaño máximo de archivo (5 MB). Coincide con el límite multipart de Spring. */
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

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
        validarArchivo(file);

        String url = cloudinaryService.upload(file, folder);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Valida tamaño máximo (5 MB) y formato real del archivo subido inspeccionando
     * los primeros bytes (magic bytes), no la extensión ni el {@code Content-Type}
     * declarado por el cliente. Acepta PNG, JPEG, WebP, GIF; rechaza explícitamente
     * SVG (puede contener {@code <script>} con XSS) y cualquier otro formato.
     * Cierra S-14.
     */
    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo es obligatorio");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("El archivo excede el tamaño máximo permitido (5 MB)");
        }

        byte[] header = new byte[12];
        try {
            int read = file.getInputStream().read(header);
            if (read < 4) {
                throw new BadRequestException("Archivo demasiado pequeño o ilegible");
            }
        } catch (IOException e) {
            throw new BadRequestException("No se pudo leer el archivo");
        }

        if (!esFormatoImagenPermitido(header)) {
            throw new BadRequestException(
                    "Formato no permitido. Acepta PNG, JPEG, WebP o GIF (no SVG ni otros).");
        }
    }

    private boolean esFormatoImagenPermitido(byte[] h) {
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (h.length >= 8
                && (h[0] & 0xFF) == 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47
                && h[4] == 0x0D && h[5] == 0x0A && (h[6] & 0xFF) == 0x1A && h[7] == 0x0A) {
            return true;
        }
        // JPEG: FF D8 FF
        if (h.length >= 3
                && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return true;
        }
        // GIF: "GIF87a" o "GIF89a"
        if (h.length >= 6
                && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8'
                && (h[4] == '7' || h[4] == '9') && h[5] == 'a') {
            return true;
        }
        // WebP: "RIFF" .... "WEBP"
        if (h.length >= 12
                && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return true;
        }
        return false;
    }
}
