package edu.ues.dam.demo_api_rest.services;

import edu.ues.dam.demo_api_rest.dtos.RegisterDTO;
import edu.ues.dam.demo_api_rest.dtos.TokenDTO;
import edu.ues.dam.demo_api_rest.entities.User;

public interface AuthServices {
    TokenDTO login(String user, String pass);
    User register(RegisterDTO dto);
}
