package com.procurement.security.dto;

public record AuthResponse(String token, String refreshToken, String type, String username, String role) {
}
