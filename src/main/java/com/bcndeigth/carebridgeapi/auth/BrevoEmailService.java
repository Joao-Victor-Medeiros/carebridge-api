package com.bcndeigth.carebridgeapi.auth;

import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class BrevoEmailService {
    private final JavaMailSender mailSender;
    private final String senderEmail;
    private final String senderName;
    private final boolean enabled;
    private final String smtpUsername;
    private final String smtpKey;

    public BrevoEmailService(
            JavaMailSender mailSender,
            @Value("${app.brevo.sender-email:}") String senderEmail,
            @Value("${app.brevo.sender-name:CareBridge}") String senderName,
            @Value("${app.brevo.enabled:false}") boolean enabled,
            @Value("${spring.mail.username:}") String smtpUsername,
            @Value("${spring.mail.password:}") String smtpKey
    ) {
        this.mailSender = mailSender;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.enabled = enabled;
        this.smtpUsername = smtpUsername;
        this.smtpKey = smtpKey;
        validarConfiguracao();
    }

    public void enviarCodigoVerificacao(String destinatario, String nome, String codigo) {
        enviar(destinatario, nome, "Confirme seu e-mail na CareBridge", montarHtmlVerificacao(codigo));
    }

    public void enviarCodigoRedefinicaoSenha(String destinatario, String nome, String codigo) {
        enviar(destinatario, nome, "Redefinição de senha da CareBridge", montarHtmlRedefinicao(codigo));
    }

    private void enviar(String destinatario, String nome, String assunto, String html) {
        if (!enabled) {
            return;
        }
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, "UTF-8");
            helper.setFrom(senderEmail, senderName);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(html, true);
            mailSender.send(mensagem);
        } catch (MessagingException | MailException | UnsupportedEncodingException exception) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                    "Não foi possível enviar o e-mail. Tente novamente mais tarde.", exception);
        }
    }

    private void validarConfiguracao() {
        if (enabled && (senderEmail.isBlank() || smtpUsername.isBlank() || smtpKey.isBlank())) {
            throw new IllegalStateException(
                    "BREVO_SMTP_USERNAME, BREVO_SMTP_KEY e BREVO_SENDER_EMAIL são obrigatórios "
                            + "quando BREVO_ENABLED=true.");
        }
    }

    private String montarHtmlVerificacao(String codigo) {
        return """
                <!doctype html>
                <html lang="pt-BR">
                <body style="font-family:Arial,sans-serif;color:#1f2937">
                  <h2>Confirme seu e-mail</h2>
                  <p>Use o código abaixo para confirmar sua conta profissional na CareBridge:</p>
                  <p style="font-size:28px;font-weight:bold;letter-spacing:6px">%s</p>
                  <p>O código é válido por 15 minutos. Se você não fez este cadastro, ignore esta mensagem.</p>
                </body>
                </html>
                """.formatted(codigo);
    }

    private String montarHtmlRedefinicao(String codigo) {
        return """
                <!doctype html>
                <html lang="pt-BR">
                <body style="font-family:Arial,sans-serif;color:#1f2937">
                  <h2>Redefinição de senha</h2>
                  <p>Use o código abaixo para redefinir sua senha na CareBridge:</p>
                  <p style="font-size:28px;font-weight:bold;letter-spacing:6px">%s</p>
                  <p>O código é válido por 15 minutos. Se você não solicitou a alteração, ignore esta mensagem.</p>
                </body>
                </html>
                """.formatted(codigo);
    }
}