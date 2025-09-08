package com.codecool.askmateoop.service;

import com.codecool.askmateoop.controller.dto.user.LoginDTO;
import com.codecool.askmateoop.controller.dto.user.PointsDTO;
import com.codecool.askmateoop.model.entities.Role;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.codecool.askmateoop.controller.dto.user.NewUserDTO;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginDTO loginUser(String name, String password) {
        UserEntity user = userRepository.findByUsernameAndPassword(name, password)
                .orElseThrow(() -> new IllegalArgumentException("Wrong username or password"));

        return new LoginDTO(user.getUsername(), user.getId());
    }

    public int getReliabilityLevel(int id) {
        return userRepository.findReliabilityPointsById(id);
    }

    public boolean addNewUser(NewUserDTO newUser) {
        Optional<UserEntity> user = userRepository.findByUsername(newUser.username());
        if (user.isPresent()) {
            throw new IllegalArgumentException("User already exists");
        } else {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(newUser.username());
            userEntity.setPassword(newUser.password());
            userEntity.setEmail(newUser.email());
            userEntity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            userEntity.setReliabilityPoints(0);
            userEntity.setRoles(Set.of(Role.ROLE_USER));
            userRepository.save(userEntity);
        }
        return true;
    }

    public Map<String, String> addNewPoints(PointsDTO pointsDTO) {
        Optional<UserEntity> user = userRepository.findById(pointsDTO.userId());
        if (user.isEmpty()) {
            return Map.of("message", "User not found");
        }
        UserEntity userEntity = user.get();
        userEntity.setReliabilityPoints(userEntity.getReliabilityPoints() + pointsDTO.points());
        userRepository.save(userEntity);
        return Map.of("message", "Points added successfully");
    }
}
