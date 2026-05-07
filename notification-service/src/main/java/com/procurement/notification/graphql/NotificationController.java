package com.procurement.notification.graphql;

import com.procurement.notification.domain.Notification;
import com.procurement.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;

@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Notification> getUnreadNotifications() {
        return notificationService.getUnreadNotifications();
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Notification sendNotification(@Argument String eventType, @Argument String message,
                                          @Argument String targetRole, @Argument Long entityId) {
        return notificationService.createNotification(eventType, message, targetRole, entityId);
    }

    @SubscriptionMapping
    public Flux<Notification> notificationReceived(@Argument String role) {
        return notificationService.subscribeToNotifications(role);
    }
}
