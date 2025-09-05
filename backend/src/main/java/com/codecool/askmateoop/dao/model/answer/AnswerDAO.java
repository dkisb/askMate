package com.codecool.askmateoop.dao.model.answer;

import com.codecool.askmateoop.controller.dto.answer.NewAnswerDTO;

import java.util.List;

public interface AnswerDAO {
    int createNewAnswer(NewAnswerDTO newAnswerDTO);

    boolean deleteAnswer(int id);

    List<Answer> getAllAnswersById(int id);
}
