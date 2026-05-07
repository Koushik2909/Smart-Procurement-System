package com.procurement.security.graphql;

import com.procurement.security.domain.User;
import com.procurement.security.dto.AuthInput;
import com.procurement.security.dto.AuthResponse;
import com.procurement.security.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @MutationMapping
    public User registerUser(@Argument AuthInput input) {
        return authService.registerUser(input);
    }

    @MutationMapping
    public AuthResponse login(@Argument AuthInput input) {
        return authService.authenticateUser(input);
    }

    @MutationMapping
    public AuthResponse refreshToken(@Argument String token) {
        return authService.refreshToken(token);
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Boolean requestVendorRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return authService.requestVendorRole(username);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean changeUserRole(@Argument Long userId, @Argument String role) {
        return authService.changeUserRole(userId, role);
    }

    // A simple query to test authentication
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public String me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
