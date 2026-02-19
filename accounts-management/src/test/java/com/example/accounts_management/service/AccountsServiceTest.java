package com.example.accounts_management.service;

import com.example.accounts_management.exception.custom.BusinessException;
import com.example.accounts_management.infrastructure.dto.AccountRequest;
import com.example.accounts_management.model.Account;
import com.example.accounts_management.model.CustomerSnapshot;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private CustomerSnapshotRepository customerSnapshotRepository;

    @Mock
    private MovementsRepository movementsRepository;

    @InjectMocks
    private AccountsService accountsService;

    private Account account;
    private CustomerSnapshot customerSnapshot;
    private AccountRequest accountRequest;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setCustomerId(1L);
        account.setAccountNumber("123456");
        account.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
        account.setInitialBalance(BigDecimal.valueOf(100.0));
        account.setStatus(true);

        customerSnapshot = new CustomerSnapshot();
        customerSnapshot.setId(1L);
        customerSnapshot.setCustomerName("Jose Lema");

        accountRequest = new AccountRequest();
        accountRequest.setCustomerId(1L);
        accountRequest.setAccountNumber("123456");
        accountRequest.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
        accountRequest.setInitialBalance(BigDecimal.valueOf(100.0));
        accountRequest.setStatus(true);
    }

    @Test
    void findById_Success() {
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));

        StepVerifier.create(accountsService.findById(1L))
                .expectNextMatches(response -> response.getId().equals(1L) && response.getCustomerName().equals("Jose Lema"))
                .verifyComplete();
    }

    @Test
    void findById_NotFound() {
        when(accountsRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(accountsService.findById(1L))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void findAll_Success() {
        when(accountsRepository.findAll()).thenReturn(Flux.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));

        StepVerifier.create(accountsService.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByCustomerId_Success() {
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));
        when(accountsRepository.findByCustomerId(1L)).thenReturn(Flux.just(account));

        StepVerifier.create(accountsService.findByCustomerId(1L))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByCustomerId_NotFound() {
        when(customerSnapshotRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(accountsService.findByCustomerId(1L))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void save_Success() {
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));
        when(accountsRepository.findByAccountNumber(anyString())).thenReturn(Mono.empty());
        when(accountsRepository.save(any(Account.class))).thenReturn(Mono.just(account));

        StepVerifier.create(accountsService.save(accountRequest))
                .expectNextMatches(response -> response.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void save_CustomerNotFound() {
        when(customerSnapshotRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(accountsService.save(accountRequest))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void save_AccountAlreadyExists() {
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));
        when(accountsRepository.findByAccountNumber(anyString())).thenReturn(Mono.just(account));

        StepVerifier.create(accountsService.save(accountRequest))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void update_Success() {
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));
        when(accountsRepository.save(any(Account.class))).thenReturn(Mono.just(account));

        StepVerifier.create(accountsService.update(1L, accountRequest))
                .expectNextMatches(response -> response.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void update_NotFound() {
        when(accountsRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(accountsService.update(1L, accountRequest))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void deleteById_Success() {
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));
        when(movementsRepository.findByAccountIdOrderByDateDesc(1L)).thenReturn(Flux.empty());
        when(accountsRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(accountsService.deleteById(1L))
                .verifyComplete();
    }

    @Test
    void deleteById_NotFound() {
        when(accountsRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(accountsService.deleteById(1L))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void deleteById_HasMovements() {
        when(accountsRepository.findById(1L)).thenReturn(Mono.just(account));
        when(customerSnapshotRepository.findById(1L)).thenReturn(Mono.just(customerSnapshot));
        when(movementsRepository.findByAccountIdOrderByDateDesc(1L)).thenReturn(Flux.just(new com.example.accounts_management.model.Movement()));

        StepVerifier.create(accountsService.deleteById(1L))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }
}
