package com.example.accounts_management.service;

import com.example.accounts_management.exception.custom.BusinessException;
import com.example.accounts_management.infrastructure.dto.AccountRequest;
import com.example.accounts_management.infrastructure.dto.AccountResponse;
import com.example.accounts_management.model.Account;
import com.example.accounts_management.repository.AccountsRepository;
import com.example.accounts_management.repository.CustomerSnapshotRepository;
import com.example.accounts_management.repository.MovementsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountsService {

    private final AccountsRepository accountsRepository;
    private final CustomerSnapshotRepository customerSnapshotRepository;
    private final MovementsRepository movementsRepository;

    public AccountsService(AccountsRepository accountsRepository, CustomerSnapshotRepository customerSnapshotRepository, MovementsRepository movementsRepository) {
        this.accountsRepository = accountsRepository;
        this.customerSnapshotRepository = customerSnapshotRepository;
        this.movementsRepository = movementsRepository;
    }

    public Mono<AccountResponse> findById(Long accountId) {
        return accountsRepository.findById(accountId)//Mono<Account>
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Account not found")))
                .flatMap(account ->
                        customerSnapshotRepository.findById(account.getCustomerId()) //Mono<CustomerSnapshot>
                                .flatMap(cs -> Mono.just(Account.fromAccount(account, cs.getCustomerName())))

                );
    }

    public Flux<AccountResponse> findAll() {
        return accountsRepository.findAll()
                .flatMap(account -> customerSnapshotRepository.findById(account.getCustomerId()) //Mono<CustomerSnapshot>
                        .flatMap(cs -> Mono.just(Account.fromAccount(account, cs.getCustomerName()))));
    }

    public Flux<AccountResponse> findByCustomerId(Long customerId) {
        return customerSnapshotRepository.findById(customerId) //Mono<CustomerSnapshot>
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Customer not found")))
                .flatMapMany(customerSnapshot ->
                        accountsRepository
                                .findByCustomerId(customerId) // Flux<Account>
                                .map(account ->
                                        Account.fromAccount(
                                                account,
                                                customerSnapshot.getCustomerName()
                                        )
                                )
                );
    }

    public Mono<Void> deleteById(Long accountId) {
        return findById(accountId)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Account not found")))
                .flatMap(accountResponse ->
                        movementsRepository.findByAccountIdOrderByDateDesc(accountResponse.getId())
                                .hasElements() // Mono<Boolean>
                                .flatMap(hasMovements -> {
                                    if (hasMovements) {
                                        return Mono.error(
                                                new BusinessException(HttpStatus.CONFLICT, "Account has transactions")
                                        );
                                    }
                                    return accountsRepository.deleteById(accountId);
                                })
                );

    }

    public Mono<AccountResponse> save(AccountRequest accountRequest) {
        return customerSnapshotRepository
                .findById(accountRequest.getCustomerId())
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Customer not found")))
                .flatMap(customerSnapshot ->
                        accountsRepository.findByAccountNumber(accountRequest.getAccountNumber()) //Mono<Account>
                                .flatMap(existingAccount ->
                                        Mono.<AccountResponse>error(new BusinessException(
                                                HttpStatus.CONFLICT,
                                                "Account already exists")))
                                .switchIfEmpty(
                                        Mono.defer(() ->
                                                accountsRepository.save(Account.fromAccountRequest(accountRequest))
                                                        .flatMap(savedAccount -> Mono.just(Account.fromAccount(savedAccount, customerSnapshot.getCustomerName())))
                                        )
                                )
                );


        /*return accountsRepository.findByAccountNumber(accountRequest.getAccountNumber()) //Mono<Account>
                .flatMap(existingAccount ->
                        Mono.<AccountResponse>error(new BusinessException(
                                HttpStatus.CONFLICT,
                                "Account already exists")))
                .switchIfEmpty(
                        Mono.defer(() ->
                                accountsRepository
                                        .save(Account.fromAccountRequest(accountRequest))
                                        .flatMap(savedAccount ->
                                                customerSnapshotRepository
                                                        .findById(savedAccount.getCustomerId())
                                                        .switchIfEmpty(Mono.error(
                                                                new BusinessException(
                                                                        HttpStatus.NOT_FOUND,
                                                                        "Customer not found"
                                                                )
                                                        ))
                                                        .map(cs ->
                                                                Account.fromAccount(
                                                                        savedAccount,
                                                                        cs.getCustomerName()
                                                                )
                                                        )
                                        )
                        )
                );*/
    }

    public Mono<AccountResponse> update(Long id, AccountRequest accountRequest) {
        return findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Account not found")))
                .flatMap(accountResponse -> {
                    Account account = Account.fromAccountRequest(accountRequest);
                    account.setId(id);
                    return accountsRepository.save(account)
                            .flatMap(savedAccount ->
                                    customerSnapshotRepository
                                            .findById(savedAccount.getCustomerId())
                                            .switchIfEmpty(Mono.error(
                                                    new BusinessException(
                                                            HttpStatus.NOT_FOUND,
                                                            "Customer not found"
                                                    )
                                            ))
                                            .map(cs ->
                                                    Account.fromAccount(
                                                            savedAccount,
                                                            cs.getCustomerName()
                                                    )
                                            )
                            );
                });
    }
}
