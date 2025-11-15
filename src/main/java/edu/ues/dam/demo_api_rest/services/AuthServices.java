package edu.ues.dam.demo_api_rest.services;

import edu.ues.dam.demo_api_rest.dtos.TokenDTO;

public interface AuthServices {
    TokenDTO login(String user, String pass);
}