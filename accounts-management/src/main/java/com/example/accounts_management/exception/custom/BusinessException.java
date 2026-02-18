package com.example.accounts_management.exception.custom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@AllArgsConstructor
public class BusinessException extends Throwable {
    private final HttpStatus status;
    private final String message;
}
