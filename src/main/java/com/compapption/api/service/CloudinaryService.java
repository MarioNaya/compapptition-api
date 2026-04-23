package com.compapption.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.InternalStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Servicio de subida y borrado de imágenes contra Cloudinary.
 *
 * <p>Encapsula el flujo de signed upload: validación de tipo/tamaño, firma con credenciales
 * de servidor y devolución de la URL segura (HTTPS) resultante, que es la que se persistirá
 * en los campos {@code escudoUrl}, {@code fotoUrl} o {@code iconoUrl} de las entidades.</p>
 *
 * <p>Política de validación:
 * <ul>
 *   <li>El fichero no puede ser nulo ni estar vacío.</li>
 *   <li>Solo se aceptan content-types {@code image/jpeg}, {@code image/png},
 *       {@code image/webp} e {@code image/gif}.</li>
 *   <li>Tamaño máximo 5 MB (coherente con {@code spring.servlet.multipart.max-file-size}).</li>
 * </ul>
 *
 * @author Mario
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    /** Tamaño máximo permitido por upload: 5 MB en bytes. */
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    /** Content-types aceptados (whitelist). */
    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final Cloudinary cloudinary;

    @Value("${cloudinary.upload-folder}")
    private String folder;

    /**
     * Sube una imagen a Cloudinary y devuelve su URL segura.
     *
     * <p>El destino final es {@code {cloudinary.upload-folder}/{folderSuffix}} dentro del
     * bucket del cloud configurado. Cloudinary genera un nombre único y no sobreescribe
     * ficheros existentes.</p>
     *
     * @param file fichero multipart recibido del frontend (no puede ser nulo ni vacío)
     * @param folderSuffix subcarpeta lógica (p. ej. {@code "escudos"}, {@code "fotos"})
     * @return URL HTTPS pública y cacheable generada por Cloudinary
     * @throws BadRequestException si el fichero es inválido (vacío, tipo no permitido, demasiado grande)
     * @throws InternalStateException si Cloudinary devuelve un error de IO durante el upload
     */
    public String upload(MultipartFile file, String folderSuffix) {
        validar(file);

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder + "/" + folderSuffix,
                            "resource_type", "image",
                            "unique_filename", true,
                            "overwrite", false
                    )
            );
            return (String) resultado.get("secure_url");
        } catch (IOException ex) {
            log.error("Error subiendo imagen a Cloudinary (folder={})", folderSuffix, ex);
            throw new InternalStateException("No se pudo subir la imagen");
        }
    }

    /**
     * Elimina una imagen de Cloudinary a partir de su {@code public_id}.
     *
     * <p>Método utilitario todavía no expuesto vía controller; se reserva para cuando
     * se implemente el borrado/rotación de imágenes asociadas a entidades.</p>
     *
     * @param publicId identificador público devuelto por Cloudinary en el upload
     * @throws InternalStateException si Cloudinary devuelve un error de IO durante el borrado
     */
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException ex) {
            log.error("Error eliminando imagen de Cloudinary (publicId={})", publicId, ex);
            throw new InternalStateException("No se pudo eliminar la imagen");
        }
    }

    /**
     * Aplica las validaciones de presencia, tipo y tamaño al fichero recibido.
     *
     * @param file fichero a validar
     * @throws BadRequestException si falla cualquiera de las validaciones
     */
    private void validar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo no puede estar vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "Tipo de archivo no permitido. Permitidos: jpg, png, webp, gif");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("El archivo excede el tamaño máximo permitido (5 MB)");
        }
    }
}
