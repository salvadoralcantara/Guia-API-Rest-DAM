package edu.ues.dam.demo_api_rest.controllers;

import edu.ues.dam.demo_api_rest.dtos.ProductsDTO;
import edu.ues.dam.demo_api_rest.services.ProductServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServices productServices;

    @GetMapping
    public ResponseEntity<List<ProductsDTO>> getAllItems() {
        try {
            List<ProductsDTO> items = productServices.getAllProducts();
            if (items.isEmpty()) {
                return ResponseEntity.status(204).build();
            } else {
                return ResponseEntity.ok(items);
            }
        } catch (Exception e) {
            log.error("{}", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<ProductsDTO> create(@RequestBody ProductsDTO dto) {
        try {
            ProductsDTO created = productServices.createProduct(dto);
            return ResponseEntity.status(201).body(created);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}