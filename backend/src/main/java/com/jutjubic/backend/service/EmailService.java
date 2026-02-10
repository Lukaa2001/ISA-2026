package com.jutjubic.backend.service;

import com.jutjubic.backend.config.AppProperties;
import com.jutjubic.backend.exception.ApiException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;
  private final AppProperties appProperties;
  private final String smtpUser;
  private final String smtpPass;

  public EmailService(
      JavaMailSender mailSender,
      AppProperties appProperties,
      @Value("${spring.mail.username:}") String smtpUser,
      @Value("${spring.mail.password:}") String smtpPass
  ) {
    this.mailSender = mailSender;
    this.appProperties = appProperties;
    this.smtpUser = smtpUser == null ? "" : smtpUser;
    this.smtpPass = smtpPass == null ? "" : smtpPass;
  }

  public String sendActivationEmail(String email, String token) {
    String activationUrl = appProperties.getBackendUrl() + "/api/auth/activate/" + token;

    // Preserve dev ergonomics without requiring SMTP credentials.
    if (smtpUser.isBlank() || smtpPass.isBlank()) {
      log.info("Activation URL for {}: {}", email, activationUrl);
      return activationUrl;
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom("noreply@jutjubic.com", "Jutjubic");
      helper.setTo(email);
      helper.setSubject("Aktivirajte svoj Jutjubic racun");
      helper.setText("""
          <h1>Dobrodosli na Jutjubic!</h1>
          <p>Kliknite na link ispod za aktivaciju vaseg racuna:</p>
          <a href=\"%s\">%s</a>
          <p>Link je jednokratan.</p>
          """.formatted(activationUrl, activationUrl), true);

      mailSender.send(message);
      return null;
    } catch (Exception ex) {
      throw new ApiException(500, "Failed to send activation email");
    }
  }
}
