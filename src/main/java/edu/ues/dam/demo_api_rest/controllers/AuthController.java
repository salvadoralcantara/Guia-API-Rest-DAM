package edu.ues.dam.demo_api_rest.controllers;

import edu.ues.dam.demo_api_rest.dtos.LoginDTO;
import edu.ues.dam.demo_api_rest.dtos.RegisterDTO;
import edu.ues.dam.demo_api_rest.dtos.TokenDTO;
import edu.ues.dam.demo_api_rest.entities.User;
import edu.ues.dam.demo_api_rest.services.AuthServices;
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

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO authRequest) {
        log.info("AuthController.login() called with user='{}'", authRequest != null ? authRequest.getUser() : "null");
        try {
            TokenDTO token = authServices.login(authRequest.getUser(), authRequest.getPass());
            if (token == null) {
                log.warn("AuthController.login() -> authServices.login returned null (bad credentials or inactive).");
                return ResponseEntity.status(401).build();
            } else {
                log.info("AuthController.login() -> token generated, returning 200");
                return ResponseEntity.ok(token);
            }
        } catch (Exception e) {
            log.error("AuthController.login() exception: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        log.info("AuthController.register() called for email='{}'", dto != null ? dto.getEmail() : "null");
        try {
            User saved = authServices.register(dto);
            log.info("AuthController.register() -> user saved id={}", saved != null ? saved.getId() : "null");
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            log.error("Error register: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error al crear usuario");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        log.info("AuthController.logout() called");
        return ResponseEntity.ok().body("{\"msj\":\"logout ok\"}");
    }
}
