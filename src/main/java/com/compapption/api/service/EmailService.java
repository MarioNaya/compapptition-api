package com.compapption.api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio de envío de correos electrónicos transaccionales.
 * <p>
 * Proporciona métodos {@code @Async} para enviar emails de recuperación de contraseña
 * e invitaciones a competiciones. Los mensajes se componen en formato HTML y se
 * despachan mediante {@link JavaMailSender}. Los errores de envío se capturan y
 * registran sin propagar la excepción al hilo llamador.
 * </p>
 *
 * @author Mario
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@compapptition.com}")
    private String fromEmail;

    /**
     * Envía de forma asíncrona un email HTML con el enlace de restablecimiento de contraseña.
     * <p>
     * El enlace generado apunta a {@code {frontendUrl}/auth/reset-password?token=...}
     * y es válido durante 24 horas.
     * </p>
     *
     * @param to     dirección de correo del destinatario
     * @param nombre nombre del usuario para personalizar el saludo (puede ser {@code null})
     * @param token  token de recuperación de contraseña generado previamente
     */
    @Async
    public void enviarEmailRecuperacion(String to, String nombre, String token) {
        String subject = "Recuperación de contraseña - CompAPPtition";
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

        String content = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #1976d2; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f5f5f5; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #1976d2;
                                  color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Recuperación de Contraseña</h1>
                        </div>
                        <div class="content">
                            <p>Hola %s,</p>
                            <p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.</p>
                            <p>Haz clic en el siguiente botón para crear una nueva contraseña:</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">Restablecer Contraseña</a>
                            </p>
                            <p>Si no solicitaste este cambio, puedes ignorar este email.</p>
                            <p>Este enlace expirará en 24 horas.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un email automático, por favor no respondas.</p>
                            <p>© 2024 Competiciones Deportivas</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombre != null ? nombre : "Usuario", resetUrl);

        enviarEmail(to, subject, content);
    }

    /**
     * Envía de forma asíncrona un email HTML con la invitación a una competición.
     * <p>
     * El email incluye el nombre del emisor, la competición de destino, el rol
     * ofrecido y un botón de aceptación que apunta a
     * {@code {frontendUrl}/invitaciones/aceptar?token=...}. La invitación caduca en 7 días.
     * </p>
     *
     * @param to               dirección de correo del destinatario
     * @param emisorNombre     nombre del usuario que envía la invitación
     * @param competicionNombre nombre de la competición (o del equipo/contexto) al que se invita
     * @param rolOfrecido      rol que se ofrece al destinatario (p.ej. {@code JUGADOR})
     * @param token            token UUID único de la invitación
     */
    @Async
    public void enviarInvitacion(String to,
                                 String emisorNombre,
                                 String competicionNombre,
                                 String rolOfrecido,
                                 String token) {
        String subject = "Invitación a " + competicionNombre + " - CompAPPtition -";
        String acceptUrl = frontendUrl + "/invitaciones/aceptar?token=" + token;

        String content = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4caf50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f5f5f5; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #4caf50;
                                  color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .info-box { background-color: white; padding: 15px; border-radius: 4px; margin: 15px 0; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Has sido invitado!</h1>
                        </div>
                        <div class="content">
                            <p>Hola,</p>
                            <p><strong>%s</strong> te ha invitado a participar en:</p>
                            <div class="info-box">
                                <p><strong>Competición:</strong> %s</p>
                                <p><strong>Rol:</strong> %s</p>
                            </div>
                            <p>Haz clic en el siguiente botón para aceptar la invitación:</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">Aceptar Invitación</a>
                            </p>
                            <p>Esta invitación expirará en 7 días.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un email automático, por favor no respondas.</p>
                            <p>© 2024 Competiciones Deportivas</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(emisorNombre, competicionNombre, rolOfrecido, acceptUrl);

        enviarEmail(to, subject, content);
    }

    @Async
    private void enviarNotificacionPartido(String to,
                                           String nombreEquipo,
                                           String nombreRival,
                                           String fecha,
                                           String lugar) {
        String subject = "Próximo partido: " + nombreEquipo + " VS " + nombreRival;

        String content = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #ff9800; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f5f5f5; }
                        .match-box { background-color: white; padding: 20px; border-radius: 4px;
                                     margin: 15px 0; text-align: center; }
                        .vs { font-size: 24px; color: #ff9800; margin: 10px 0; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🏆 Próximo Partido</h1>
                        </div>
                        <div class="content">
                            <div class="match-box">
                                <h2>%s</h2>
                                <div class="vs">VS</div>
                                <h2>%s</h2>
                                <hr>
                                <p><strong>📅 Fecha:</strong> %s</p>
                                <p><strong>📍 Lugar:</strong> %s</p>
                            </div>
                            <p>¡No faltes! Tu equipo te necesita.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un email automático, por favor no respondas.</p>
                            <p>© 2024 Competiciones Deportivas</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombreEquipo, nombreRival, fecha, lugar);

        enviarEmail(to, subject, content);
    }
    private void enviarEmail(String to, String subject, String htmlContent) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent);

            mailSender.send(message);
            log.info("Email enviado a: {}", to);
        } catch (MessagingException e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
        }
    }
}
