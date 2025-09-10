
package com.codecool.askmateoop.controller;

import com.codecool.askmateoop.model.payload.dto.user.LoginRequestDTO;
import com.codecool.askmateoop.model.payload.dto.user.NewUserDTO;
import com.codecool.askmateoop.model.payload.dto.user.PointsDTO;
import com.codecool.askmateoop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO loginRequest) {
        return userService.loginUser(loginRequest);
    }

    @PostMapping("/register")
    public void addNewUser(@RequestBody NewUserDTO newUser) {
        userService.createUser(newUser);
    }

    @GetMapping("/{user_id}/points")
    public int getPoints(@PathVariable int user_id) {
        return userService.getReliabilityLevel(user_id);
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
