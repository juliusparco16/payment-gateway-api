package com.juliusparco.payment_gateway.paymentintent;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePaymentIntentRequest(
		@NotNull @Positive Long amount,
		@NotNull @Pattern(regexp = "[A-Z]{3}", message = "must be a three-letter uppercase ISO currency code") String currency,
		@Size(max = 255) String description) {
}
