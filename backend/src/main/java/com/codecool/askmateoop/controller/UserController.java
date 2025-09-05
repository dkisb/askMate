
package com.codecool.askmateoop.controller;

import com.codecool.askmateoop.controller.dto.user.LoginDTO;
import com.codecool.askmateoop.controller.dto.user.LoginRequestDTO;
import com.codecool.askmateoop.controller.dto.user.NewUserDTO;
import com.codecool.askmateoop.controller.dto.user.PointsDTO;
import com.codecool.askmateoop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginDTO loginUser(@RequestBody LoginRequestDTO loginRequest) {
        return userService.loginUser(loginRequest.username(), loginRequest.password());
    }


    @GetMapping("/{user_id}/points")
    public int getPoints(@PathVariable int user_id) {
        return userService.getReliabilityLevel(user_id);
    }

    @PostMapping("/")
    public boolean addNewUser(@RequestBody NewUserDTO newUser) {
        return userService.addNewUser(newUser);
    }

    @PatchMapping("/")
    public Map<String, String> addNewPoints(@RequestBody PointsDTO pointsDTO) {
        return userService.addNewPoints(pointsDTO);
    }
}
