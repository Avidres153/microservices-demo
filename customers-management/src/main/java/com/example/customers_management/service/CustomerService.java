package com.example.customers_management.service;

import com.example.customers_management.exception.custom.BusinessException;
import com.example.customers_management.infrastructure.dto.CustomerRequest;
import com.example.customers_management.infrastructure.dto.CustomerResponse;
import com.example.customers_management.model.Customer;
import com.example.customers_management.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Mono<CustomerResponse> findById(Long customerId) {
        return customerRepository.findById(customerId)
                .map(Customer::toCustomerResponse)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Customer not found")));
    }

    public Flux<CustomerResponse> findAll() {
        return customerRepository.findAll()
                .map(Customer::toCustomerResponse);
    }

    public Mono<CustomerResponse> save(CustomerRequest customerRequest) {
        return customerRepository.findByIdentification(customerRequest.getIdentification())
                .flatMap(existingCustomer ->
                        Mono.<CustomerResponse>error(new BusinessException(
                                HttpStatus.CONFLICT,
                                "Customer with identification " +
                                        customerRequest.getIdentification() +
                                        " already exists"
                        ))
                )
                .switchIfEmpty(
                        Mono.defer(() -> {
                            Customer customer = Customer.fromCustomerRequest(customerRequest);
                            return customerRepository.save(customer)
                                    .map(Customer::toCustomerResponse);
                        })
                );
    }

    public Mono<CustomerResponse> update(Long id, CustomerRequest customerRequest) {
        return findById(id)
                .flatMap(customerResponse -> {
                    Customer customer = Customer.fromCustomerRequest(customerRequest);
                    customer.setId(id);
                    return customerRepository.save(customer)
                            .map(Customer::toCustomerResponse);
                })
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Customer not found")));
    }

    public Mono<Void> deleteById(Long customerId) {
        return findById(customerId)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Customer not found")))
                .flatMap(customerResponse -> customerRepository.deleteById(customerId));
    }
}
