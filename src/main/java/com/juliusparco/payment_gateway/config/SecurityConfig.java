package com.juliusparco.payment_gateway.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.juliusparco.payment_gateway.auth.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/token").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) -> {
							response.setStatus(401);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required\"}");
						})
						.accessDeniedHandler((request, response, exception) -> {
							response.setStatus(403);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\"}");
						}))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
