package edu.ues.dam.demo_api_rest.configs;

import edu.ues.dam.demo_api_rest.entities.User;
import edu.ues.dam.demo_api_rest.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomerDetailServices implements UserDetailsService {

    private final UserRepository userRepo;
    private User userDetail;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        userDetail = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        try {
            return new org.springframework.security.core.userdetails.User(
                    userDetail.getEmail(),
                    userDetail.getPassword(),
                    new ArrayList<>()
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("Error al cargar usuario");
        }
    }

    public User getUserDetail() {
        return userDetail;
    }
}