package com.juliusparco.payment_gateway.paymentintent;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "payment_intents")
public class PaymentIntent {

	@Id
	private UUID id;

	@Column(name = "owner_subject", nullable = false, length = 100)
	private String ownerSubject;

	@Column(name = "amount_minor", nullable = false)
	private long amountMinor;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(length = 255)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private PaymentIntentStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected PaymentIntent() {
	}

	PaymentIntent(String ownerSubject, long amountMinor, String currency, String description, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerSubject = ownerSubject;
		this.amountMinor = amountMinor;
		this.currency = currency;
		this.description = description;
		this.status = PaymentIntentStatus.REQUIRES_CONFIRMATION;
		this.createdAt = now;
		this.updatedAt = now;
	}

	void markSucceeded(Instant now) {
		if (status == PaymentIntentStatus.REQUIRES_CONFIRMATION) {
			status = PaymentIntentStatus.SUCCEEDED;
			updatedAt = now;
		}
	}

	public UUID getId() {
		return id;
	}

	public String getOwnerSubject() {
		return ownerSubject;
	}

	public long getAmountMinor() {
		return amountMinor;
	}

	public String getCurrency() {
		return currency;
	}

	public String getDescription() {
		return description;
	}

	public PaymentIntentStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
