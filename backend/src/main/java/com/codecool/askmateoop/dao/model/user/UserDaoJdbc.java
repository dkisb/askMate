package com.codecool.askmateoop.dao.model.user;

import com.codecool.askmateoop.controller.dto.user.LoginDTO;
import com.codecool.askmateoop.controller.dto.user.NewUserDTO;
import com.codecool.askmateoop.controller.dto.user.PointsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;


@Repository
public class UserDaoJdbc implements UserDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDaoJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LoginDTO logInUser(String username, String password) {
        String sql = "SELECT id, name FROM users WHERE name = ? AND password_hash = ?";

        return jdbcTemplate.query(sql, new Object[]{username, password}, rs -> {
            if (rs.next()) {
                int userId = rs.getInt("id");
                String name = rs.getString("name");
                return new LoginDTO(name, userId);
            } else {
                return new LoginDTO(null, 0); // or throw exception, or use Optional
            }
        });
    }


    @Override
    public int getReliabilityLevel(int id) {
        String sql = "SELECT COALESCE(reliability_points, 0) from users where id=?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception e) {
            System.err.println("Error during getting points: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean addUser(NewUserDTO newUser) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE name = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, newUser.username());

        if (count != null && count > 0) {
            return true; // user already exists
        }

        String insertSql = "INSERT INTO users(name, password_hash, email, created_at) VALUES (?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Timestamp timestamp = Timestamp.valueOf(now);

        jdbcTemplate.update(insertSql,
                newUser.username(),
                newUser.password(),
                newUser.email(),
                timestamp
        );

        return false; // user was added
    }

    @Override
    public Map<String, String> addNewPoints(PointsDTO pointsDTO) {
        try {
            String selectSql = "SELECT reliability_points FROM users WHERE id = ?";
            Integer currentPoints = jdbcTemplate.queryForObject(selectSql, Integer.class, pointsDTO.userId());
            if (currentPoints == null) {
                return Map.of("message", "Something went wrong when points added");
            }
            int updatedPoints = currentPoints + pointsDTO.points();
            String updateSql = "UPDATE users SET reliability_points = ? WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(updateSql, updatedPoints, pointsDTO.userId());
            if (rowsAffected > 0) {
                return Map.of("message", "Points added");
            } else {
                return Map.of("message", "Something went wrong when points added");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Something went wrong when points added");
        }
    }


}
