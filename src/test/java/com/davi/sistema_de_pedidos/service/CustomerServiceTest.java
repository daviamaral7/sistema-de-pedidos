package com.davi.sistema_de_pedidos.service;

import com.davi.sistema_de_pedidos.dto.CustomerRequestDTO;
import com.davi.sistema_de_pedidos.dto.CustomerResponseDTO;
import com.davi.sistema_de_pedidos.exceptions.EmailInUseException;
import com.davi.sistema_de_pedidos.exceptions.ResourceNotFoundException;
import com.davi.sistema_de_pedidos.model.Customer;
import com.davi.sistema_de_pedidos.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldSaveCustomer() {
        CustomerRequestDTO dto = new CustomerRequestDTO("Maria", "maria@email.com");
        UUID customerId = UUID.randomUUID();

        when(customerRepository.existsByEmail(dto.email())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(customerId);
            return customer;
        });

        CustomerResponseDTO response = customerService.save(dto);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertEquals("Maria", savedCustomer.getName());
        assertEquals("maria@email.com", savedCustomer.getEmail());
        assertEquals(customerId, response.id());
        assertEquals("Maria", response.name());
        assertEquals("maria@email.com", response.email());
    }

    @Test
    void shouldThrowConflictWhenSavingCustomerWithExistingEmail() {
        CustomerRequestDTO dto = new CustomerRequestDTO("Maria", "maria@email.com");
        when(customerRepository.existsByEmail(dto.email())).thenReturn(true);

        EmailInUseException exception = assertThrows(EmailInUseException.class, () -> customerService.save(dto));

        assertEquals("Email already in use", exception.getMessage());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllCustomers() {
        Customer customer1 = buildCustomer(UUID.randomUUID(), "Maria", "maria@email.com");
        Customer customer2 = buildCustomer(UUID.randomUUID(), "Joao", "joao@email.com");
        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));

        List<CustomerResponseDTO> response = customerService.findAll();

        assertEquals(2, response.size());
        assertEquals("Maria", response.getFirst().name());
        assertEquals("joao@email.com", response.get(1).email());
    }

    @Test
    void shouldReturnCustomerById() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerResponseDTO response = customerService.findById(customerId);

        assertEquals(customerId, response.id());
        assertEquals("Maria", response.name());
        assertEquals("maria@email.com", response.email());
    }

    @Test
    void shouldThrowNotFoundWhenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> customerService.findById(customerId));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void shouldUpdateCustomer() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        CustomerRequestDTO dto = new CustomerRequestDTO("Maria Silva", "maria.silva@email.com");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        CustomerResponseDTO response = customerService.update(customerId, dto);

        assertEquals("Maria Silva", customer.getName());
        assertEquals("maria.silva@email.com", customer.getEmail());
        assertEquals(customerId, response.id());
        assertEquals("Maria Silva", response.name());
        assertEquals("maria.silva@email.com", response.email());
    }

    @Test
    void shouldAllowUpdateWhenEmailBelongsToSameCustomer() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        CustomerRequestDTO dto = new CustomerRequestDTO("Maria Silva", "maria@email.com");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail(dto.email())).thenReturn(Optional.of(customer));

        CustomerResponseDTO response = customerService.update(customerId, dto);

        assertEquals("Maria Silva", response.name());
        assertEquals("maria@email.com", response.email());
    }

    @Test
    void shouldThrowConflictWhenUpdatingCustomerWithExistingEmail() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        Customer otherCustomer = buildCustomer(UUID.randomUUID(), "Joao", "joao@email.com");
        CustomerRequestDTO dto = new CustomerRequestDTO("Maria Silva", "joao@email.com");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail(dto.email())).thenReturn(Optional.of(otherCustomer));

        EmailInUseException exception =
                assertThrows(EmailInUseException.class, () -> customerService.update(customerId, dto));

        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonexistentCustomer() {
        UUID customerId = UUID.randomUUID();
        CustomerRequestDTO dto = new CustomerRequestDTO("Maria Silva", "maria@email.com");
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> customerService.update(customerId, dto));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void shouldDeleteCustomer() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).delete(customer);

        customerService.delete(customerId);

        verify(customerRepository).delete(customer);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonexistentCustomer() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> customerService.delete(customerId));

        assertEquals("Customer not found", exception.getMessage());
    }

    private Customer buildCustomer(UUID id, String name, String email) {
        Customer customer = Customer.createCustomer(name, email);
        customer.setId(id);
        assertNotNull(customer);
        return customer;
    }
}
