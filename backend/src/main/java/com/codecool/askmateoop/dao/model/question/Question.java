package com.codecool.askmateoop.dao.model.question;

import java.sql.Timestamp;


public class Question {
    private final int id;
    private final String title;
    private String content;
    private final Timestamp createdAt;
    private final int userId;

    public Question(int id, String title, String content, Timestamp date, int userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = date;
        this.userId = userId;

    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
