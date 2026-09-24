package com.juliusparco.payment_gateway.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
