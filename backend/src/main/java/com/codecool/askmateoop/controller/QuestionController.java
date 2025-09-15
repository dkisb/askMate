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

    @GetMapping("/like/{id}")
    public int getLikes(@PathVariable int id) {
        return questionService.getLikes(id);
    }
    @GetMapping("/dislike/{id}")
    public int getDislikes(@PathVariable int id) {
        return questionService.getDislikes(id);
    }

    @PatchMapping("/like/{id}")
    public void likeQuestion(@PathVariable int id) {
        questionService.likeQuestion(id);
    }

    @PatchMapping("/dislike/{id}")
    public void dislikeQuestion(@PathVariable int id) {
        questionService.dislikeQuestion(id);
    }



    @DeleteMapping("/{id}")
    public void deleteQuestionById(@PathVariable int id) {
        questionService.deleteQuestionById(id);
    }
}
