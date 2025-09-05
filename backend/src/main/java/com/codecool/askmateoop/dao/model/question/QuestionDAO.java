package com.codecool.askmateoop.dao.model.question;

import com.codecool.askmateoop.controller.dto.question.NewQuestionDTO;

import java.util.List;

public interface QuestionDAO {
    List<Question> getAllQuestions();
    Question getQuestionById(int id);
    int addQuestion(NewQuestionDTO newQuestion);
    boolean deleteQuestion(int id);
}
