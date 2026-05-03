package dev.tmmc.ulms.security;

public record JwtPrincipal(Integer userId, String email, String role) {}
