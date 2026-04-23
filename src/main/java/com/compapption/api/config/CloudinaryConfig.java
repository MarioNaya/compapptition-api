package com.compapption.api.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del cliente Cloudinary para el upload firmado de imágenes.
 *
 * <p>Expone un bean {@link Cloudinary} construido a partir de las propiedades
 * {@code cloudinary.cloud-name}, {@code cloudinary.api-key} y
 * {@code cloudinary.api-secret}, que a su vez se alimentan de las variables de
 * entorno {@code CLOUDINARY_CLOUD_NAME}, {@code CLOUDINARY_API_KEY} y
 * {@code CLOUDINARY_API_SECRET} (vía {@code .env}).</p>
 *
 * <p>El cliente se configura con {@code secure=true} para usar siempre URLs HTTPS.</p>
 *
 * @author Mario
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    /**
     * Construye el cliente Cloudinary a partir de las credenciales configuradas.
     *
     * @return instancia de {@link Cloudinary} con URLs seguras (HTTPS)
     */
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
