package com.codecool.askmateoop.service;

import com.codecool.askmateoop.controller.dto.question.NewQuestionDTO;
import com.codecool.askmateoop.controller.dto.question.QuestionDTO;
import com.codecool.askmateoop.dao.model.question.QuestionDAO;
import com.codecool.askmateoop.dao.model.question.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class QuestionService {

    private final QuestionDAO questionDAO;

    @Autowired
    public QuestionService(QuestionDAO questionDAO) {
        this.questionDAO = questionDAO;
    }

    public List<QuestionDTO> getAllQuestions() {
        List<Question> allQuestions = questionDAO.getAllQuestions();
        return allQuestions.stream().map(q -> new QuestionDTO(
                q.getId(),
                q.getTitle(),
                q.getContent(),
                q.getCreatedAt()
        ))
                .toList();
    }

    public QuestionDTO getQuestionById(int id) {
        Question question = questionDAO.getQuestionById(id);
        return new QuestionDTO(question.getId(), question.getTitle(), question.getContent(), question.getCreatedAt());
    }

    public boolean deleteQuestionById(int id) {
        return questionDAO.deleteQuestion(id);
    }

    public int addNewQuestion(NewQuestionDTO newQuestion) {
        return questionDAO.addQuestion(newQuestion);
    }
}
