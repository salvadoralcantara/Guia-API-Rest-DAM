package edu.ues.dam.demo_api_rest.repositories;

import edu.ues.dam.demo_api_rest.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}