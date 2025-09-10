package com.codecool.askmateoop.model.payload.dto.question;

import java.sql.Timestamp;

public record QuestionDTO(
        int id, String title, String content, Timestamp created) {}
