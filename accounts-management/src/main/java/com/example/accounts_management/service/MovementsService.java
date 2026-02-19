package com.example.accounts_management.service;

import com.example.accounts_management.exception.custom.BusinessException;
import com.example.accounts_management.infrastructure.dto.MovementRequest;
import com.example.accounts_management.infrastructure.dto.MovementResponse;
import com.example.accounts_management.model.Account;
import com.example.accounts_management.model.Movement;
import com.example.accounts_management.repository.AccountsRepository;
import com.example.accounts_management.repository.CustomerSnapshotRepository;
import com.example.accounts_management.repository.MovementsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovementsService {

    private final MovementsRepository movementsRepository;
    private final AccountsRepository accountsRepository;
    private final CustomerSnapshotRepository customerSnapshotRepository;

    public MovementsService(MovementsRepository movementsRepository, AccountsRepository accountsRepository, CustomerSnapshotRepository customerSnapshotRepository) {
        this.movementsRepository = movementsRepository;
        this.accountsRepository = accountsRepository;
        this.customerSnapshotRepository = customerSnapshotRepository;
    }

    public Mono<MovementResponse> findById(Long movementId) {
        return movementsRepository.findById(movementId)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Movement not found")))
                .flatMap(this::getMovementResponseFromMovement);
    }

    public Flux<MovementResponse> findAll() {
        return movementsRepository.findAll()
                .flatMap(this::getMovementResponseFromMovement);
    }

    public Mono<Void> delete(Long movementId) {
        return movementsRepository.findById(movementId)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Movement not found")))
                .flatMap(movement -> movementsRepository.deleteById(movementId));
    }

    public Mono<MovementResponse> save(MovementRequest movementRequest) {
        return accountsRepository.findById(movementRequest.getAccountId())
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Account not found")))
                .flatMap(account ->
                                addMovement(account, movementRequest)
//                        movementsRepository.save(Movement.fromMovementRequest(movementRequest))
//                                .flatMap(this::getMovementResponseFromMovement));
                );
    }

    public Flux<MovementResponse> findAllByAccount(Long accountId) {
        return movementsRepository.findByAccountIdOrderByDateDesc(accountId)
                .flatMap(this::getMovementResponseFromMovement);
    }

    private Mono<MovementResponse> addMovement(Account account, MovementRequest movementRequest) {
        return movementsRepository.findByAccountIdOrderByDateDesc(account.getId())
                .next()
                .map(Movement::getBalance)
                .defaultIfEmpty(account.getInitialBalance())
                .flatMap(previousBalance ->
                        getAndValidateBalance(movementRequest.getType(), previousBalance, movementRequest.getValue())
                                .flatMap(actualBalance ->
                                        movementsRepository
                                                .save(Movement.fromMovementRequest(movementRequest, actualBalance))
                                                .flatMap(this::getMovementResponseFromMovement)
                                )
                );

    }

    public Mono<MovementResponse> updateMovement(Long movementId, MovementRequest movementRequest) {
        return movementsRepository.findById(movementId)
                .switchIfEmpty(Mono.error(
                        new BusinessException(HttpStatus.NOT_FOUND, "Movement not found")
                ))
                .flatMap(existingMovement ->
                        accountsRepository.findById(existingMovement.getAccountId())
                                .switchIfEmpty(Mono.error(
                                        new BusinessException(HttpStatus.NOT_FOUND, "Account not found")
                                ))
                                .flatMap(account ->
                                        movementsRepository
                                                .findByAccountIdOrderByDateAsc(account.getId())
                                                .collectList()
                                                .flatMap(movements -> {
                                                    List<Movement> updatedMovements = updateBalanceForAllMovements(
                                                            account,
                                                            movements,
                                                            movementId,
                                                            movementRequest
                                                    );

                                                    return Flux.fromIterable(updatedMovements)
                                                            .concatMap(movementsRepository::save)
                                                            .then(findById(movementId));
                                                })
                                )
                );
    }

    private List<Movement> updateBalanceForAllMovements(Account account, List<Movement> movements, Long movementId, MovementRequest movementRequest) {
        BigDecimal runningBalance = account.getInitialBalance();

        List<Movement> updatedMovements = new ArrayList<>();
        for (Movement movement : movements) {

            boolean isTarget = movement.getId().equals(movementId);

            BigDecimal value = isTarget ? movementRequest.getValue() : movement.getValue();

            MovementRequest.TypeEnum type = isTarget ? movementRequest.getType() : movement.getType();

            runningBalance = calculateBalance(type, runningBalance, value);

            movement.setBalance(runningBalance);

            if (isTarget) {
                movement.setValue(value);
                movement.setType(type);
            }

            updatedMovements.add(movement);
        }
        return updatedMovements;
    }

    public Flux<MovementResponse> getMovementsReport(
            LocalDate startDate,
            LocalDate endDate,
            Long customerId) {

        LocalDate nowDate = LocalDate.now();

        if (startDate == null) {
            startDate = nowDate;
        }

        if (endDate == null) {
            endDate = nowDate;
        }

        if (startDate.isAfter(endDate)) {
            return Flux.error(new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "The start date cannot be later than the end date"
            ));
        }

        return movementsRepository.findTransactionByDateRangeAndCustomerId(startDate, endDate, customerId)
                .flatMap(this::getMovementResponseFromMovement);
    }

    private Mono<BigDecimal> getAndValidateBalance(MovementRequest.TypeEnum operationType, BigDecimal initialBalance, BigDecimal currentValue) {
        BigDecimal actualBalance = calculateBalance(operationType, initialBalance, currentValue);

        if (actualBalance.compareTo(BigDecimal.ZERO) < 0) {
            Mono.just(new BusinessException(HttpStatus.CONFLICT, "Unavailable balance"));
        }

        return Mono.just(actualBalance);
    }

    private BigDecimal calculateBalance(MovementRequest.TypeEnum operationType, BigDecimal previousBalance, BigDecimal value) {
        return MovementRequest.TypeEnum.WITHDRAWAL.equals(operationType)
                ? previousBalance.subtract(value)
                : previousBalance.add(value);

    }

    private Mono<MovementResponse> getMovementResponseFromMovement(Movement movement) {
        return accountsRepository.findById(movement.getAccountId())
                .flatMap(account ->
                        customerSnapshotRepository.findById(account.getCustomerId())
                                .map(customerSnapshot ->
                                        Movement.fromMovement(
                                                movement,
                                                customerSnapshot.getCustomerName(),
                                                account.getStatus(),
                                                account.getInitialBalance()
                                        )
                                )
                );
    }
}
