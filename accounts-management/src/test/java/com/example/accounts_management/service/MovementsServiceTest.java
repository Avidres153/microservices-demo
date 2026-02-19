package com.example.accounts_management.service;

import com.example.accounts_management.exception.custom.BusinessException;
import com.example.accounts_management.infrastructure.dto.MovementRequest;
import com.example.accounts_management.infrastructure.dto.MovementResponse;
import com.example.accounts_management.model.Account;
import com.example.accounts_management.model.CustomerSnapshot;
import com.example.accounts_management.model.Movement;
import com.example.accounts_management.repository.AccountsRepository;
import com.example.accounts_management.repository.CustomerSnapshotRepository;
import com.example.accounts_management.repository.MovementsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovementsServiceTest {

    @Mock
    private MovementsRepository movementsRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private CustomerSnapshotRepository customerSnapshotRepository;

    @InjectMocks
    private MovementsService movementsService;

    private Movement movement;
    private Account account;
    private CustomerSnapshot customerSnapshot;
    private MovementRequest movementRequest;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setCustomerId(1L);
        account.setInitialBalance(BigDecimal.valueOf(100.0));
        account.setStatus(true);

        customerSnapshot = new CustomerSnapshot();
        customerSnapshot.setId(1L);
        customerSnapshot.setCustomerName("Jose Lema");

        movement = new Movement();
        movement.setId(1L);
        movement.setAccountId(1L);
        movement.setDate(LocalDate.now());
        movement.setType(MovementRequest.TypeEnum.DEPOSIT);
        movement.setValue(BigDecimal.valueOf(50.0));
        movement.setBalance(BigDecimal.valueOf(150.0));

        movementRequest = new MovementRequest();
        movementRequest.setAccountId(1L);
        movementRequest.setType(MovementRequest.TypeEnum.DEPOSIT);
        movementRequest.setValue(BigDecimal.valueOf(50.0));
    }

    @Test
    void findById_Success() {
        when(movementsRepository.findById(1L)).thenReturn(Mono.just(movement));
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));

        StepVerifier.create(movementsService.findById(1L))
                .expectNextMatches(response -> response.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void findById_NotFound() {
        when(movementsRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(movementsService.findById(1L))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void findAll_Success() {
        when(movementsRepository.findAll()).thenReturn(Flux.just(movement));
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));

        StepVerifier.create(movementsService.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void delete_Success() {
        when(movementsRepository.findById(1L)).thenReturn(Mono.just(movement));
        when(movementsRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(movementsService.delete(1L))
                .verifyComplete();
    }

    @Test
    void delete_NotFound() {
        when(movementsRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(movementsService.delete(1L))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void save_Success() {
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(movementsRepository.findByAccountIdOrderByDateDesc(1L)).thenReturn(Flux.just(movement));
        when(movementsRepository.save(any(Movement.class))).thenReturn(Mono.just(movement));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));

        StepVerifier.create(movementsService.save(movementRequest))
                .expectNextMatches(response -> response.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void save_AccountNotFound() {
        when(accountsRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(movementsService.save(movementRequest))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void findAllByAccount_Success() {
        when(movementsRepository.findByAccountIdOrderByDateDesc(1L)).thenReturn(Flux.just(movement));
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));

        StepVerifier.create(movementsService.findAllByAccount(1L))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getMovementsReport_Success() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        when(movementsRepository.findTransactionByDateRangeAndCustomerId(startDate, endDate, 1L)).thenReturn(Flux.just(movement));
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));

        StepVerifier.create(movementsService.getMovementsReport(startDate, endDate, 1L))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getMovementsReport_InvalidDates() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        StepVerifier.create(movementsService.getMovementsReport(startDate, endDate, 1L))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }
}
