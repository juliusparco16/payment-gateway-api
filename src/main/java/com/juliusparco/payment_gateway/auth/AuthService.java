package com.juliusparco.payment_gateway.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final String demoClientId;
	private final String demoClientSecret;
	private final JwtService jwtService;

	public AuthService(
			@Value("${app.security.demo-client-id}") String demoClientId,
			@Value("${app.security.demo-client-secret}") String demoClientSecret,
			JwtService jwtService) {
		this.demoClientId = demoClientId;
		this.demoClientSecret = demoClientSecret;
		this.jwtService = jwtService;
	}

	public TokenResponse authenticate(TokenRequest request) {
		if (!secureEquals(demoClientId, request.clientId())
				|| !secureEquals(demoClientSecret, request.clientSecret())) {
			throw new InvalidCredentialsException();
		}
		return jwtService.issueToken(request.clientId());
	}

	private boolean secureEquals(String expected, String actual) {
		return MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8),
				actual.getBytes(StandardCharsets.UTF_8));
	}
}
