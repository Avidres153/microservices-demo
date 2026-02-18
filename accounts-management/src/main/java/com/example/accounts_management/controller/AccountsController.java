package com.example.accounts_management.controller;

import com.example.accounts_management.infrastructure.accounts.api.AccountsApi;
import com.example.accounts_management.infrastructure.dto.AccountRequest;
import com.example.accounts_management.infrastructure.dto.AccountResponse;
import com.example.accounts_management.service.AccountsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class AccountsController implements AccountsApi {

    private final AccountsService accountsService;

    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @Override
    public Mono<ResponseEntity<Void>> delete(Long id, ServerWebExchange exchange) {
        return accountsService.deleteById(id).then(Mono.just(ResponseEntity.ok().build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> getAllAccounts(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(accountsService.findAll()));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> getByAccountId(Long id, ServerWebExchange exchange) {
        return accountsService.findById(id)
                .flatMap(accountResponse -> Mono.just(ResponseEntity.ok(accountResponse)));
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> getByCustomerId(Long id, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(accountsService.findByCustomerId(id)));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> save(Mono<AccountRequest> accountRequest, ServerWebExchange exchange) {
        return accountRequest
                .flatMap(accountsService::save)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> update(Long id, Mono<AccountRequest> accountRequest, ServerWebExchange exchange) {
        return accountRequest
                .flatMap(request -> accountsService.update(id, request))
                .map(ResponseEntity::ok);
    }
}
