package edu.ues.dam.demo_api_rest.services;

import edu.ues.dam.demo_api_rest.dtos.ProductsDTO;

import java.util.List;

public interface ProductServices {
    List<ProductsDTO> getAllProducts();
    ProductsDTO createProduct(ProductsDTO dto);
}