package com.davi.sistema_de_pedidos.controller;

import com.davi.sistema_de_pedidos.dto.CustomerResponseDTO;
import com.davi.sistema_de_pedidos.exceptions.EmailInUseException;
import com.davi.sistema_de_pedidos.exceptions.GlobalExceptionHandler;
import com.davi.sistema_de_pedidos.exceptions.ResourceNotFoundException;
import com.davi.sistema_de_pedidos.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerResponseDTO response = new CustomerResponseDTO(customerId, "Maria", "maria@email.com");

        when(customerService.save(any())).thenReturn(response);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria",
                                  "email": "maria@email.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/customers/" + customerId))
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    void shouldReturnAllCustomers() throws Exception {
        List<CustomerResponseDTO> response = List.of(
                new CustomerResponseDTO(UUID.randomUUID(), "Maria", "maria@email.com"),
                new CustomerResponseDTO(UUID.randomUUID(), "Joao", "joao@email.com")
        );

        when(customerService.findAll()).thenReturn(response);

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Maria"))
                .andExpect(jsonPath("$[1].email").value("joao@email.com"));
    }

    @Test
    void shouldReturnCustomerById() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerResponseDTO response = new CustomerResponseDTO(customerId, "Maria", "maria@email.com");

        when(customerService.findById(customerId)).thenReturn(response);

        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("Maria"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(customerService.findById(customerId)).thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.path").value("/customers/" + customerId));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerResponseDTO response = new CustomerResponseDTO(customerId, "Maria Silva", "maria.silva@email.com");

        when(customerService.update(eq(customerId), any())).thenReturn(response);

        mockMvc.perform(put("/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria.silva@email.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria.silva@email.com"));
    }

    @Test
    void shouldReturnConflictWhenCreatingCustomerWithExistingEmail() throws Exception {
        when(customerService.save(any()))
                .thenThrow(new EmailInUseException("Email already in use"));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria",
                                  "email": "maria@email.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Email Conflict"))
                .andExpect(jsonPath("$.message").value("Email already in use"))
                .andExpect(jsonPath("$.path").value("/customers"));
    }

    @Test
    void shouldReturnConflictWhenUpdatingCustomerWithExistingEmail() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(customerService.update(eq(customerId), any()))
                .thenThrow(new EmailInUseException("Email already in use"));

        mockMvc.perform(put("/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria@email.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Email Conflict"))
                .andExpect(jsonPath("$.message").value("Email already in use"))
                .andExpect(jsonPath("$.path").value("/customers/" + customerId));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(customerService.update(eq(customerId), any()))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(put("/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria.silva@email.com"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.path").value("/customers/" + customerId));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        doNothing().when(customerService).delete(customerId);

        mockMvc.perform(delete("/customers/{id}", customerId))
                .andExpect(status().isNoContent());

        verify(customerService).delete(customerId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonexistentCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();

        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Customer not found"))
                .when(customerService).delete(customerId);

        mockMvc.perform(delete("/customers/{id}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.path").value("/customers/" + customerId));
    }

    @Test
    void shouldReturnBadRequestWhenCreateCustomerPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "email-invalido"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/customers"))
                .andExpect(jsonPath("$.errors", hasItem("name: Name is required")))
                .andExpect(jsonPath("$.errors", hasItem("email: Email not valid")));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateCustomerPayloadIsInvalid() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(put("/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "email-invalido"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/customers/" + customerId))
                .andExpect(jsonPath("$.errors", hasItem("name: Name is required")))
                .andExpect(jsonPath("$.errors", hasItem("email: Email not valid")));
    }
}
