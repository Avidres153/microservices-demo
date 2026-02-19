package com.example.accounts_management.repository;

import com.example.accounts_management.model.CustomerSnapshot;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerSnapshotRepository extends ReactiveCrudRepository<CustomerSnapshot, Long> {

    @Query("""
        insert into customer_snapshot (id, customer_name)
        values (:#{#snapshot.id}, :#{#snapshot.customerName})
    """)
    Mono<Void> insertSnapshot(CustomerSnapshot snapshot);
}
