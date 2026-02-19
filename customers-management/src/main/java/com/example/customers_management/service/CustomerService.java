package com.example.customers_management.service;

import com.example.customers_management.exception.custom.BusinessException;
import com.example.customers_management.infrastructure.dto.CustomerRequest;
import com.example.customers_management.infrastructure.dto.CustomerResponse;
import com.example.customers_management.model.Customer;
import com.example.customers_management.repository.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${spring.kafka.topic}")
    private String topicName;

    public CustomerService(CustomerRepository customerRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
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
                                    .map(savedCustomer -> {
                                        try {
                                            sendKafkaEvent(savedCustomer.getId().toString(), savedCustomer.getName());
                                        } catch (JsonProcessingException e) {
                                            Mono.error(new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Error to send event"));
                                        }
                                        return Customer.toCustomerResponse(savedCustomer);
                                    });
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

    public void sendKafkaEvent(String customerId, String customerName) throws JsonProcessingException {
        Map<String, String> purchaseEvent = Map.of(customerId, customerName);

        var value = objectMapper.writeValueAsString(purchaseEvent);

        var completableFuture = kafkaTemplate.send(topicName, customerId, value);

        completableFuture.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error to send event: ", ex);
            } else {
                log.info("Message sent successfully: {} - {}, returned value (partition value): {}", customerId, value, result.getRecordMetadata().partition());
            }
        });
    }
}
