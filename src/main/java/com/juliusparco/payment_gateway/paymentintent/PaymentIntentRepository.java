package com.juliusparco.payment_gateway.paymentintent;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {

	Optional<PaymentIntent> findByIdAndOwnerSubject(UUID id, String ownerSubject);
}
