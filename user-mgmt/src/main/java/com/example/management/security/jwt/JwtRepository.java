package com.example.management.security.jwt;

import com.example.management.user.AppUser;
import com.example.management.user.UserRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JwtRepository {
    private final JdbcTemplate jdbcTemplate;

    public JwtRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Jwt rowMapper(ResultSet rs, int rowNumber) throws SQLException {
        AppUser user = UserRowMapper.rowMapper(rs, rowNumber);
        return new Jwt(
                rs.getInt("ID"),
                rs.getString("TOKEN"),
                user,
                rs.getBoolean("IS_LOGGED_OUT")
        );
    }

    public List<Jwt> getAllTokens() {
        String sql = """
                    SELECT *
                    FROM TOKEN_TABLE T
                    INNER JOIN USER_TABLE U
                    ON T.USER_ID = U.ID
                    WHERE T.IS_LOGGED_OUT = FALSE
                """;
        return jdbcTemplate.query(sql, this::rowMapper);
    }

    public Optional<Jwt> getJwt(String token) {
        String sql = """
                    SELECT *
                    FROM TOKEN_TABLE T
                    INNER JOIN USER_TABLE U
                    ON T.USER_ID = U.ID
                    WHERE T.TOKEN = ?
                """;
        return jdbcTemplate
                .query(sql, this::rowMapper, token)
                .stream()
                .findFirst();
    }

    public void save(Jwt token) {
        String sql = "INSERT INTO TOKEN_TABLE (USER_ID, TOKEN, IS_LOGGED_OUT) values ( ?, ?, ? )";
        jdbcTemplate.update(
                sql,
                token.getUser().getId(),
                token.getToken(),
                token.isLoggedOut()
        );
    }

    public void update(Jwt token) {
        String sql = "UPDATE TOKEN_TABLE SET IS_LOGGED_OUT = ? WHERE ID = ?";
        jdbcTemplate.update(
                sql,
                token.isLoggedOut(),
                token.getId()
        );
    }
}
