package com.codecool.askmateoop.repository;

import com.codecool.askmateoop.model.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Optional<Question> findQuestionById(int id);
    void deleteQuestionById(int id);
    void deleteById(int id);
}