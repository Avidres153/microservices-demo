package com.example.accounts_management.model;

import com.example.accounts_management.infrastructure.dto.AccountRequest;
import com.example.accounts_management.infrastructure.dto.AccountResponse;
import com.example.accounts_management.infrastructure.dto.CustomerSnapshotResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "account")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    private Long id;
    private String accountNumber;
    private AccountRequest.AccountTypeEnum accountType;
    private BigDecimal initialBalance;
    private Boolean status;
    private Long customerId;

    public static Account fromAccountRequest(AccountRequest accountRequest) {
        return Account.builder()
                .accountNumber(accountRequest.getAccountNumber())
                .accountType(accountRequest.getAccountType())
                .initialBalance(accountRequest.getInitialBalance())
                .status(accountRequest.getStatus())
                .customerId(accountRequest.getCustomerId())
                .build();

    }

    public static AccountResponse fromAccount(Account account, String customerName){
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().getValue())
                .initialBalance(account.getInitialBalance())
                .status(account.getStatus())
                .customerName(customerName)
                .build();
    }

    public Account setId(Long id) {
        this.id = id;
        return this;
    }
}
