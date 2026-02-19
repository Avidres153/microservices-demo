package com.example.customers_management.model;

import com.example.customers_management.infrastructure.dto.CustomerRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Person {
    private String name;
    private CustomerRequest.GenderEnum gender;
    private String identification;
    private String address;
    private String phone;
}
