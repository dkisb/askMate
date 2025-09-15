package com.codecool.askmateoop.controller;

import com.codecool.askmateoop.model.payload.dto.answer.AnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.NewAnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.UpdatedAnswerDTO;
import com.codecool.askmateoop.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/answer")
public class AnswerController {
    private final AnswerService answerService;

    @Autowired
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping("/{question_id}")
    public List<AnswerDTO> getAllAnswers(@PathVariable("question_id") int id) {
        return answerService.getAnswers(id);
    }

    @GetMapping("/a/{id}")
    public AnswerDTO getAnswerById(@PathVariable("id") int id) {
        return answerService.getAnswer(id);
    }

    @PostMapping("/")
    public void addNewAnswer(@RequestBody NewAnswerDTO newAnswerDTO) {
        answerService.addNewAnswer(newAnswerDTO);
    }

    @PostMapping("/a/{parent_id}")
    public void addNewComment(@PathVariable("parent_id") int id, @RequestBody NewAnswerDTO newAnswerDTO) {
        answerService.addNewComment(id, newAnswerDTO);
    }

    @PatchMapping("/")
    public void updateAnswer(@RequestBody UpdatedAnswerDTO answerDTO) {
        answerService.updateAnswer(answerDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteQuestionById(@PathVariable int id) {
        answerService.deleteAnswer(id);
    }
}
