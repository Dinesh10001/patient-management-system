package com.pms.authservice.service;


import com.pms.authservice.model.User;
import com.pms.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public Optional<User> findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
