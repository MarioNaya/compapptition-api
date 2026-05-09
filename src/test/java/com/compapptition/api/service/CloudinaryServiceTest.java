package com.compapptition.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.compapptition.api.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryService(cloudinary);
        // Inyección manual de la propiedad @Value ya que Mockito no la resuelve
        ReflectionTestUtils.setField(cloudinaryService, "folder", "compapptition-test");
    }

    // =========================================================
    // upload() — validaciones
    // =========================================================

    @Test
    void upload_archivoVacio_lanzaBadRequest() {
        MultipartFile vacio = new MockMultipartFile(
                "file", "escudo.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> cloudinaryService.upload(vacio, "escudos"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    void upload_contentTypeInvalido_lanzaBadRequest() {
        MultipartFile pdf = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> cloudinaryService.upload(pdf, "escudos"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Tipo de archivo no permitido");
    }

    @Test
    void upload_archivoDemasiadoGrande_lanzaBadRequest() {
        // 6 MB > límite de 5 MB
        byte[] contenido = new byte[6 * 1024 * 1024];
        MultipartFile grande = new MockMultipartFile(
                "file", "grande.jpg", "image/jpeg", contenido);

        assertThatThrownBy(() -> cloudinaryService.upload(grande, "escudos"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tamaño máximo");
    }

    @Test
    void upload_contentTypeSpoofeadoConBytesNoImagen_lanzaBadRequest() {
        // Vector clásico que se cierra con magic bytes: el atacante envía un payload
        // arbitrario (típicamente una webshell con extensión .jpg) y fuerza el
        // Content-Type a "image/jpeg" en la cabecera HTTP. La whitelist de
        // Content-Type por sí sola lo aceptaría; la validación de magic bytes lo
        // rechaza porque la firma binaria no coincide con ninguno de los formatos
        // permitidos. Aquí usamos un payload ASCII inocuo (no se incluye un payload
        // real de webshell para evitar falsos positivos del antivirus en el repo).
        byte[] payloadNoImagen = "NOT_AN_IMAGE_PAYLOAD_FOR_TESTING".getBytes();
        MultipartFile spoofeado = new MockMultipartFile(
                "file", "documento.jpg", "image/jpeg", payloadNoImagen);

        assertThatThrownBy(() -> cloudinaryService.upload(spoofeado, "escudos"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no coincide con un formato de imagen permitido");
    }

    // =========================================================
    // upload() — flujo feliz
    // =========================================================

    @Test
    void upload_flujoFeliz_devuelveSecureUrl() throws Exception {
        // Cabecera PNG válida: 89 50 4E 47 0D 0A 1A 0A + relleno
        byte[] pngValido = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
        };
        MultipartFile imagen = new MockMultipartFile(
                "file", "escudo.png", "image/png", pngValido);

        String urlEsperada = "https://res.cloudinary.com/compapptition/image/upload/v1/compapptition-test/escudos/abc.png";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", urlEsperada, "public_id", "compapptition-test/escudos/abc"));

        String resultado = cloudinaryService.upload(imagen, "escudos");

        assertThat(resultado).isEqualTo(urlEsperada);
    }
}
