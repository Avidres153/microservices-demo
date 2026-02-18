package com.example.accounts_management.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;


@Table(name = "transaction")
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Movement {
    @Id
    private Long id;
    private LocalDate date;
    private String type;
    private BigDecimal value;
    private BigDecimal balance;
    private Long accountId;
}
