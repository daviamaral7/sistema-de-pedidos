package com.davi.sistema_de_pedidos.service;

import com.davi.sistema_de_pedidos.dto.ProductRequestDTO;
import com.davi.sistema_de_pedidos.dto.ProductResponseDTO;
import com.davi.sistema_de_pedidos.exceptions.ResourceNotFoundException;
import com.davi.sistema_de_pedidos.model.Product;
import com.davi.sistema_de_pedidos.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldSaveProduct() {
        ProductRequestDTO dto = new ProductRequestDTO("Notebook", new BigDecimal("4999.90"));
        UUID productId = UUID.randomUUID();

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(productId);
            return product;
        });

        ProductResponseDTO response = productService.save(dto);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertEquals("Notebook", savedProduct.getName());
        assertEquals(new BigDecimal("4999.90"), savedProduct.getPrice());
        assertEquals(productId, response.id());
        assertEquals("Notebook", response.name());
        assertEquals(new BigDecimal("4999.90"), response.price());
    }

    @Test
    void shouldReturnAllProducts() {
        Product product1 = buildProduct(UUID.randomUUID(), "Notebook", new BigDecimal("4999.90"));
        Product product2 = buildProduct(UUID.randomUUID(), "Mouse", new BigDecimal("99.90"));
        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        List<ProductResponseDTO> response = productService.findAll();

        assertEquals(2, response.size());
        assertEquals("Notebook", response.getFirst().name());
        assertEquals(new BigDecimal("99.90"), response.get(1).price());
    }

    @Test
    void shouldReturnProductById() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, "Mouse", new BigDecimal("99.90"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponseDTO response = productService.getById(productId);

        assertEquals(productId, response.id());
        assertEquals("Mouse", response.name());
        assertEquals(new BigDecimal("99.90"), response.price());
    }

    @Test
    void shouldThrowNotFoundWhenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> productService.getById(productId));

        assertEquals("Product Not Found", exception.getMessage());
    }

    @Test
    void shouldUpdateProduct() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, "Mouse", new BigDecimal("99.90"));
        ProductRequestDTO dto = new ProductRequestDTO("Teclado", new BigDecimal("199.90"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponseDTO response = productService.update(productId, dto);

        assertEquals("Teclado", product.getName());
        assertEquals(new BigDecimal("199.90"), product.getPrice());
        assertEquals(productId, response.id());
        assertEquals("Teclado", response.name());
        assertEquals(new BigDecimal("199.90"), response.price());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonexistentProduct() {
        UUID productId = UUID.randomUUID();
        ProductRequestDTO dto = new ProductRequestDTO("Teclado", new BigDecimal("199.90"));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> productService.update(productId, dto));

        assertEquals("Product Not Found", exception.getMessage());
    }

    @Test
    void shouldDeleteProduct() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, "Mouse", new BigDecimal("99.90"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.delete(productId);

        verify(productRepository).delete(product);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonexistentProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> productService.delete(productId));

        assertEquals("Product Not Found", exception.getMessage());
    }

    private Product buildProduct(UUID id, String name, BigDecimal price) {
        Product product = Product.createProduct(name, price);
        product.setId(id);
        return product;
    }
}
