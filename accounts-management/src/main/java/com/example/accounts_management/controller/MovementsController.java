package com.example.accounts_management.controller;

import com.example.accounts_management.infrastructure.dto.MovementRequest;
import com.example.accounts_management.infrastructure.dto.MovementResponse;
import com.example.accounts_management.infrastructure.movements.api.MovementsApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
public class MovementsController implements MovementsApi {

    @Override
    public Mono<ResponseEntity<Void>> delete(Long id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> findById(Long id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getAllMovements(ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getAllMovementsByDateRanges(LocalDate startDate, LocalDate endDate, Long customerId, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getByAccountId(Long id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> save(Mono<MovementRequest> movementRequest, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> update(Long id, Mono<MovementRequest> movementRequest, ServerWebExchange exchange) {
        return null;
    }
}
