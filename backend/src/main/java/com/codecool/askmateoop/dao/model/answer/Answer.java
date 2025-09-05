package com.codecool.askmateoop.dao.model.answer;

import java.sql.Timestamp;
import java.time.LocalDate;


public class Answer {
    private int id;
    private String content;
    private Timestamp createdAt;
    private int userId;
    private int questionId;

   public Answer(int id, int questionId, int userId, String content, Timestamp createdAt) {
        this.id = id;
        this.questionId = questionId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getContent() {
       return content;
    }
    public Timestamp getCreatedAt() {
       return createdAt;
    }

}