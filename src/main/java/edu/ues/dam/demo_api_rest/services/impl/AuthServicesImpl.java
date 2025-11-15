package edu.ues.dam.demo_api_rest.services.impl;

import edu.ues.dam.demo_api_rest.configs.CustomerDetailServices;
import edu.ues.dam.demo_api_rest.dtos.RegisterDTO;
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
        log.info("AuthServicesImpl.login() called for user='{}'", user);
        try {
            if (user == null || pass == null) {
                log.warn("AuthServicesImpl.login() -> user or pass is null (user='{}', passNull={})", user, pass==null);
                return null;
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user, pass)
            );

            log.info("AuthServicesImpl.login() -> authentication.isAuthenticated = {}", authentication.isAuthenticated());

            if (authentication.isAuthenticated()) {
                UserDetails usuarioDetail = (UserDetails) authentication.getPrincipal();
                log.info("AuthServicesImpl.login() -> principal username = {}", usuarioDetail.getUsername());

                // getUserDetail may load current logged user — log it
                try {
                    var ud = customerDetailServices.getUserDetail();
                    log.info("AuthServicesImpl.login() -> customerDetailServices.getUserDetail() returned active={}", ud != null ? ud.getActive() : "null");
                } catch (Exception ex) {
                    log.warn("AuthServicesImpl.login() -> customerDetailServices.getUserDetail() threw: {}", ex.toString(), ex);
                }

                if (customerDetailServices.getUserDetail() != null && Boolean.TRUE.equals(customerDetailServices.getUserDetail().getActive())) {
                    TokenDTO token = jwtUtil.generateToken(user, usuarioDetail);
                    log.info("AuthServicesImpl.login() -> token generated length={}", token != null && token.getToken()!=null ? token.getToken().length() : "null");
                    return token;
                } else {
                    log.warn("AuthServicesImpl.login() -> user not active or getUserDetail returned null");
                }
            } else {
                log.warn("AuthServicesImpl.login() -> authentication.isAuthenticated is false");
            }
        } catch (BadCredentialsException bad) {
            log.warn("AuthServicesImpl.login() BadCredentialsException: {}", bad.getMessage());
            return null;
        } catch (Exception e) {
            log.error("AuthServicesImpl.login() exception: ", e);
            return null;
        }
        return null;
    }


    // Implementación del método del servicio para registrar usuarios (usado por el controller)
    @Override
    @Transactional
    public User register(RegisterDTO dto) {
        User u = new User();
        u.setName(dto.getName());
        u.setLastName(dto.getLastName());
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        return registerUser(u);
    }

    // Método auxiliar interno para persistir el usuario (password ya hasheado)
    @Transactional
    protected User registerUser(User u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        u.setActive(true);
        return userRepository.save(u);
    }

}
