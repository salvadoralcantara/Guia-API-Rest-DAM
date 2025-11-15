package edu.ues.dam.demo_api_rest.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code")
    private Integer code;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private boolean status;
}