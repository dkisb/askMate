package com.codecool.askmateoop.repository;

import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.entities.ratings.QuestionDislike;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionDislikeRepository extends JpaRepository<QuestionDislike, Integer> {
    boolean existsByQuestionIdAndReviewer(Integer questionId, UserEntity reviewer);
    @Transactional
    void deleteByQuestionIdAndReviewer(Integer questionId, UserEntity reviewer);
}
