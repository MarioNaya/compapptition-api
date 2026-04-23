package com.compapption.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.compapption.api.exception.BadRequestException;
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
        ReflectionTestUtils.setField(cloudinaryService, "folder", "compapption-test");
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

    // =========================================================
    // upload() — flujo feliz
    // =========================================================

    @Test
    void upload_flujoFeliz_devuelveSecureUrl() throws Exception {
        MultipartFile imagen = new MockMultipartFile(
                "file", "escudo.png", "image/png", new byte[]{1, 2, 3, 4, 5});

        String urlEsperada = "https://res.cloudinary.com/compapption/image/upload/v1/compapption-test/escudos/abc.png";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", urlEsperada, "public_id", "compapption-test/escudos/abc"));

        String resultado = cloudinaryService.upload(imagen, "escudos");

        assertThat(resultado).isEqualTo(urlEsperada);
    }
}
