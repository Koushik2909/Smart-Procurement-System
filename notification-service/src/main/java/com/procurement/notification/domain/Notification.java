package com.procurement.notification.domain;

import com.procurement.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String message;

    @Column(name = "target_role")
    private String targetRole;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "is_read")
    private Boolean isRead = false;
}
