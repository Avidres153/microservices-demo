package com.example.accounts_management.model;

import com.example.accounts_management.infrastructure.dto.MovementRequest;
import com.example.accounts_management.infrastructure.dto.MovementResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;


@Table(name = "movement")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Movement {
    @Id
    private Long id;
    @Column("movement_date")
    private LocalDate date;
    @Column("movement_type")
    private MovementRequest.TypeEnum type;
    private BigDecimal value;
    private BigDecimal balance;
    private Long accountId;

    public static Movement fromMovementRequest(MovementRequest movementRequest, BigDecimal balance) {
        return Movement.builder()
                .date(movementRequest.getDate())
                .type(movementRequest.getType())
                .value(movementRequest.getValue())
                .balance(balance)
                .accountId(movementRequest.getAccountId())
                .build();
    }

    public static MovementResponse fromMovement(Movement movement, String customerName, Boolean status, BigDecimal initialBalance){
        return MovementResponse.builder()
                .id(movement.getId())
                .date(movement.getDate())
                .type(movement.getType().getValue())
                .value(movement.getValue())
                .balance(movement.getBalance())
                .customer(customerName)
                .accountNumber(movement.getAccountId().toString())
                .status(status)
                .initialBalance(initialBalance)
                .build();
    }

    public static MovementResponse fromMovementToReport(Movement movement){
        return MovementResponse.builder()
                .date(movement.getDate())
                .type(movement.getType().getValue())
                .value(movement.getValue())
                .balance(movement.getBalance())
                .accountNumber(movement.getAccountId().toString())
                .build();
    }

    /*public Movement setId(Long id){
        this.id = id;
        return this;
    }

    public Movement setBalance(BigDecimal balance){
        this.balance = balance;
        return this;
    }

    public Movement setValue(BigDecimal value){
        this.value = value;
        return this;
    }

    public Movement setType()*/
}
