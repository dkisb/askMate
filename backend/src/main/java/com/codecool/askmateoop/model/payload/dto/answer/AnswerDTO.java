package com.codecool.askmateoop.model.payload.dto.answer;

import java.sql.Timestamp;

public record AnswerDTO(int id, String content, Timestamp created) {
}
