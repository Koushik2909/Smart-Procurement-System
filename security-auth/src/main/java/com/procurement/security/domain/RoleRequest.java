package com.procurement.security.domain;

import com.procurement.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role_requests")
@Getter
@Setter
public class RoleRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column(name = "requested_role", nullable = false)
    private String requestedRole;

    @Column(nullable = false)
    private String status = "PENDING";
    
    public RoleRequest() {}

    public RoleRequest(Long userId, String username, String requestedRole) {
        this.userId = userId;
        this.username = username;
        this.requestedRole = requestedRole;
    }
}
