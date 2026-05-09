package com.compapptition.api.service;

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

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:no-reply@compapptition.com}")
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
        String subject = "Recuperación de contraseña - Compapptition";
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

        String content = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="color-scheme" content="light dark">
                <meta name="supported-color-schemes" content="light dark">
                <title>Recuperación de contraseña - Compapptition</title>
                <style>
                  body { margin: 0; padding: 0; }
                  a { text-decoration: none; }
                  @media (prefers-color-scheme: dark) {
                    .bg-page    { background-color: #0a0808 !important; }
                    .bg-card    { background-color: #120f0d !important; }
                    .bg-inset   { background-color: #1a1513 !important; }
                    .border-soft{ border-color: #2a211c !important; }
                    .text-main  { color: #f4ede6 !important; }
                    .text-mute  { color: #b5a89c !important; }
                    .text-faint { color: #6e625a !important; }
                    .divider    { background-color: #2a211c !important; }
                  }
                </style>
                </head>
                <body class="bg-page" style="margin:0; padding:0; background-color:#F6F3EE; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">

                <div style="display:none; max-height:0; overflow:hidden; mso-hide:all; font-size:1px; line-height:1px; color:#F6F3EE; opacity:0;">
                  Hemos recibido una solicitud para restablecer tu contraseña. El enlace expira en 24 horas.
                </div>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="bg-page" style="background-color:#F6F3EE;">
                  <tr>
                    <td align="center" style="padding:32px 16px;">
                      <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0" style="width:100%%; max-width:600px;">
                        <tr>
                          <td align="left" style="padding:0 8px 24px 8px;">
                            <span style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:18px; font-weight:800; letter-spacing:0.5px; color:#1A1411;" class="text-main">
                              COMP<span style="color:#FF7A1A;">APP</span>TITION
                            </span>
                          </td>
                        </tr>
                        <tr>
                          <td class="bg-card border-soft" style="background-color:#FFFFFF; border:1px solid #E8E2DA; border-radius:14px; padding:40px 40px 32px 40px;">
                            <p style="margin:0 0 14px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:11px; font-weight:700; letter-spacing:1.5px; color:#FF7A1A; text-transform:uppercase;">
                              Seguridad de la cuenta
                            </p>
                            <h1 style="margin:0 0 16px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:28px; line-height:1.2; font-weight:800; letter-spacing:-0.3px; color:#1A1411;" class="text-main">
                              Restablece tu contraseña
                            </h1>
                            <p style="margin:0 0 14px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:16px; line-height:1.55; color:#3A322B;" class="text-main">
                              Hola, %s.
                            </p>
                            <p style="margin:0 0 28px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:16px; line-height:1.55; color:#564B42;" class="text-mute">
                              Hemos recibido una solicitud para restablecer la contraseña de tu cuenta de Compapptition. Pulsa el botón de abajo para crear una nueva.
                            </p>
                            <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 28px 0;">
                              <tr>
                                <td align="center" bgcolor="#FF7A1A" style="border-radius:10px; background-color:#FF7A1A;">
                                  <a href="%s" target="_blank"
                                     style="display:inline-block; padding:16px 28px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:15px; font-weight:700; letter-spacing:0.3px; color:#FFFFFF; text-decoration:none; border-radius:10px;">
                                    Restablecer contraseña →
                                  </a>
                                </td>
                              </tr>
                            </table>
                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="bg-inset border-soft" style="background-color:#FFF6EE; border:1px solid #FFE4CC; border-radius:10px;">
                              <tr>
                                <td style="padding:14px 16px;">
                                  <p style="margin:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:13px; line-height:1.5; color:#7A4A1A;" class="text-main">
                                    <span style="font-weight:700; color:#FF7A1A;">⧗ Expira en 24h.</span>
                                    Pasado ese tiempo tendrás que solicitar un nuevo enlace.
                                  </p>
                                </td>
                              </tr>
                            </table>
                            <p style="margin:24px 0 6px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:12px; line-height:1.5; color:#7A6E62;" class="text-mute">
                              ¿El botón no funciona? Copia y pega esta URL en tu navegador:
                            </p>
                            <p style="margin:0 0 28px 0; font-family:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace; font-size:12px; line-height:1.5; color:#1A1411; word-break:break-all;" class="text-main">
                              %s
                            </p>
                            <div class="divider" style="height:1px; line-height:1px; font-size:1px; background-color:#E8E2DA; margin:0 0 20px 0;">&nbsp;</div>
                            <p style="margin:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:13px; line-height:1.55; color:#7A6E62;" class="text-mute">
                              Si no fuiste tú quien solicitó este cambio, ignora este email. Tu contraseña actual seguirá funcionando.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td align="center" style="padding:24px 16px 8px 16px;">
                            <p style="margin:0 0 6px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:12px; line-height:1.55; color:#7A6E62;" class="text-mute">
                              © 2026 Compapptition. Email automático, no respondas a este mensaje.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
                </body>
                </html>
                """.formatted(nombre != null ? nombre : "Usuario", resetUrl, resetUrl);

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
        String subject = "Invitación a " + competicionNombre + " - Compapptition";
        String acceptUrl = frontendUrl + "/app/invitations?accept=" + token;

        String content = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="color-scheme" content="light dark">
                <meta name="supported-color-schemes" content="light dark">
                <title>Invitación a competición - Compapptition</title>
                <style>
                  body { margin: 0; padding: 0; }
                  a { text-decoration: none; }
                  @media (prefers-color-scheme: dark) {
                    .bg-page    { background-color: #0a0808 !important; }
                    .bg-card    { background-color: #120f0d !important; }
                    .bg-inset   { background-color: #1a1513 !important; }
                    .border-soft{ border-color: #2a211c !important; }
                    .text-main  { color: #f4ede6 !important; }
                    .text-mute  { color: #b5a89c !important; }
                    .text-faint { color: #6e625a !important; }
                    .divider    { background-color: #2a211c !important; }
                    .role-chip-bg { background-color: #1a1513 !important; border-color:#2a211c !important; }
                  }
                </style>
                </head>
                <body class="bg-page" style="margin:0; padding:0; background-color:#F6F3EE; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">

                <div style="display:none; max-height:0; overflow:hidden; mso-hide:all; font-size:1px; line-height:1px; color:#F6F3EE; opacity:0;">
                  Has recibido una nueva invitación a una competición. La invitación expira en 7 días.
                </div>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="bg-page" style="background-color:#F6F3EE;">
                  <tr>
                    <td align="center" style="padding:32px 16px;">
                      <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0" style="width:100%%; max-width:600px;">
                        <tr>
                          <td align="left" style="padding:0 8px 24px 8px;">
                            <span style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:18px; font-weight:800; letter-spacing:0.5px; color:#1A1411;" class="text-main">
                              COMP<span style="color:#FF7A1A;">APP</span>TITION
                            </span>
                          </td>
                        </tr>
                        <tr>
                          <td class="bg-card border-soft" style="background-color:#FFFFFF; border:1px solid #E8E2DA; border-radius:14px; padding:40px 40px 32px 40px;">
                            <p style="margin:0 0 14px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:11px; font-weight:700; letter-spacing:1.5px; color:#FF7A1A; text-transform:uppercase;">
                              Nueva invitación
                            </p>
                            <h1 style="margin:0 0 20px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:28px; line-height:1.2; font-weight:800; letter-spacing:-0.3px; color:#1A1411;" class="text-main">
                              Te han invitado a una competición
                            </h1>
                            <p style="margin:0 0 14px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:16px; line-height:1.55; color:#3A322B;" class="text-main">
                              Hola,
                            </p>
                            <p style="margin:0 0 24px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:16px; line-height:1.55; color:#564B42;" class="text-mute">
                              <strong style="color:#1A1411; font-weight:700;" class="text-main">%s</strong> te ha invitado a unirte a
                              <strong style="color:#1A1411; font-weight:700;" class="text-main">%s</strong>.
                            </p>
                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="bg-inset border-soft role-chip-bg" style="background-color:#FAF6F1; border:1px solid #E8E2DA; border-radius:10px; margin:0 0 28px 0;">
                              <tr>
                                <td style="padding:18px 20px;">
                                  <p style="margin:0 0 4px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:10px; font-weight:700; letter-spacing:1.5px; color:#9A8E82; text-transform:uppercase;" class="text-faint">
                                    Competición
                                  </p>
                                  <p style="margin:0 0 14px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:18px; font-weight:700; color:#1A1411;" class="text-main">
                                    %s
                                  </p>
                                  <p style="margin:0 0 4px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:10px; font-weight:700; letter-spacing:1.5px; color:#9A8E82; text-transform:uppercase;" class="text-faint">
                                    Rol asignado
                                  </p>
                                  <p style="margin:0;">
                                    <span style="display:inline-block; padding:6px 12px; background-color:#FFF1E2; border:1px solid #FFD3A8; border-radius:6px; font-family:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace; font-size:12px; font-weight:700; letter-spacing:0.5px; color:#B8530F;">
                                      %s
                                    </span>
                                  </p>
                                </td>
                              </tr>
                            </table>
                            <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin:0 0 16px 0;">
                              <tr>
                                <td align="center" bgcolor="#FF7A1A" style="border-radius:10px; background-color:#FF7A1A;">
                                  <a href="%s" target="_blank"
                                     style="display:inline-block; padding:16px 28px; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:15px; font-weight:700; letter-spacing:0.3px; color:#FFFFFF; text-decoration:none; border-radius:10px;">
                                    Aceptar invitación →
                                  </a>
                                </td>
                              </tr>
                            </table>
                            <p style="margin:0 0 28px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:13px; line-height:1.5; color:#7A6E62;" class="text-mute">
                              <span style="color:#FF7A1A; font-weight:700;">⧗</span> Esta invitación expira en <strong style="color:#1A1411;" class="text-main">7 días</strong>.
                            </p>
                            <p style="margin:0 0 6px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:12px; line-height:1.5; color:#7A6E62;" class="text-mute">
                              ¿El botón no funciona? Copia y pega esta URL:
                            </p>
                            <p style="margin:0 0 24px 0; font-family:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace; font-size:12px; line-height:1.5; color:#1A1411; word-break:break-all;" class="text-main">
                              %s
                            </p>
                            <div class="divider" style="height:1px; line-height:1px; font-size:1px; background-color:#E8E2DA; margin:0 0 20px 0;">&nbsp;</div>
                            <p style="margin:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:13px; line-height:1.55; color:#7A6E62;" class="text-mute">
                              Si no esperabas esta invitación, puedes ignorar este email sin problema.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td align="center" style="padding:24px 16px 8px 16px;">
                            <p style="margin:0 0 6px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:12px; line-height:1.55; color:#7A6E62;" class="text-mute">
                              © 2026 Compapptition. Email automático, no respondas a este mensaje.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
                </body>
                </html>
                """.formatted(emisorNombre, competicionNombre, competicionNombre, rolOfrecido, acceptUrl, acceptUrl);

        enviarEmail(to, subject, content);
    }

    /**
     * Envía de forma asíncrona un email HTML con la notificación de un próximo
     * partido. Lo invoca {@code EventoService.notificarPartido} tanto desde el
     * scheduler automático (24h antes) como desde el endpoint manual (cambios
     * de fecha). El destinatario es el email del usuario asociado al jugador
     * inscrito en uno de los dos equipos del evento.
     *
     * @param to            email del destinatario (jugador)
     * @param nombreEquipo  nombre del equipo del destinatario (local o visitante según corresponda)
     * @param nombreRival   nombre del equipo rival
     * @param fecha         fecha y hora del partido formateada (p.ej. "Sáb 29 Ago, 19:00")
     * @param lugar         lugar del encuentro (texto libre)
     */
    @Async
    public void enviarNotificacionPartido(String to,
                                          String nombreEquipo,
                                          String nombreRival,
                                          String fecha,
                                          String lugar) {
        String subject = "Próximo partido: " + nombreEquipo + " vs " + nombreRival + " - Compapptition";

        String content = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="color-scheme" content="light dark">
                <meta name="supported-color-schemes" content="light dark">
                <title>Próximo partido - Compapptition</title>
                <style>
                  body { margin: 0; padding: 0; }
                  a { text-decoration: none; }
                  @media (prefers-color-scheme: dark) {
                    .bg-page    { background-color: #0a0808 !important; }
                    .bg-card    { background-color: #120f0d !important; }
                    .bg-arena   { background-color: #0a0808 !important; border-color:#2a211c !important; }
                    .border-soft{ border-color: #2a211c !important; }
                    .text-main  { color: #f4ede6 !important; }
                    .text-mute  { color: #b5a89c !important; }
                    .text-faint { color: #6e625a !important; }
                    .divider    { background-color: #2a211c !important; }
                    .meta-cell  { background-color: #1a1513 !important; border-color:#2a211c !important; }
                  }
                </style>
                </head>
                <body class="bg-page" style="margin:0; padding:0; background-color:#F6F3EE; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">

                <div style="display:none; max-height:0; overflow:hidden; mso-hide:all; font-size:1px; line-height:1px; color:#F6F3EE; opacity:0;">
                  Tu próximo partido se acerca. Prepárate para saltar al campo.
                </div>

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="bg-page" style="background-color:#F6F3EE;">
                  <tr>
                    <td align="center" style="padding:32px 16px;">
                      <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0" style="width:100%%; max-width:600px;">
                        <tr>
                          <td align="left" style="padding:0 8px 24px 8px;">
                            <span style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:18px; font-weight:800; letter-spacing:0.5px; color:#1A1411;" class="text-main">
                              COMP<span style="color:#FF7A1A;">APP</span>TITION
                            </span>
                          </td>
                        </tr>
                        <tr>
                          <td class="bg-card border-soft" style="background-color:#FFFFFF; border:1px solid #E8E2DA; border-radius:14px; padding:40px 32px 32px 32px;">
                            <p style="margin:0 0 12px 0; text-align:center; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:11px; font-weight:700; letter-spacing:1.5px; color:#FF7A1A; text-transform:uppercase;">
                              ◉ Próximo partido
                            </p>
                            <h1 style="margin:0 0 8px 0; text-align:center; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:24px; line-height:1.25; font-weight:800; letter-spacing:-0.3px; color:#1A1411;" class="text-main">
                              Tu próximo partido está cerca
                            </h1>
                            <p style="margin:0 0 28px 0; text-align:center; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:15px; line-height:1.5; color:#564B42;" class="text-mute">
                              Prepara la equipación, calienta y sal a por los tres puntos.
                            </p>
                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="bg-arena" style="background-color:#120f0d; border:1px solid #2a211c; border-radius:12px;">
                              <tr>
                                <td style="padding:32px 16px;">
                                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                      <td width="42%%" align="center" valign="middle" style="padding:0 8px;">
                                        <p style="margin:0 0 8px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:10px; font-weight:700; letter-spacing:1.5px; color:#b5a89c; text-transform:uppercase;">
                                          Local
                                        </p>
                                        <p style="margin:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:22px; line-height:1.15; font-weight:800; letter-spacing:-0.3px; color:#f4ede6;">
                                          %s
                                        </p>
                                      </td>
                                      <td width="16%%" align="center" valign="middle" style="padding:0 4px;">
                                        <span style="display:inline-block; font-family:ui-monospace,SFMono-Regular,Menlo,Consolas,monospace; font-size:13px; font-weight:700; letter-spacing:1px; color:#FF7A1A; padding:8px 12px; border:1px solid #2a211c; border-radius:999px;">
                                          VS
                                        </span>
                                      </td>
                                      <td width="42%%" align="center" valign="middle" style="padding:0 8px;">
                                        <p style="margin:0 0 8px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:10px; font-weight:700; letter-spacing:1.5px; color:#b5a89c; text-transform:uppercase;">
                                          Visitante
                                        </p>
                                        <p style="margin:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:22px; line-height:1.15; font-weight:800; letter-spacing:-0.3px; color:#f4ede6;">
                                          %s
                                        </p>
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>
                            </table>
                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-top:20px;">
                              <tr>
                                <td width="50%%" valign="top" style="padding-right:6px;">
                                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="meta-cell border-soft" style="background-color:#FAF6F1; border:1px solid #E8E2DA; border-radius:10px;">
                                    <tr>
                                      <td style="padding:14px 16px;">
                                        <p style="margin:0 0 4px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:10px; font-weight:700; letter-spacing:1.5px; color:#9A8E82; text-transform:uppercase;" class="text-faint">
                                          Fecha
                                        </p>
                                        <p style="margin:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:15px; font-weight:700; color:#1A1411;" class="text-main">
                                          %s
                                        </p>
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                                <td width="50%%" valign="top" style="padding-left:6px;">
                                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" class="meta-cell border-soft" style="background-color:#FAF6F1; border:1px solid #E8E2DA; border-radius:10px;">
                                    <tr>
                                      <td style="padding:14px 16px;">
                                        <p style="margin:0 0 4px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:10px; font-weight:700; letter-spacing:1.5px; color:#9A8E82; text-transform:uppercase;" class="text-faint">
                                          Lugar
                                        </p>
                                        <p style="margin:0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:15px; font-weight:700; color:#1A1411;" class="text-main">
                                          %s
                                        </p>
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>
                            </table>
                            <div class="divider" style="height:1px; line-height:1px; font-size:1px; background-color:#E8E2DA; margin:24px 0 16px 0;">&nbsp;</div>
                            <p style="margin:0; text-align:center; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:13px; line-height:1.55; color:#7A6E62;" class="text-mute">
                              Consulta la alineación y el estado del partido en tu panel de Compapptition.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td align="center" style="padding:24px 16px 8px 16px;">
                            <p style="margin:0 0 6px 0; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; font-size:12px; line-height:1.55; color:#7A6E62;" class="text-mute">
                              © 2026 Compapptition. Email automático, no respondas a este mensaje.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
                </body>
                </html>
                """.formatted(nombreEquipo, nombreRival, fecha, lugar);

        enviarEmail(to, subject, content);
    }

    /**
     * Envía un email simple al admin de sistema notificando que se ha
     * abierto un nuevo ticket de soporte. La plantilla es deliberadamente
     * sencilla: este email es interno (admin → admin), no externo, y no
     * necesita el branding completo de los emails de usuarios.
     *
     * @param adminEmail      destinatario (admin de sistema)
     * @param ticketId        identificador del ticket creado
     * @param ticketAsunto    asunto del ticket
     * @param ticketDescripcion descripción libre del ticket
     * @param autorUsername   username del autor
     * @param autorEmail      email del autor (para responder fuera de la app si fuera necesario)
     */
    @Async
    public void enviarNotificacionAdminTicket(String adminEmail,
                                              Long ticketId,
                                              String ticketAsunto,
                                              String ticketDescripcion,
                                              String autorUsername,
                                              String autorEmail) {
        String subject = "[Compapptition] Nuevo ticket #" + ticketId + " — " + ticketAsunto;
        String adminUrl = frontendUrl + "/app/tickets/" + ticketId;
        String descripcionEscaped = escapeHtml(ticketDescripcion);

        String content = """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"></head>
                <body style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#F6F3EE;padding:24px;color:#1A1411;">
                  <div style="max-width:600px;margin:0 auto;background:#FFFFFF;border:1px solid #E8E2DA;border-radius:12px;padding:28px;">
                    <p style="margin:0 0 4px 0;font-size:11px;font-weight:700;letter-spacing:1.5px;color:#FF7A1A;text-transform:uppercase;">
                      Nuevo ticket de soporte
                    </p>
                    <h1 style="margin:0 0 16px 0;font-size:22px;font-weight:800;color:#1A1411;">
                      #%d — %s
                    </h1>
                    <p style="margin:0 0 4px 0;font-size:11px;font-weight:700;letter-spacing:1px;color:#9A8E82;text-transform:uppercase;">Autor</p>
                    <p style="margin:0 0 16px 0;font-size:14px;color:#1A1411;">
                      %s · <a href="mailto:%s" style="color:#FF7A1A;">%s</a>
                    </p>
                    <p style="margin:0 0 4px 0;font-size:11px;font-weight:700;letter-spacing:1px;color:#9A8E82;text-transform:uppercase;">Descripción</p>
                    <div style="background:#FAF6F1;border:1px solid #E8E2DA;border-radius:8px;padding:14px;font-size:14px;line-height:1.5;color:#3A322B;white-space:pre-wrap;">
                      %s
                    </div>
                    <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin:24px 0 4px 0;">
                      <tr>
                        <td bgcolor="#FF7A1A" style="border-radius:8px;background-color:#FF7A1A;">
                          <a href="%s" style="display:inline-block;padding:12px 22px;font-size:14px;font-weight:700;color:#FFFFFF;text-decoration:none;border-radius:8px;">
                            Ver y responder en la app →
                          </a>
                        </td>
                      </tr>
                    </table>
                    <p style="margin:16px 0 0 0;font-size:12px;color:#7A6E62;">
                      Email automático del sistema de soporte. Puedes responder al ticket desde el panel admin de Compapptition.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(ticketId, escapeHtml(ticketAsunto), escapeHtml(autorUsername),
                              autorEmail != null ? autorEmail : "",
                              autorEmail != null ? escapeHtml(autorEmail) : "—",
                              descripcionEscaped, adminUrl);

        enviarEmail(adminEmail, subject, content);
    }

    /**
     * Escapado mínimo HTML para que la descripción libre del ticket no
     * pueda inyectar marcado al renderizarse en el email del admin.
     */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void enviarEmail(String to, String subject, String htmlContent) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email enviado a: {}", to);
        } catch (MessagingException e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
        }
    }
}
