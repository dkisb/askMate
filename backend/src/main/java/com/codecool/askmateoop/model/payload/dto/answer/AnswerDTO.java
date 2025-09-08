package com.codecool.askmateoop.model.payload.dto.answer;

import java.sql.Timestamp;

public record AnswerDTO(String content, Timestamp created, int userId) {
}
