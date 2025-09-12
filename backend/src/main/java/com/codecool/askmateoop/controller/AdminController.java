package com.codecool.askmateoop.controller;

import com.codecool.askmateoop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}")
    public void makeUserMod(@PathVariable int id) {
        userService.makeUserMod(id);
    }

    @PatchMapping("/reverse/{id}")
    public void makeModUser(@PathVariable int id) {
        userService.makeModUser(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteAnyUser(id);
    }
}
