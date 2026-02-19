package com.example.accounts_management.repository;

import com.example.accounts_management.model.Movement;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovementsRepository extends ReactiveCrudRepository<Movement, Long> {

    Flux<Movement> findByAccountIdOrderByDateDesc(Long accountId);

    Flux<Movement> findByAccountIdOrderByDateAsc(Long accountId);

    @Query("Select * from movement where account_id = :accountId order by 1 desc limit 1")
    Mono<Movement> findLastTransactionByAccountId(@Param("accountId") Long accountId);

    @Query("Select * from movement where movement_date Between :startDate and :endDate and account_id= :customerId order by id asc")
    Flux<Movement> findTransactionByDateRangeAndCustomerId(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("customerId") Long customerId);
}
