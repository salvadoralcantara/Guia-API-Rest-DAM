package edu.ues.dam.demo_api_rest.dtos;

import lombok.Data;

@Data
public class TokenDTO {
    private String token;
    private String expireIn;
    private String msj;
}