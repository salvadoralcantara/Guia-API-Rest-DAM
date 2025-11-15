package edu.ues.dam.demo_api_rest.services.impl;

import edu.ues.dam.demo_api_rest.dtos.ProductsDTO;
import edu.ues.dam.demo_api_rest.entities.Product;
import edu.ues.dam.demo_api_rest.repositories.ProductRepository;
import edu.ues.dam.demo_api_rest.services.ProductServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductsImpl implements ProductServices {

    private final ProductRepository productRepository;

    @Override
    public List<ProductsDTO> getAllProducts() {
        List<ProductsDTO> result = new ArrayList<>();
        List<Product> items = this.productRepository.findAll();
        for (Product item : items) {
            result.add(new ProductsDTO(item.getCode(), item.getName(), item.isStatus()));
        }
        return result;
    }

    @Override
    public ProductsDTO createProduct(ProductsDTO dto) {
        Product p = new Product();
        p.setName(dto.getName());
        p.setStatus(dto.isStatus());
        Product saved = productRepository.save(p);
        return new ProductsDTO(saved.getCode(), saved.getName(), saved.isStatus());
    }
}