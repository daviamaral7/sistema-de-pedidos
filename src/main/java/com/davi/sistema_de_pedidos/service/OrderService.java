package com.davi.sistema_de_pedidos.service;

import com.davi.sistema_de_pedidos.dto.*;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    private Order findOrderByIdOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private Product findProductByIdOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Customer findCustomerByIdOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private List<OrderItem> orderItemDtoToEntity(List<OrderItemDTO> dto) {
        return dto.stream()
                .map(orderItemDTO -> {
                    Product p = findProductByIdOrThrow(orderItemDTO.productId());
                    return OrderItem.createOrderItem(p, orderItemDTO.quantity(), p.getPrice());
                })
                .toList();
    }

    private OrderResponseDTO orderEntityToDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                new CustomerSummaryDTO(order.getCustomer().getId(), order.getCustomer().getName()),
                order.getOrderDate(),
                order.getStatus().toString(),
                order.calculateTotal(),
                order.getItems().stream().map(o ->
                        new OrderItemResponseDTO(
                                o.getProduct().getId(),
                                o.getProduct().getName(),
                                o.getQuantity(),
                                o.getPriceAtPurchase())).toList());
    }


    public OrderResponseDTO save(OrderRequestDTO dto) {
        Customer customer = findCustomerByIdOrThrow(dto.customerId());

        Order order = Order.createOrder(customer, orderItemDtoToEntity(dto.items()));

        return orderEntityToDTO(orderRepository.save(order));
    }

    public List<OrderResponseDTO> getOrdersByCustomer(UUID id) {
        Customer customer = findCustomerByIdOrThrow(id);
        List<Order> orders = orderRepository.findByCustomer(customer);
        return orders.stream().map(this::orderEntityToDTO).toList();
    }

    public OrderResponseDTO getOrderById(UUID id) {
        return orderEntityToDTO(findOrderByIdOrThrow(id));
    }

    public OrderResponseDTO payOrder(UUID id) {
        Order order = findOrderByIdOrThrow(id);
        if (!order.getStatus().equals(OrderStatus.CREATED)) {
            throw new OrderStatusException("Order already closed");
        }
        order.setStatus(OrderStatus.PAID);
        return orderEntityToDTO(order);
    }

    public OrderResponseDTO cancelOrder(UUID id) {
        Order order = findOrderByIdOrThrow(id);
        if (!order.getStatus().equals(OrderStatus.CREATED)) {
            throw new OrderStatusException("Order already closed");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderEntityToDTO(order);
    }
}
