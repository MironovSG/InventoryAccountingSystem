package com.systemtmc.inventory.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.InputStream;
import java.util.Properties;

/**
 * Предоставляет no-op JavaMailSender, если SMTP не настроен,
 * чтобы приложение запускалось без реальной почты.
 */
@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        return new NoOpJavaMailSender();
    }

    /**
     * Реализация JavaMailSender, которая не отправляет письма (для запуска без SMTP).
     */
    private static class NoOpJavaMailSender implements JavaMailSender {

        private final Session session = Session.getDefaultInstance(new Properties());

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(session);
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            MimeMessage message = createMimeMessage();
            // no-op: не разбираем content
            return message;
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            // no-op
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            // no-op
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            // no-op
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            // no-op
        }
    }
}
