package edu.ues.dam.demo_api_rest.dtos;

import lombok.Data;

@Data
public class RegisterDTO {
    private String name;
    private String lastName;
    private String email;
    private String password;
}