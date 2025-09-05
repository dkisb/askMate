package com.codecool.askmateoop.controller.dto.question;

import java.sql.Timestamp;

public record QuestionDTO(
        int id,String title, String content, Timestamp created) {}
