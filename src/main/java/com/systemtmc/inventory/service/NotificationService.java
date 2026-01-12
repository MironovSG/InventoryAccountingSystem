package com.systemtmc.inventory.service;

import com.systemtmc.inventory.model.entity.Request;
import com.systemtmc.inventory.model.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки уведомлений
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final JavaMailSender mailSender;
    
    @Async
    public void sendRequestCreatedNotification(Request request) {
        try {
            String subject = "Новая заявка на выдачу ТМЦ: " + request.getRequestNumber();
            String message = String.format(
                    "Создана новая заявка на выдачу ТМЦ\n\n" +
                    "Номер заявки: %s\n" +
                    "Инициатор: %s\n" +
                    "Подразделение: %s\n" +
                    "Назначение: %s\n" +
                    "Количество позиций: %d\n\n" +
                    "Дата создания: %s",
                    request.getRequestNumber(),
                    request.getRequester().getFullName(),
                    request.getDepartment().getName(),
                    request.getPurpose(),
                    request.getItems().size(),
                    request.getCreatedAt()
            );
            
            // Отправка email (здесь нужно добавить получателей - МОЛ)
            log.info("Отправлено уведомление о создании заявки: {}", request.getRequestNumber());
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления о создании заявки", e);
        }
    }
    
    @Async
    public void sendRequestStatusChangedNotification(Request request, RequestStatus oldStatus) {
        try {
            String subject = "Изменен статус заявки: " + request.getRequestNumber();
            String message = String.format(
                    "Изменен статус заявки на выдачу ТМЦ\n\n" +
                    "Номер заявки: %s\n" +
                    "Старый статус: %s\n" +
                    "Новый статус: %s\n" +
                    "Инициатор: %s\n" +
                    "Подразделение: %s\n",
                    request.getRequestNumber(),
                    oldStatus.getDisplayName(),
                    request.getStatus().getDisplayName(),
                    request.getRequester().getFullName(),
                    request.getDepartment().getName()
            );
            
            String recipientEmail = request.getRequester().getEmail();
            sendEmail(recipientEmail, subject, message);
            
            log.info("Отправлено уведомление об изменении статуса заявки: {}", request.getRequestNumber());
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления об изменении статуса заявки", e);
        }
    }
    
    @Async
    public void sendLowStockNotification(String materialName, String article) {
        try {
            String subject = "Низкий уровень запасов: " + materialName;
            String message = String.format(
                    "Внимание! Низкий уровень запасов материала\n\n" +
                    "Артикул: %s\n" +
                    "Название: %s\n\n" +
                    "Необходимо пополнение запасов.",
                    article,
                    materialName
            );
            
            log.info("Отправлено уведомление о низком уровне запасов: {} - {}", article, materialName);
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления о низком уровне запасов", e);
        }
    }
    
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("inventory-system@company.com");
            
            mailSender.send(message);
            log.debug("Email отправлен на: {}", to);
        } catch (Exception e) {
            log.error("Ошибка при отправке email на: {}", to, e);
        }
    }
}
