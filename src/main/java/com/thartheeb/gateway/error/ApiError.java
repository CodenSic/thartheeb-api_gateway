package com.thartheeb.gateway.error;

public record ApiError(int status, String error, String message) {
}
