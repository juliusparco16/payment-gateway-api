package com.juliusparco.payment_gateway.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.juliusparco.payment_gateway.auth.JwtService;
import com.juliusparco.payment_gateway.paymentintent.PaymentIntentRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentIntentFlowIntegrationTest {

	private static final Pattern TOKEN_PATTERN = Pattern.compile("\\\"accessToken\\\":\\\"([^\\\"]+)\\\"");
	private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"");

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
			.withDatabaseName("payment_gateway")
			.withUsername("payment_gateway")
			.withPassword("payment_gateway");

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PaymentIntentRepository repository;

	@Autowired
	private JwtService jwtService;

	@BeforeEach
	void cleanDatabase() {
		repository.deleteAll();
	}

	@Test
	void createsRetrievesAndConfirmsPaymentIntent() throws Exception {
		String token = obtainDemoToken();

		String createResponse = mockMvc.perform(post("/api/payment-intents")
						.header(HttpHeaders.AUTHORIZATION, bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":2599,"currency":"CAD","description":"Order 1001"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists(HttpHeaders.LOCATION))
				.andExpect(jsonPath("$.amount").value(2599))
				.andExpect(jsonPath("$.currency").value("CAD"))
				.andExpect(jsonPath("$.status").value("REQUIRES_CONFIRMATION"))
				.andReturn().getResponse().getContentAsString();

		String id = extract(ID_PATTERN, createResponse);

		mockMvc.perform(get("/api/payment-intents/{id}", id)
						.header(HttpHeaders.AUTHORIZATION, bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.status").value("REQUIRES_CONFIRMATION"));

		mockMvc.perform(post("/api/payment-intents/{id}/confirm", id)
						.header(HttpHeaders.AUTHORIZATION, bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCEEDED"));

		mockMvc.perform(post("/api/payment-intents/{id}/confirm", id)
						.header(HttpHeaders.AUTHORIZATION, bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCEEDED"));
	}

	@Test
	void rejectsUnauthenticatedAndInvalidRequests() throws Exception {
		mockMvc.perform(post("/api/payment-intents")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"amount\":2599,\"currency\":\"CAD\"}"))
				.andExpect(status().isUnauthorized());

		String token = obtainDemoToken();
		mockMvc.perform(post("/api/payment-intents")
						.header(HttpHeaders.AUTHORIZATION, bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"amount\":0,\"currency\":\"cad\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.amount").exists())
				.andExpect(jsonPath("$.fieldErrors.currency").exists());
	}

	@Test
	void doesNotExposeAnotherClientsPaymentIntent() throws Exception {
		String ownerToken = obtainDemoToken();
		String createResponse = mockMvc.perform(post("/api/payment-intents")
						.header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"amount\":1000,\"currency\":\"CAD\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String id = extract(ID_PATTERN, createResponse);
		String otherToken = jwtService.issueToken("other-client").accessToken();

		mockMvc.perform(get("/api/payment-intents/{id}", id)
						.header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
				.andExpect(status().isNotFound());
	}

	private String obtainDemoToken() throws Exception {
		String response = mockMvc.perform(post("/api/auth/token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"clientId\":\"demo-client\",\"clientSecret\":\"change-me\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn().getResponse().getContentAsString();
		return extract(TOKEN_PATTERN, response);
	}

	private String extract(Pattern pattern, String json) {
		Matcher matcher = pattern.matcher(json);
		if (!matcher.find()) {
			throw new AssertionError("Expected value was not present in response: " + json);
		}
		return matcher.group(1);
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}
}
