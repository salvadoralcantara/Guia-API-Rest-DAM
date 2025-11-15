package edu.ues.dam.demo_api_rest.repositories;

import edu.ues.dam.demo_api_rest.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}