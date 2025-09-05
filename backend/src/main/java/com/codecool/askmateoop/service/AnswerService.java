package com.codecool.askmateoop.service;

import com.codecool.askmateoop.controller.dto.answer.AnswerDTO;
import com.codecool.askmateoop.controller.dto.answer.NewAnswerDTO;
import com.codecool.askmateoop.dao.model.answer.Answer;
import com.codecool.askmateoop.dao.model.answer.AnswerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerService {

    private final AnswerDAO answerDAO;

    @Autowired
    public AnswerService(AnswerDAO answerDAO) {
        this.answerDAO = answerDAO;
    }

    public List<AnswerDTO> getAnswers(int questionId) {
        List<Answer> allAnswerForId = answerDAO.getAllAnswersById(questionId);
        return allAnswerForId.stream().map(a -> new AnswerDTO(
                a.getContent(),
                a.getCreatedAt(),
                a.getUserId()
        )).toList();
    }

    public int addNewAnswer(NewAnswerDTO answer) {
        return  answerDAO.createNewAnswer(answer);
    }

    public boolean deleteAnswer(int id){
        return answerDAO.deleteAnswer(id);
    }
}
