package com.juliusparco.payment_gateway.auth;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Invalid client credentials");
	}
}
