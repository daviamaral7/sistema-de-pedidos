package com.davi.sistema_de_pedidos.service;

import com.davi.sistema_de_pedidos.dto.OrderItemDTO;
import com.davi.sistema_de_pedidos.dto.OrderResponseDTO;
import com.davi.sistema_de_pedidos.dto.OrderRequestDTO;
import com.davi.sistema_de_pedidos.exceptions.OrderStatusException;
import com.davi.sistema_de_pedidos.exceptions.ResourceNotFoundException;
import com.davi.sistema_de_pedidos.model.Customer;
import com.davi.sistema_de_pedidos.model.Order;
import com.davi.sistema_de_pedidos.model.OrderItem;
import com.davi.sistema_de_pedidos.model.Product;
import com.davi.sistema_de_pedidos.model.enums.OrderStatus;
import com.davi.sistema_de_pedidos.repository.CustomerRepository;
import com.davi.sistema_de_pedidos.repository.OrderRepository;
import com.davi.sistema_de_pedidos.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldSaveOrderWithCalculatedTotalAndPurchaseSnapshot() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID notebookId = UUID.randomUUID();
        UUID mouseId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        Product notebook = buildProduct(notebookId, "Notebook", new BigDecimal("3000.00"));
        Product mouse = buildProduct(mouseId, "Mouse", new BigDecimal("50.00"));
        OrderRequestDTO dto = new OrderRequestDTO(customerId, List.of(
                new OrderItemDTO(notebookId, 2),
                new OrderItemDTO(mouseId, 3)
        ));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(notebookId)).thenReturn(Optional.of(notebook));
        when(productRepository.findById(mouseId)).thenReturn(Optional.of(mouse));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });

        OrderResponseDTO response = orderService.save(dto);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(customer, savedOrder.getCustomer());
        assertEquals(OrderStatus.CREATED, savedOrder.getStatus());
        assertNotNull(savedOrder.getOrderDate());
        assertEquals(2, savedOrder.getItems().size());
        assertTrue(savedOrder.getItems().stream().allMatch(item -> item.getOrder() == savedOrder));

        OrderItem firstItem = savedOrder.getItems().getFirst();
        OrderItem secondItem = savedOrder.getItems().get(1);
        assertSame(notebook, firstItem.getProduct());
        assertEquals(2, firstItem.getQuantity());
        assertEquals(new BigDecimal("3000.00"), firstItem.getPriceAtPurchase());
        assertSame(mouse, secondItem.getProduct());
        assertEquals(3, secondItem.getQuantity());
        assertEquals(new BigDecimal("50.00"), secondItem.getPriceAtPurchase());

        assertEquals(orderId, response.orderId());
        assertEquals(customerId, response.customer().id());
        assertEquals("Maria", response.customer().name());
        assertEquals("CREATED", response.orderStatus());
        assertEquals(new BigDecimal("6150.00"), response.total());
        assertEquals(2, response.purchasedItems().size());
        assertEquals("Notebook", response.purchasedItems().getFirst().productName());
        assertEquals(new BigDecimal("3000.00"), response.purchasedItems().getFirst().priceAtPurchase());
    }

    @Test
    void shouldThrowNotFoundWhenSavingOrderForNonexistentCustomer() {
        UUID customerId = UUID.randomUUID();
        OrderRequestDTO dto = new OrderRequestDTO(customerId, List.of(new OrderItemDTO(UUID.randomUUID(), 1)));
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orderService.save(dto));

        assertEquals("Customer not found", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenSavingOrderWithNonexistentProduct() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        OrderRequestDTO dto = new OrderRequestDTO(customerId, List.of(new OrderItemDTO(productId, 1)));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> orderService.save(dto));

        assertEquals("Product not found", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldReturnOrdersByCustomer() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        Order order1 = buildOrder(UUID.randomUUID(), customer, OrderStatus.CREATED, List.of(
                buildOrderItem(buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("3000.00")), 1, new BigDecimal("3000.00"))
        ));
        Order order2 = buildOrder(UUID.randomUUID(), customer, OrderStatus.PAID, List.of(
                buildOrderItem(buildProduct(UUID.randomUUID(), "Mouse", new BigDecimal("50.00")), 2, new BigDecimal("50.00"))
        ));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer)).thenReturn(List.of(order1, order2));

        List<OrderResponseDTO> response = orderService.getOrdersByCustomer(customerId);

        assertEquals(2, response.size());
        assertEquals("CREATED", response.getFirst().orderStatus());
        assertEquals("PAID", response.get(1).orderStatus());
        assertEquals(customerId, response.getFirst().customer().id());
    }

    @Test
    void shouldThrowNotFoundWhenCustomerDoesNotExistWhileListingOrders() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> orderService.getOrdersByCustomer(customerId));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void shouldReturnOrderById() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Product product = buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("3000.00"));
        Customer customer = buildCustomer(customerId, "Maria", "maria@email.com");
        Order order = buildOrder(orderId, customer, OrderStatus.CREATED, List.of(
                buildOrderItem(product, 2, new BigDecimal("3000.00"))
        ));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.getOrderById(orderId);

        assertEquals(orderId, response.orderId());
        assertEquals(customerId, response.customer().id());
        assertEquals("CREATED", response.orderStatus());
        assertEquals(new BigDecimal("6000.00"), response.total());
        assertEquals("Notebook", response.purchasedItems().getFirst().productName());
    }

    @Test
    void shouldThrowNotFoundWhenOrderDoesNotExist() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(orderId));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void shouldPayOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, buildCustomer(UUID.randomUUID(), "Maria", "maria@email.com"),
                OrderStatus.CREATED,
                List.of(buildOrderItem(buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("100.00")), 2, new BigDecimal("100.00"))));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.payOrder(orderId);

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("PAID", response.orderStatus());
        assertEquals(new BigDecimal("200.00"), response.total());
    }

    @Test
    void shouldThrowNotFoundWhenPayingNonexistentOrder() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> orderService.payOrder(orderId));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessErrorWhenPayingAlreadyPaidOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, buildCustomer(UUID.randomUUID(), "Maria", "maria@email.com"),
                OrderStatus.PAID,
                List.of(buildOrderItem(buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("100.00")), 1, new BigDecimal("100.00"))));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderStatusException exception =
                assertThrows(OrderStatusException.class, () -> orderService.payOrder(orderId));

        assertEquals("Order already closed", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessErrorWhenPayingCancelledOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, buildCustomer(UUID.randomUUID(), "Maria", "maria@email.com"),
                OrderStatus.CANCELLED,
                List.of(buildOrderItem(buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("100.00")), 1, new BigDecimal("100.00"))));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderStatusException exception =
                assertThrows(OrderStatusException.class, () -> orderService.payOrder(orderId));

        assertEquals("Order already closed", exception.getMessage());
    }

    @Test
    void shouldCancelOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, buildCustomer(UUID.randomUUID(), "Maria", "maria@email.com"),
                OrderStatus.CREATED,
                List.of(buildOrderItem(buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("100.00")), 1, new BigDecimal("100.00"))));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals("CANCELLED", response.orderStatus());
    }

    @Test
    void shouldThrowNotFoundWhenCancellingNonexistentOrder() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(orderId));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessErrorWhenCancellingAlreadyPaidOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, buildCustomer(UUID.randomUUID(), "Maria", "maria@email.com"),
                OrderStatus.PAID,
                List.of(buildOrderItem(buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("100.00")), 1, new BigDecimal("100.00"))));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderStatusException exception =
                assertThrows(OrderStatusException.class, () -> orderService.cancelOrder(orderId));

        assertEquals("Order already closed", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessErrorWhenCancellingAlreadyCancelledOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, buildCustomer(UUID.randomUUID(), "Maria", "maria@email.com"),
                OrderStatus.CANCELLED,
                List.of(buildOrderItem(buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("100.00")), 1, new BigDecimal("100.00"))));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderStatusException exception =
                assertThrows(OrderStatusException.class, () -> orderService.cancelOrder(orderId));

        assertEquals("Order already closed", exception.getMessage());
    }

    private Customer buildCustomer(UUID id, String name, String email) {
        Customer customer = Customer.createCustomer(name, email);
        customer.setId(id);
        return customer;
    }

    private Product buildProduct(UUID id, String name, BigDecimal price) {
        Product product = Product.createProduct(name, price);
        product.setId(id);
        return product;
    }

    private OrderItem buildOrderItem(Product product, int quantity, BigDecimal priceAtPurchase) {
        OrderItem orderItem = OrderItem.createOrderItem(product, quantity, priceAtPurchase);
        orderItem.setId(UUID.randomUUID());
        return orderItem;
    }

    private Order buildOrder(UUID id, Customer customer, OrderStatus status, List<OrderItem> items) {
        Order order = Order.createOrder(customer, items);
        order.setId(id);
        order.setOrderDate(LocalDateTime.of(2026, 4, 23, 10, 30));
        order.setStatus(status);
        return order;
    }
}
