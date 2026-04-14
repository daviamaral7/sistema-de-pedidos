package com.davi.sistema_de_pedidos.service;

import com.davi.sistema_de_pedidos.dto.CustomerRequestDTO;
import com.davi.sistema_de_pedidos.dto.CustomerResponseDTO;
import com.davi.sistema_de_pedidos.exceptions.EmailInUseException;
import com.davi.sistema_de_pedidos.exceptions.ResourceNotFoundException;
import com.davi.sistema_de_pedidos.model.Customer;
import com.davi.sistema_de_pedidos.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    private CustomerResponseDTO entityToDTO(Customer customer) {
        return new CustomerResponseDTO(customer.getId(), customer.getName(), customer.getEmail());
    }

    private Customer dtoToEntity(CustomerRequestDTO dto) {
        return Customer.createCustomer(dto.name(), dto.email());
    }

    private Customer findByIdOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    public CustomerResponseDTO save(CustomerRequestDTO dto) {
        Boolean chkeml = customerRepository.existsByEmail(dto.email());

        if (chkeml) {
            throw new EmailInUseException("Email already in use");
        }

        return entityToDTO(customerRepository.save(dtoToEntity(dto)));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> findAll() {
        return customerRepository.findAll().stream().map(this::entityToDTO).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO findById(UUID id) {
        Customer customer = findByIdOrThrow(id);

        return entityToDTO(customer);
    }

    public CustomerResponseDTO update(UUID id, CustomerRequestDTO dto) {
        Customer customer = findByIdOrThrow(id);
        Optional<Customer> existingCustomerByEmail = customerRepository.findByEmail(dto.email());

        if (existingCustomerByEmail.isPresent() && !customer.getId().equals(existingCustomerByEmail.get().getId())) {
            throw new EmailInUseException("Email already in use");
        }

        customer.setName(dto.name());
        customer.setEmail(dto.email());

        return entityToDTO(customer);
    }

    public void delete(UUID id) {
        Customer customer = findByIdOrThrow(id);

        customerRepository.delete(customer);
    }
}
