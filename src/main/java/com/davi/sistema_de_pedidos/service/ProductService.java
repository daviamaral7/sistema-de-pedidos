package com.davi.sistema_de_pedidos.service;

import com.davi.sistema_de_pedidos.dto.ProductRequestDTO;
import com.davi.sistema_de_pedidos.dto.ProductResponseDTO;
import com.davi.sistema_de_pedidos.model.Product;
import com.davi.sistema_de_pedidos.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    private ProductResponseDTO entityToDTO(Product product) {
        return new ProductResponseDTO(product.getId(), product.getName(), product.getPrice());
    }

    private Product dtoToEntity(ProductRequestDTO dto) {
        return Product.createProduct(dto.name(), dto.price());
    }

    public ProductResponseDTO save(ProductRequestDTO dto) {
        Product product = productRepository.save(dtoToEntity(dto));
        return entityToDTO(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        List<Product> productList = productRepository.findAll();
        return productList.stream().map(this::entityToDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        return entityToDTO(product);
    }

    public ProductResponseDTO update(UUID id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setName(dto.name());
        product.setPrice(dto.price());

        return entityToDTO(product);
    }

    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        productRepository.delete(product);
    }
}
