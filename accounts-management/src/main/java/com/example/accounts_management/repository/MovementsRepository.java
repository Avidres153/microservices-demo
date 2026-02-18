package com.example.accounts_management.repository;

import com.example.accounts_management.model.Movement;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface MovementsRepository extends ReactiveCrudRepository<Movement, Long> {

    @Query("Select * from movement where account_id = :accountId order by id asc")
    Flux<Movement> findByAccountId(@Param("accountId") Long accountId);
}
