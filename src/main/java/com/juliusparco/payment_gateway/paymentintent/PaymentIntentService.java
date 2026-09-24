package com.juliusparco.payment_gateway.paymentintent;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juliusparco.payment_gateway.common.ResourceNotFoundException;

@Service
public class PaymentIntentService {

	private final PaymentIntentRepository repository;
	private final Clock clock;

	public PaymentIntentService(PaymentIntentRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	@Transactional
	public PaymentIntentResponse create(String ownerSubject, CreatePaymentIntentRequest request) {
		Instant now = clock.instant();
		PaymentIntent intent = new PaymentIntent(
				ownerSubject,
				request.amount(),
				request.currency(),
				request.description(),
				now);
		return PaymentIntentResponse.from(repository.save(intent));
	}

	@Transactional(readOnly = true)
	public PaymentIntentResponse get(UUID id, String ownerSubject) {
		return PaymentIntentResponse.from(findOwned(id, ownerSubject));
	}

	@Transactional
	public PaymentIntentResponse confirm(UUID id, String ownerSubject) {
		PaymentIntent intent = findOwned(id, ownerSubject);
		intent.markSucceeded(clock.instant());
		return PaymentIntentResponse.from(intent);
	}

	private PaymentIntent findOwned(UUID id, String ownerSubject) {
		return repository.findByIdAndOwnerSubject(id, ownerSubject)
				.orElseThrow(() -> new ResourceNotFoundException("Payment intent not found"));
	}
}
