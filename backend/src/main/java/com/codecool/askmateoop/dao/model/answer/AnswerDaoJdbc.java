package com.codecool.askmateoop.dao.model.answer;

import com.codecool.askmateoop.controller.dto.answer.NewAnswerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class AnswerDaoJdbc implements AnswerDAO {


    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AnswerDaoJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Answer> getAllAnswersById(int id) {
        String sql = "SELECT id,user_id,content,created_at, question_id FROM answer WHERE question_id = ?";
        return jdbcTemplate.query(
                sql,
                new Object[]{id},
                (rs, rowNum) -> new Answer(
                        rs.getInt("id"),
                        rs.getInt("question_id"),
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at")
                )
        );
    }

    @Override
    public int createNewAnswer(NewAnswerDTO newAnswerDTO) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO answer (question_id, content,user_id) VALUES (?,?,?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1,newAnswerDTO.questionId() );
            ps.setString(2, newAnswerDTO.content());
            ps.setInt(3, newAnswerDTO.userId());
            return ps;
        }, keyHolder);
        if (keyHolder.getKeys() != null) {
            Object idObj = keyHolder.getKeys().get("id");
            if (idObj != null) {
                return ((Number) idObj).intValue();
            }
        }
        return -1;
    }

    @Override
    public boolean deleteAnswer(int id){
        String sql = "DELETE FROM answer WHERE id = ?";
        int affectedRows = jdbcTemplate.update(sql, id);
        return affectedRows > 0;
    }
}
