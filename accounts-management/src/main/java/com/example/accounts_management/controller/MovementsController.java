package com.example.accounts_management.controller;

import com.example.accounts_management.infrastructure.dto.MovementRequest;
import com.example.accounts_management.infrastructure.dto.MovementResponse;
import com.example.accounts_management.infrastructure.movements.api.MovementsApi;
import com.example.accounts_management.service.MovementsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
public class MovementsController implements MovementsApi {

    private final MovementsService movementService;

    public MovementsController(MovementsService movementService){
        this.movementService = movementService;
    }


    @Override
    public Mono<ResponseEntity<Void>> delete(Long id, ServerWebExchange exchange) {
        return movementService.delete(id).then(Mono.just(ResponseEntity.ok().build()));
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> findById(Long id, ServerWebExchange exchange) {
        return movementService.findById(id)
                .flatMap(movementResponse -> Mono.just(ResponseEntity.ok(movementResponse)));
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getAllMovements(ServerWebExchange exchange) {
        Flux<MovementResponse> movements = movementService.findAll();
        return Mono.just(ResponseEntity.ok(movements));
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getAllMovementsByDateRanges(LocalDate startDate, LocalDate endDate, Long customerId, ServerWebExchange exchange) {
        Flux<MovementResponse> movementResponseFlux = movementService.getMovementsReport(startDate, endDate, customerId);
        return Mono.just(ResponseEntity.ok(movementResponseFlux));
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getByAccountId(Long id, ServerWebExchange exchange) {
        Flux<MovementResponse> movements = movementService.findAllByAccount(id);
        return Mono.just(ResponseEntity.ok(movements));
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> save(Mono<MovementRequest> movementRequest, ServerWebExchange exchange) {
        return movementRequest
                .flatMap(request -> movementService.save(request).map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> update(Long id, Mono<MovementRequest> movementRequest, ServerWebExchange exchange) {
        return movementRequest
                .flatMap(request -> movementService.updateMovement(id, request).map(ResponseEntity::ok));
    }
}
