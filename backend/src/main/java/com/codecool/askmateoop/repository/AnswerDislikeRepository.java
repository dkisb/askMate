package com.codecool.askmateoop.repository;

import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.entities.ratings.AnswerDislike;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerDislikeRepository extends JpaRepository<AnswerDislike, Integer> {
    boolean existsByAnswerIdAndReviewer(Integer answerId, UserEntity reviewer);
    @Transactional
    void deleteByAnswerIdAndReviewer(Integer answerId, UserEntity reviewer);
}
