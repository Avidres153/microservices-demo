package com.example.accounts_management.repository;

import com.example.accounts_management.model.Account;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface AccountsRepository extends ReactiveCrudRepository<Account, Long> {

    @Query(value = "Select * from account where customer_id = :customerId")
    Flux<Account> findByCustomerId(@Param("customerId") Long customerId);

    Mono<Account> findByAccountNumber(String accountNumber);
}
