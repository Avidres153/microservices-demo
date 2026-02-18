package com.example.accounts_management.repository;

import com.example.accounts_management.model.CustomerSnapshot;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSnapshotRepository extends ReactiveCrudRepository<CustomerSnapshot, Long> {
}
