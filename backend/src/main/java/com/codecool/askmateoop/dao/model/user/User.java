package com.codecool.askmateoop.dao.model.user;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

import java.time.LocalDate;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private boolean isAdmin;
    private Timestamp createdAt;
    private int reliabilityPoints;

    public User(int id, String username, String password, String email, boolean isAdmin, Timestamp createdAt, int reliabilityPoints) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
        this.createdAt = createdAt;
        this.reliabilityPoints = reliabilityPoints;
    }

    public int getId() {
        return id;
    }
}
