package com.example.customers_management.controller;

import com.example.customers_management.infrastructure.api.DefaultApi;
import com.example.customers_management.infrastructure.dto.CustomerRequest;
import com.example.customers_management.infrastructure.dto.CustomerResponse;
import com.example.customers_management.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CustomerController implements DefaultApi {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(Mono<CustomerRequest> customerRequest, ServerWebExchange exchange) {
        return customerRequest.flatMap(customerService::save)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomerById(Long id, ServerWebExchange exchange) {
        return customerService.deleteById(id)
                .flatMap(result -> Mono.just(ResponseEntity.ok().build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> getAllCustomers(ServerWebExchange exchange) {
        Flux<CustomerResponse> customerResponseFlux = customerService.findAll();
        return Mono.just(ResponseEntity.ok(customerResponseFlux));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(Long id, ServerWebExchange exchange) {
        return customerService.findById(id)
                .flatMap(customerResponse -> Mono.just(ResponseEntity.ok(customerResponse)));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomerById(Long id, Mono<CustomerRequest> customerRequest, ServerWebExchange exchange) {
        return customerRequest.flatMap(request -> customerService.update(id, request))
                .map(ResponseEntity::ok);
    }
}
