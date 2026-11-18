package com.roombooking.dto;

import java.util.Map;

public record ErrorResponse(int status, String error, Map<String, String> messages) {
}
