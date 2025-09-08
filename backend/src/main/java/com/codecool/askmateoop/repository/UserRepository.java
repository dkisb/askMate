package com.codecool.askmateoop.repository;

import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsernameAndPassword(String username, String password);
    Optional<UserEntity> findByUsername(String username);
    int findReliabilityPointsById(int id);
    Optional<UserEntity> findById(int id);
}

