package com.davi.sistema_de_pedidos.controller;

import com.davi.sistema_de_pedidos.dto.ProductRequestDTO;
import com.davi.sistema_de_pedidos.dto.ProductResponseDTO;
import com.davi.sistema_de_pedidos.service.ProductService;
import com.davi.sistema_de_pedidos.util.ControllerUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody @Valid ProductRequestDTO dto) {
        ProductResponseDTO response = productService.save(dto);
        return ResponseEntity.created(ControllerUtils.createHeaderLocation(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductResponseDTO> getProductById (@PathVariable UUID id){
        return ResponseEntity.ok(productService.getById(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductResponseDTO> update (@PathVariable UUID id, @RequestBody @Valid ProductRequestDTO dto){
        ProductResponseDTO response = productService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete (@PathVariable UUID id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
