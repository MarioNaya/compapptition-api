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
 * <p>Encapsula el flujo de upload server-side autenticado: validación de tipo/tamaño,
 * envío de los bytes a Cloudinary mediante el SDK con credenciales de servidor, y
 * devolución de la URL segura (HTTPS) resultante, que es la que se persistirá en los
 * campos {@code escudoUrl}, {@code fotoUrl} o {@code iconoUrl} de las entidades.</p>
 *
 * <p>Política de validación, en orden:
 * <ol>
 *   <li>El fichero no puede ser nulo ni estar vacío.</li>
 *   <li>El content-type declarado debe estar en la whitelist
 *       ({@code image/jpeg}, {@code image/png}, {@code image/webp}, {@code image/gif}).</li>
 *   <li>El tamaño no puede exceder 5 MB
 *       (coherente con {@code spring.servlet.multipart.max-file-size}).</li>
 *   <li>La firma binaria del archivo (<em>magic bytes</em>) debe coincidir con uno de los
 *       formatos permitidos. Cierra el flanco de un cliente que mande un PHP
 *       (o cualquier otro binario ejecutable) renombrado a .jpg con
 *       {@code Content-Type: image/jpeg} forzado.</li>
 * </ol>
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

    /** Número de bytes a leer del inicio del fichero para inspeccionar la firma. */
    private static final int MAGIC_BYTES_LENGTH = 12;

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
     * Aplica las validaciones de presencia, tipo declarado, tamaño y firma binaria
     * al fichero recibido.
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

        validarMagicBytes(file);
    }

    /**
     * Comprueba que la firma binaria de los primeros bytes del fichero corresponde
     * a uno de los formatos permitidos (JPEG, PNG, WebP, GIF).
     *
     * <p>Esta validación es independiente del {@code Content-Type} declarado por el
     * cliente: aunque un atacante mande {@code Content-Type: image/jpeg} en la
     * cabecera HTTP, si los bytes reales no son JPEG/PNG/WebP/GIF se rechaza el
     * upload. Cierra el vector clásico "PHP renombrado a .jpg".</p>
     *
     * <p>Firmas comprobadas:
     * <ul>
     *   <li>JPEG: {@code FF D8 FF}</li>
     *   <li>PNG: {@code 89 50 4E 47 0D 0A 1A 0A}</li>
     *   <li>WebP: {@code 52 49 46 46 ?? ?? ?? ?? 57 45 42 50} ("RIFF....WEBP")</li>
     *   <li>GIF: {@code 47 49 46 38} ("GIF8", cubre GIF87a y GIF89a)</li>
     * </ul>
     *
     * @param file fichero a inspeccionar
     * @throws BadRequestException si la firma no coincide con ningún formato permitido
     *                             o si el fichero es más corto que la firma mínima
     */
    private void validarMagicBytes(MultipartFile file) {
        byte[] head = new byte[MAGIC_BYTES_LENGTH];
        try (var in = file.getInputStream()) {
            int leidos = in.readNBytes(head, 0, MAGIC_BYTES_LENGTH);
            if (leidos < 4) {
                throw new BadRequestException(
                        "El archivo es demasiado corto para ser una imagen válida");
            }
        } catch (IOException ex) {
            log.warn("Error leyendo cabecera del fichero para validación de magic bytes", ex);
            throw new BadRequestException("No se pudo leer el contenido del archivo");
        }

        if (esJpeg(head) || esPng(head) || esGif(head) || esWebp(head)) {
            return;
        }

        throw new BadRequestException(
                "El contenido del archivo no coincide con un formato de imagen permitido");
    }

    /** JPEG: FF D8 FF. */
    private boolean esJpeg(byte[] h) {
        return h[0] == (byte) 0xFF && h[1] == (byte) 0xD8 && h[2] == (byte) 0xFF;
    }

    /** PNG: 89 50 4E 47 0D 0A 1A 0A. */
    private boolean esPng(byte[] h) {
        return h[0] == (byte) 0x89 && h[1] == (byte) 0x50 && h[2] == (byte) 0x4E
                && h[3] == (byte) 0x47 && h[4] == (byte) 0x0D && h[5] == (byte) 0x0A
                && h[6] == (byte) 0x1A && h[7] == (byte) 0x0A;
    }

    /** GIF: 47 49 46 38 ("GIF8"). */
    private boolean esGif(byte[] h) {
        return h[0] == (byte) 0x47 && h[1] == (byte) 0x49 && h[2] == (byte) 0x46
                && h[3] == (byte) 0x38;
    }

    /** WebP: "RIFF" en bytes 0..3 y "WEBP" en bytes 8..11. */
    private boolean esWebp(byte[] h) {
        return h[0] == (byte) 0x52 && h[1] == (byte) 0x49 && h[2] == (byte) 0x46
                && h[3] == (byte) 0x46
                && h[8] == (byte) 0x57 && h[9] == (byte) 0x45 && h[10] == (byte) 0x42
                && h[11] == (byte) 0x50;
    }
}
