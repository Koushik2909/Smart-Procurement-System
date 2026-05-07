package com.procurement.notification.service;

import com.procurement.notification.domain.Notification;
import com.procurement.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Reactive sink for GraphQL subscriptions
    private final Sinks.Many<Notification> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    public Notification createNotification(String eventType, String message, String targetRole, Long entityId) {
        Notification notification = new Notification();
        notification.setEventType(eventType);
        notification.setMessage(message);
        notification.setTargetRole(targetRole);
        notification.setEntityId(entityId);
        Notification saved = notificationRepository.save(notification);

        // Emit to subscribers
        notificationSink.tryEmitNext(saved);

        return saved;
    }

    public Flux<Notification> subscribeToNotifications(String role) {
        return notificationSink.asFlux()
                .filter(n -> role == null || n.getTargetRole().equals(role));
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalse();
    }

    public List<Notification> getNotificationsByRole(String role) {
        return notificationRepository.findByTargetRole(role);
    }
}
