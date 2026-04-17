package com.davi.sistema_de_pedidos.controller;

import com.davi.sistema_de_pedidos.dto.OrderRequestDTO;
import com.davi.sistema_de_pedidos.dto.OrderResponseDTO;
import com.davi.sistema_de_pedidos.service.OrderService;
import com.davi.sistema_de_pedidos.util.ControllerUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody @Valid OrderRequestDTO dto) {
        OrderResponseDTO response = orderService.save(dto);
        return ResponseEntity.created(ControllerUtils.createHeaderLocation(response.orderId())).body(response);
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<List<OrderResponseDTO>> getAllCustomersOrders(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrdersByCostumer(id));
    }

    @GetMapping("{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("{id}/pay")
    public ResponseEntity<OrderResponseDTO> payOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.payOrder(id));
    }

    @PutMapping("{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
