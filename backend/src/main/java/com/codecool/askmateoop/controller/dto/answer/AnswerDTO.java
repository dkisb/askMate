package com.codecool.askmateoop.controller.dto.answer;

import java.sql.Timestamp;

public record AnswerDTO(String content, Timestamp created, int userId) {
}
