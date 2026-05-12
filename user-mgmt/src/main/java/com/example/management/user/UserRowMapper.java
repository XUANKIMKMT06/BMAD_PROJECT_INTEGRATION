package com.example.management.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class UserRowMapper {
    public static AppUser rowMapper(ResultSet rs, int rowNum) throws SQLException {
        return new AppUser(
                rs.getInt("ID"),
                rs.getString("NAME"),
                rs.getString("USERNAME"),
                rs.getString("PASSWORD"),
                Arrays
                        .stream(rs.getString("ROLES").split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }
}