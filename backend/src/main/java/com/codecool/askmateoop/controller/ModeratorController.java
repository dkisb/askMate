package com.codecool.askmateoop.controller;

import com.codecool.askmateoop.model.payload.dto.answer.UpdatedAnswerDTO;
import com.codecool.askmateoop.model.payload.dto.question.UpdatedQuestionDTO;
import com.codecool.askmateoop.service.AnswerService;
import com.codecool.askmateoop.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moderator")
public class ModeratorController {
    private final QuestionService questionService;
    private final AnswerService answerService;

    @Autowired
    public ModeratorController(QuestionService questionService, AnswerService answerService) {
        this.questionService = questionService;
        this.answerService = answerService;
    }

    @PutMapping("/question")
    public void editAnyQuestion(@RequestBody UpdatedQuestionDTO question) {
        questionService.updateAnyQuestion(question);
    }

    @DeleteMapping("/question/{id}")
    public void deleteQuestion(@PathVariable int id) {
        questionService.deleteAnyQuestionById(id);
    }

    @PatchMapping("/")
    public void editAnswer(@RequestBody UpdatedAnswerDTO answer) {
        answerService.updateAnyAnswer(answer);
    }

    @DeleteMapping("/answer/{id}")
    public void deleteAnswer(@PathVariable int id) {
        answerService.deleteAnyAnswer(id);
    }
}
