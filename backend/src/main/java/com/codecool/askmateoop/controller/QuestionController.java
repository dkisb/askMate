package com.codecool.askmateoop.controller;

import com.codecool.askmateoop.model.payload.dto.question.NewQuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.QuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.UpdatedQuestionDTO;
import com.codecool.askmateoop.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/question")
public class QuestionController {
    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/all")
    public List<QuestionDTO> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/{id}")
    public QuestionDTO getQuestionById(@PathVariable int id) {
        return questionService.getQuestionById(id);}

    @PostMapping("/")
    public int addNewQuestion(@RequestBody NewQuestionDTO question) {
        return questionService.addNewQuestion(question);
    }

    @PutMapping("/")
    public void updateQuestion(@RequestBody UpdatedQuestionDTO question) {
        questionService.updateQuestion(question);
    }

    @DeleteMapping("/{id}")
    public void deleteQuestionById(@PathVariable int id) {
        questionService.deleteQuestionById(id);
    }
}
