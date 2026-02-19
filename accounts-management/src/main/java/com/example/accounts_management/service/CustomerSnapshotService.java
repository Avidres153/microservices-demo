package com.example.accounts_management.service;

import com.example.accounts_management.model.CustomerSnapshot;
import com.example.accounts_management.repository.CustomerSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class CustomerSnapshotService {

    private final CustomerSnapshotRepository customerSnapshotRepository;
    private final ObjectMapper objectMapper;

    public CustomerSnapshotService(CustomerSnapshotRepository customerSnapshotRepository, ObjectMapper objectMapper) {
        this.customerSnapshotRepository = customerSnapshotRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("Received Message: {}", message);
        try {
            Map<String, String> event = objectMapper.readValue(message, new TypeReference<>() {
            });

            for (Map.Entry<String, String> entry : event.entrySet()) {
                Long customerId = Long.valueOf(entry.getKey());
                String customerName = entry.getValue();

                CustomerSnapshot snapshot = CustomerSnapshot.builder()
                        .id(customerId)
                        .customerName(customerName)
                        .build();


                save(snapshot).subscribe(
                        null,
                        error -> log.error("Error saving customer snapshot: {}", error.getMessage(), error),
                        () -> log.info("Customer snapshot saved successfully for ID: {}", customerId)
                );
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing Kafka message", e);
        } catch (NumberFormatException e) {
            log.error("Error parsing customer ID", e);
        }
    }

    public Mono<Void> save(CustomerSnapshot customerSnapshot) {
        return customerSnapshotRepository.insertSnapshot(customerSnapshot).then();
    }
}
