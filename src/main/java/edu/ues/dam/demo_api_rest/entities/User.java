package edu.ues.dam.demo_api_rest.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    private Boolean active = true;
}