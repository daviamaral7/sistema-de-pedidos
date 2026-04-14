package com.davi.sistema_de_pedidos.controller;

import com.davi.sistema_de_pedidos.dto.CustomerRequestDTO;
import com.davi.sistema_de_pedidos.dto.CustomerResponseDTO;
import com.davi.sistema_de_pedidos.service.CustomerService;
import com.davi.sistema_de_pedidos.util.ControllerUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create (@RequestBody @Valid CustomerRequestDTO dto) {
        CustomerResponseDTO response = customerService.save(dto);
        return ResponseEntity.created(ControllerUtils.createHeaderLocation(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> findAll () {
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<CustomerResponseDTO> findById (@PathVariable UUID id){
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<CustomerResponseDTO> update (@PathVariable UUID id, @RequestBody @Valid CustomerRequestDTO dto) {
        return ResponseEntity.ok(customerService.update(id, dto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete (@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
