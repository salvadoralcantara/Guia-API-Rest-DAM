package edu.ues.dam.demo_api_rest.controllers;

import edu.ues.dam.demo_api_rest.dtos.LoginDTO;
import edu.ues.dam.demo_api_rest.dtos.RegisterDTO;
import edu.ues.dam.demo_api_rest.dtos.TokenDTO;
import edu.ues.dam.demo_api_rest.entities.User;
import edu.ues.dam.demo_api_rest.services.AuthServices;
import edu.ues.dam.demo_api_rest.services.impl.AuthServicesImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServices authServices;
    private final AuthServicesImpl authServicesImpl; // to reuse registerUser

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO authRequest) {
        try {
            TokenDTO token = authServices.login(authRequest.getUser(), authRequest.getPass());
            if (token == null) {
                return ResponseEntity.status(401).build();
            } else {
                return ResponseEntity.ok(token);
            }
        } catch (Exception e) {
            log.error("{}", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        try {
            User u = new User();
            u.setName(dto.getName());
            u.setLastName(dto.getLastName());
            u.setEmail(dto.getEmail());
            u.setPassword(dto.getPassword());
            User saved = authServicesImpl.registerUser(u);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            log.error("Error register: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error al crear usuario");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Simple logout: client must discard token. We return OK.
        return ResponseEntity.ok().body("{\"msj\":\"logout ok\"}");
    }
}