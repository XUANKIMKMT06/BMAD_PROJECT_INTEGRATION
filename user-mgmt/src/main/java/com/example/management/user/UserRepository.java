package com.example.management.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer save(AppUser user) {
        String sql = "INSERT INTO USER_TABLE (NAME, USERNAME, PASSWORD, ROLES) VALUES (?, ?, ?, ?)";

        // Key holder used to get the id set by the jdbcTemplate on the db
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Roles list stored as strings seperated by ','
        StringBuilder roles = new StringBuilder();
        user.getAuthorities().forEach(sga -> {
            roles.append(sga.getAuthority());
            roles.append(',');
        });

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getUsername());
                    ps.setString(3, user.getPassword());
                    ps.setString(4, roles.toString());
                    return ps;
                }, keyHolder
        );

        // return the Id of row stored in db
        return keyHolder.getKeyAs(Integer.class);
    }

    public Optional<AppUser> findByUsername(String username) {
        String sql = "SELECT * FROM USER_TABLE WHERE USERNAME = ?";
        return jdbcTemplate.query(
                sql,
                UserRowMapper::rowMapper,
                username
        ).stream().findFirst();
    }

    public List<AppUser> getAllUsers() {
        String sql = "SELECT * FROM USER_TABLE";
        return jdbcTemplate.query(sql, UserRowMapper::rowMapper);
    }

    public boolean delete(String username) {
        String sql = "DELETE FROM USER_TABLE WHERE USERNAME = ?";
        int succeeded = jdbcTemplate.update(sql, username);
        return succeeded == 1;
    }

    public void update(AppUser user) {
        String sql = "UPDATE USER_TABLE SET NAME = ?, USERNAME = ?, PASSWORD = ?, ROLES = ? WHERE id = ?";

        StringBuilder roles = new StringBuilder();
        user.getAuthorities().forEach(sga -> {
            roles.append(sga.getAuthority());
            roles.append(',');
        });

        jdbcTemplate.update(sql,
                user.getName(),
                user.getUsername(),
                user.getPassword(),
                roles,
                user.getId()
        );
    }
}
