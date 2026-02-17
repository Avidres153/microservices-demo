package com.example.customers_management.model;

import com.example.customers_management.infrastructure.dto.CustomerRequest;
import com.example.customers_management.infrastructure.dto.CustomerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "customer")
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Customer extends Person {

    @Id
    private Long id;
    private String password;
    private Boolean status;

    public static Customer fromCustomerRequest(CustomerRequest customerRequest){

        return Customer.builder()
                .name(customerRequest.getName())
                .gender(customerRequest.getGender())
                .identification(customerRequest.getIdentification())
                .address(customerRequest.getAddress())
                .phone(customerRequest.getPhone())
                .password(customerRequest.getPassword())
                .status(customerRequest.getStatus())
                .build();
    }

    public static CustomerResponse toCustomerResponse(Customer customer){
        CustomerResponse customerResponse = new CustomerResponse();
        customerResponse.setId(customer.getId());
        customerResponse.setName(customer.getName());
        customerResponse.setAddress(customer.getAddress());
        customerResponse.setPhone(customer.getPhone());
        customerResponse.setPassword(customer.getPassword());
        customerResponse.setStatus(customer.getStatus());

        return customerResponse;
    }

    public Customer setId(Long id) {
        this.id = id;
        return this;
    }
}
