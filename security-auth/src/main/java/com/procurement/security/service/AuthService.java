package com.procurement.security.service;

import com.procurement.core.domain.Role;
import com.procurement.core.exception.ResourceNotFoundException;
import com.procurement.security.config.JwtUtils;
import com.procurement.security.domain.User;
import com.procurement.security.dto.AuthInput;
import com.procurement.security.dto.AuthResponse;
import com.procurement.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse authenticateUser(AuthInput input) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.username(), input.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthResponse(jwt, refreshToken, "Bearer", userDetails.getUsername(), user.getRole().name());
    }

    public User registerUser(AuthInput input) {
        if (userRepository.findByUsername(input.username()).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        User user = new User(input.username(), encoder.encode(input.password()), Role.USER);
        return userRepository.save(user);
    }
    
    public AuthResponse refreshToken(String requestRefreshToken) {
        if (requestRefreshToken != null && jwtUtils.validateJwtToken(requestRefreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            String newAccessToken = jwtUtils.generateTokenFromUsername(username, 86400000); // 1 day
            return new AuthResponse(newAccessToken, requestRefreshToken, "Bearer", username, user.getRole().name());
        }
        throw new RuntimeException("Refresh token was expired or invalid. Please make a new signin request");
    }

    public boolean requestVendorRole(String username) {
        // In a real system, this might insert a record into a VendorRequest table.
        // For now, it just returns true to signify the request was received.
        return true;
    }

    public boolean changeUserRole(Long userId, String newRoleStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        try {
            Role newRole = Role.valueOf(newRoleStr.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
            return true;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role specified");
        }
    }
}
