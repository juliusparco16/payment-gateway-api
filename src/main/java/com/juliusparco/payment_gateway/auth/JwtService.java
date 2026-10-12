package com.juliusparco.payment_gateway.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final String ISSUER = "payment-gateway-api";

	private final SecretKey signingKey;
	private final Duration tokenTtl;
	private final Clock clock;

	public JwtService(
			@Value("${app.security.jwt-secret}") String encodedSecret,
			@Value("${app.security.jwt-ttl}") Duration tokenTtl,
			Clock clock) {
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
		this.tokenTtl = tokenTtl;
		this.clock = clock;
	}

	public TokenResponse issueToken(String subject) {
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(tokenTtl);
		String token = Jwts.builder()
				.setSubject(subject)
				.setIssuer(ISSUER)
				.setIssuedAt(Date.from(issuedAt))
				.setExpiration(Date.from(expiresAt))
				.signWith(signingKey, SignatureAlgorithm.HS256)
				.compact();
		return new TokenResponse(token, "Bearer", tokenTtl.toSeconds());
	}

	public String subject(String token) {
		Claims claims = Jwts.parserBuilder()
				.requireIssuer(ISSUER)
				.setSigningKey(signingKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.getSubject();
	}
}
