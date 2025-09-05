package com.codecool.askmateoop.dao.model.question;

import com.codecool.askmateoop.controller.dto.question.NewQuestionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class QuestionDaoJdbc implements QuestionDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public QuestionDaoJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Question> getAllQuestions() {
        String sql = "SELECT id,title,content,created_at,user_id FROM question";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Question(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("created_at"),
                rs.getInt("user_id")
        ));
    }
    @Override
    public Question getQuestionById(int id) {
        String sql = "SELECT id,title,content,created_at,user_id FROM question WHERE id=? ";

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> new Question(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("created_at"),
                rs.getInt("user_id")
        ));
    }

    @Override
    public int addQuestion(NewQuestionDTO newQuestion) {
        String sql = "INSERT INTO question(title, content, user_id) VALUES (?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newQuestion.title());
            ps.setString(2, newQuestion.content());
            ps.setInt(3, newQuestion.userId());
            return ps;
        }, keyHolder);

        if(keyHolder.getKeys() != null) {
            Object isObj = keyHolder.getKeys().get("id");
            if(isObj != null) {
                return((Number) isObj).intValue();
            }
        }
        return -1;
    }




    @Override
    public boolean deleteQuestion(int id){
        String sql = "DELETE FROM question WHERE id = ?";
        int affectedRows = jdbcTemplate.update(sql,id);
        return affectedRows > 0;
    }
}


