package com.codecool.askmateoop.service;


import com.codecool.askmateoop.controller.dto.user.LoginDTO;
import com.codecool.askmateoop.controller.dto.user.PointsDTO;
import com.codecool.askmateoop.dao.model.user.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.codecool.askmateoop.controller.dto.user.NewUserDTO;

import java.util.Map;

@Service
public class UserService {
    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public LoginDTO loginUser(String name, String password) {
        return userDAO.logInUser(name, password);
    }

    public int getReliabilityLevel(int id) {
        return userDAO.getReliabilityLevel(id);
    }

    public boolean addNewUser(NewUserDTO newUser) {
        return userDAO.addUser(newUser);
    }

    public Map<String, String> addNewPoints(PointsDTO pointsDTO) {
        return userDAO.addNewPoints(pointsDTO);
    }
}
