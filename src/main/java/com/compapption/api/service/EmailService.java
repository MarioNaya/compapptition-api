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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@compapptition.com}")
    private String fromEmail;

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
