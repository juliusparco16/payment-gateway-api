package com.juliusparco.payment_gateway.paymentintent;

import java.net.URI;
import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payment-intents")
public class PaymentIntentController {

	private final PaymentIntentService service;

	public PaymentIntentController(PaymentIntentService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<PaymentIntentResponse> create(
			@Valid @RequestBody CreatePaymentIntentRequest request,
			Principal principal) {
		PaymentIntentResponse response = service.create(principal.getName(), request);
		return ResponseEntity.created(URI.create("/api/payment-intents/" + response.id())).body(response);
	}

	@GetMapping("/{id}")
	public PaymentIntentResponse get(@PathVariable UUID id, Principal principal) {
		return service.get(id, principal.getName());
	}

	@PostMapping("/{id}/confirm")
	public PaymentIntentResponse confirm(@PathVariable UUID id, Principal principal) {
		return service.confirm(id, principal.getName());
	}
}
