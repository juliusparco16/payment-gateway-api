package com.juliusparco.payment_gateway.paymentintent;

import java.time.Instant;
import java.util.UUID;

public record PaymentIntentResponse(
		UUID id,
		long amount,
		String currency,
		String description,
		PaymentIntentStatus status,
		Instant createdAt,
		Instant updatedAt) {

	static PaymentIntentResponse from(PaymentIntent intent) {
		return new PaymentIntentResponse(
				intent.getId(),
				intent.getAmountMinor(),
				intent.getCurrency(),
				intent.getDescription(),
				intent.getStatus(),
				intent.getCreatedAt(),
				intent.getUpdatedAt());
	}
}
