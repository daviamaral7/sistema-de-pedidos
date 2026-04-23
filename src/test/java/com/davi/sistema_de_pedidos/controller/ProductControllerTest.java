package com.davi.sistema_de_pedidos.controller;

import com.davi.sistema_de_pedidos.dto.ProductResponseDTO;
import com.davi.sistema_de_pedidos.exceptions.GlobalExceptionHandler;
import com.davi.sistema_de_pedidos.exceptions.ResourceNotFoundException;
import com.davi.sistema_de_pedidos.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO response = new ProductResponseDTO(productId, "Notebook", new BigDecimal("4999.90"));

        when(productService.save(any())).thenReturn(response);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Notebook",
                                  "price": 4999.90
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/products/" + productId))
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Notebook"))
                .andExpect(jsonPath("$.price").value(4999.90));
    }

    @Test
    void shouldReturnAllProducts() throws Exception {
        List<ProductResponseDTO> response = List.of(
                new ProductResponseDTO(UUID.randomUUID(), "Notebook", new BigDecimal("4999.90")),
                new ProductResponseDTO(UUID.randomUUID(), "Mouse", new BigDecimal("99.90"))
        );

        when(productService.findAll()).thenReturn(response);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Notebook"))
                .andExpect(jsonPath("$[1].price").value(99.90));
    }

    @Test
    void shouldReturnProductById() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO response = new ProductResponseDTO(productId, "Mouse", new BigDecimal("99.90"));

        when(productService.getById(productId)).thenReturn(response);

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Mouse"))
                .andExpect(jsonPath("$.price").value(99.90));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        UUID productId = UUID.randomUUID();

        when(productService.getById(productId))
                .thenThrow(new ResourceNotFoundException("Product Not Found"));

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product Not Found"))
                .andExpect(jsonPath("$.path").value("/products/" + productId));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO response = new ProductResponseDTO(productId, "Teclado", new BigDecimal("199.90"));

        when(productService.update(eq(productId), any())).thenReturn(response);

        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Teclado",
                                  "price": 199.90
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Teclado"))
                .andExpect(jsonPath("$.price").value(199.90));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentProduct() throws Exception {
        UUID productId = UUID.randomUUID();

        when(productService.update(eq(productId), any()))
                .thenThrow(new ResourceNotFoundException("Product Not Found"));

        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Teclado",
                                  "price": 199.90
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product Not Found"))
                .andExpect(jsonPath("$.path").value("/products/" + productId));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        doNothing().when(productService).delete(productId);

        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService).delete(productId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonexistentProduct() throws Exception {
        UUID productId = UUID.randomUUID();

        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Product Not Found"))
                .when(productService).delete(productId);

        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product Not Found"))
                .andExpect(jsonPath("$.path").value("/products/" + productId));
    }

    @Test
    void shouldReturnBadRequestWhenCreateProductPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "price": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/products"))
                .andExpect(jsonPath("$.errors", hasItem("name: Name is required")))
                .andExpect(jsonPath("$.errors", hasItem("price: Price must be greater than 0")));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateProductPayloadIsInvalid() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "price": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/products/" + productId))
                .andExpect(jsonPath("$.errors", hasItem("name: Name is required")))
                .andExpect(jsonPath("$.errors", hasItem("price: Price must be greater than 0")));
    }
}
