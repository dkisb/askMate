package com.codecool.askmateoop.controller.dto.answer;

import java.sql.Timestamp;

public record NewAnswerDTO(String content,int questionId, int userId) {
}
