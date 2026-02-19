package com.example.customers_management.service;

import com.example.customers_management.exception.custom.BusinessException;
import com.example.customers_management.infrastructure.dto.CustomerRequest;
import com.example.customers_management.model.Customer;
import com.example.customers_management.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setIdentification("12345");

        customerRequest = new CustomerRequest();
        customerRequest.setName("John Doe");
        customerRequest.setIdentification("12345");
    }

    @Test
    void findById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.findById(1L))
                .expectNextMatches(response -> response.getId().equals(1L) && response.getName().equals("John Doe"))
                .verifyComplete();
    }

    @Test
    void findById_NotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(customerService.findById(1L))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void findAll_Success() {
        when(customerRepository.findAll()).thenReturn(Flux.just(customer));

        StepVerifier.create(customerService.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void save_Conflict() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.save(customerRequest))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void update_Success() {
        when(customerRepository.findById(1L)).thenReturn(Mono.just(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.update(1L, customerRequest))
                .expectNextMatches(response -> response.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void update_NotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(customerService.update(1L, customerRequest))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void deleteById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Mono.just(customer));
        when(customerRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteById(1L))
                .verifyComplete();
    }

    @Test
    void deleteById_NotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteById(1L))
                .expectError(BusinessException.class)
                .verify();
    }
}
