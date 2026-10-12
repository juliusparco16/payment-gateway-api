package com.juliusparco.payment_gateway.paymentintent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.juliusparco.payment_gateway.common.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class PaymentIntentServiceTest {

	private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");

	@Mock
	private PaymentIntentRepository repository;

	private PaymentIntentService service;

	@BeforeEach
	void setUp() {
		service = new PaymentIntentService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void createsPaymentIntentInRequiresConfirmationState() {
		when(repository.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PaymentIntentResponse response = service.create(
				"client-a",
				new CreatePaymentIntentRequest(2599L, "CAD", "Order 1001"));

		assertEquals(2599L, response.amount());
		assertEquals("CAD", response.currency());
		assertEquals(PaymentIntentStatus.REQUIRES_CONFIRMATION, response.status());
		assertEquals(NOW, response.createdAt());
	}

	@Test
	void confirmsOwnedPaymentIntentAndIsIdempotent() {
		UUID id = UUID.randomUUID();
		PaymentIntent intent = new PaymentIntent("client-a", 2599L, "CAD", null, NOW.minusSeconds(60));
		when(repository.findByIdAndOwnerSubject(id, "client-a")).thenReturn(Optional.of(intent));

		PaymentIntentResponse first = service.confirm(id, "client-a");
		PaymentIntentResponse second = service.confirm(id, "client-a");

		assertEquals(PaymentIntentStatus.SUCCEEDED, first.status());
		assertEquals(PaymentIntentStatus.SUCCEEDED, second.status());
		assertEquals(NOW, second.updatedAt());
	}

	@Test
	void hidesMissingOrUnownedPaymentIntentBehindNotFound() {
		UUID id = UUID.randomUUID();
		when(repository.findByIdAndOwnerSubject(id, "client-b")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> service.get(id, "client-b"));
	}
}
