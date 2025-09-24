
package com.codecool.askmateoop.controller;

import com.codecool.askmateoop.model.payload.dto.question.QuestionDTO;
import com.codecool.askmateoop.model.payload.dto.user.*;
import com.codecool.askmateoop.service.QuestionService;
import com.codecool.askmateoop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final QuestionService questionService;

    @Autowired
    public UserController(UserService userService, QuestionService questionService) {
        this.userService = userService;
        this.questionService = questionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO loginRequest) {
        return userService.loginUser(loginRequest);
    }

    @PostMapping("/")
    public void addPoints(@RequestBody PointsDTO pointsDTO) {
        userService.addNewPoints(pointsDTO);
    }

    @PostMapping("/register")
    public void addNewUser(@RequestBody NewUserDTO newUser) {
        userService.createUser(newUser);
    }

    @GetMapping("/me")
    public LoginDTO me(){
        return userService.getMe();
    }

    @PatchMapping("/me")
    public void editedUser(@RequestBody ModifierDTO modifierDTO) {
        userService.editUser(modifierDTO);
    }

    @GetMapping("/email")
    public EmailDTO email(){
        return userService.getEmail();
    }

    @GetMapping("/points/{user_id}")
    public int getPoints(@PathVariable int user_id) {
        return userService.getReliabilityLevel(user_id);
   }

    @GetMapping("/myquestions")
    public List<QuestionDTO> getMyQuestions() {
        return questionService.getMyQuestions();
    }

    @PatchMapping("/")
    public void addNewPoints(@RequestBody PointsDTO pointsDTO) {
        userService.addNewPoints(pointsDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
    }
}
