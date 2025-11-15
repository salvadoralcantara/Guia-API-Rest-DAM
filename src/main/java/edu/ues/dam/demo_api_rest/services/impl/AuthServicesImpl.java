package edu.ues.dam.demo_api_rest.services.impl;

import edu.ues.dam.demo_api_rest.configs.CustomerDetailServices;
import edu.ues.dam.demo_api_rest.dtos.TokenDTO;
import edu.ues.dam.demo_api_rest.entities.User;
import edu.ues.dam.demo_api_rest.repositories.UserRepository;
import edu.ues.dam.demo_api_rest.security.JwtUtil;
import edu.ues.dam.demo_api_rest.services.AuthServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServicesImpl implements AuthServices {

    private final AuthenticationManager authenticationManager;
    private final CustomerDetailServices customerDetailServices;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public TokenDTO login(String user, String pass) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user, pass)
            );

            if (authentication.isAuthenticated()) {
                UserDetails usuarioDetail = (UserDetails) authentication.getPrincipal();
                if (customerDetailServices.getUserDetail().getActive()) {
                    TokenDTO token = jwtUtil.generateToken(user, usuarioDetail);
                    return token;
                }
            }
        } catch (BadCredentialsException bad) {
            log.warn("Credenciales incorrectas");
            return null;
        } catch (Exception e) {
            log.error("Error auth: {}", e.getMessage());
            return null;
        }
        return null;
    }

    // Método auxiliar para registrar usuarios (usado por el controller)
    @Transactional
    public User registerUser(User u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        u.setActive(true);
        return userRepository.save(u);
    }
}