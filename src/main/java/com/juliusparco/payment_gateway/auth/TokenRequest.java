package com.juliusparco.payment_gateway.auth;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
		@NotBlank String clientId,
		@NotBlank String clientSecret) {
}
