package com.davi.sistema_de_pedidos.controller;

import com.davi.sistema_de_pedidos.dto.CustomerSummaryDTO;
import com.davi.sistema_de_pedidos.dto.OrderItemResponseDTO;
import com.davi.sistema_de_pedidos.dto.OrderResponseDTO;
import com.davi.sistema_de_pedidos.exceptions.GlobalExceptionHandler;
import com.davi.sistema_de_pedidos.exceptions.OrderStatusException;
import com.davi.sistema_de_pedidos.exceptions.ResourceNotFoundException;
import com.davi.sistema_de_pedidos.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderResponseDTO response = buildOrderResponse(orderId, customerId, productId, "CREATED");

        when(orderService.save(any())).thenReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "items": [
                                    {
                                      "productId": "%s",
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """.formatted(customerId, productId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/orders/" + orderId))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.customer.id").value(customerId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("CREATED"))
                .andExpect(jsonPath("$.purchasedItems[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.total").value(199.80));
    }

    @Test
    void shouldReturnOrdersByCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        List<OrderResponseDTO> response = List.of(
                buildOrderResponse(UUID.randomUUID(), customerId, UUID.randomUUID(), "CREATED"),
                buildOrderResponse(UUID.randomUUID(), customerId, UUID.randomUUID(), "PAID")
        );

        when(orderService.getOrdersByCustomer(customerId)).thenReturn(response);

        mockMvc.perform(get("/orders/customer/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customer.id").value(customerId.toString()))
                .andExpect(jsonPath("$[1].orderStatus").value("PAID"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerHasNoOrdersBecauseCustomerDoesNotExist() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(orderService.getOrdersByCustomer(customerId))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/orders/customer/{id}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found"))
                .andExpect(jsonPath("$.path").value("/orders/customer/" + customerId));
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderResponseDTO response = buildOrderResponse(orderId, customerId, productId, "CREATED");

        when(orderService.getOrderById(orderId)).thenReturn(response);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.customer.name").value("Maria"))
                .andExpect(jsonPath("$.purchasedItems[0].productName").value("Notebook"));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.getOrderById(orderId))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found"))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId));
    }

    @Test
    void shouldPayOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO response = buildOrderResponse(orderId, UUID.randomUUID(), UUID.randomUUID(), "PAID");

        when(orderService.payOrder(orderId)).thenReturn(response);

        mockMvc.perform(put("/orders/{id}/pay", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("PAID"));
    }

    @Test
    void shouldReturnNotFoundWhenPayingNonexistentOrder() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.payOrder(orderId))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(put("/orders/{id}/pay", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found"))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId + "/pay"));
    }

    @Test
    void shouldReturnConflictWhenPayingOrderThatIsAlreadyPaidOrCancelled() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.payOrder(orderId))
                .thenThrow(new OrderStatusException("Order already closed"));

        mockMvc.perform(put("/orders/{id}/pay", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Invalid order state"))
                .andExpect(jsonPath("$.message").value("Order already closed"))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId + "/pay"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO response = buildOrderResponse(orderId, UUID.randomUUID(), UUID.randomUUID(), "CANCELLED");

        when(orderService.cancelOrder(orderId)).thenReturn(response);

        mockMvc.perform(put("/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("CANCELLED"));
    }

    @Test
    void shouldReturnNotFoundWhenCancellingNonexistentOrder() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.cancelOrder(orderId))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(put("/orders/{id}/cancel", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found"))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId + "/cancel"));
    }

    @Test
    void shouldReturnConflictWhenCancellingOrderThatIsAlreadyCancelledOrPaid() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.cancelOrder(orderId))
                .thenThrow(new OrderStatusException("Order already closed"));

        mockMvc.perform(put("/orders/{id}/cancel", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Invalid order state"))
                .andExpect(jsonPath("$.message").value("Order already closed"))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId + "/cancel"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateOrderPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": null,
                                  "items": [
                                    {
                                      "productId": null,
                                      "quantity": 0
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/orders"))
                .andExpect(jsonPath("$.errors", hasItem("customerId: Customer id is required")))
                .andExpect(jsonPath("$.errors", hasItem("items[0].productId: Product id is required")))
                .andExpect(jsonPath("$.errors", hasItem("items[0].quantity: Quantity must be greater than 0")));
    }

    private OrderResponseDTO buildOrderResponse(UUID orderId, UUID customerId, UUID productId, String status) {
        return new OrderResponseDTO(
                orderId,
                new CustomerSummaryDTO(customerId, "Maria"),
                LocalDateTime.of(2026, 4, 23, 10, 30),
                status,
                new BigDecimal("199.80"),
                List.of(new OrderItemResponseDTO(productId, "Notebook", 2, new BigDecimal("99.90")))
        );
    }
}
