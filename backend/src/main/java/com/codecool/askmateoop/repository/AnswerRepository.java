package com.codecool.askmateoop.repository;

import com.codecool.askmateoop.model.entities.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    Optional<List<Answer>> getAllByQuestionId(int questionId);
}
