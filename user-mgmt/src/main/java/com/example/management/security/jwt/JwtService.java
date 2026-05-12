package com.example.management.security.jwt;

import com.example.management.exceptions.InvalidJwtException;
import com.example.management.user.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final JwtRepository jwtRepository;
    @Value("${jwt.secret}")
    private String SECRET;

    public JwtService(JwtRepository jwtRepository) {
        this.jwtRepository = jwtRepository;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValidToken(Jwt token, UserDetails userDetails) {
        final String username = extractUsername(token.getToken());
        return
                username.equals(userDetails.getUsername()) &&
                        !isTokenExpired(token.getToken()) &&
                        !token.isLoggedOut();
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationToken(token).before(new Date());
    }

    private Date extractExpirationToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(UserDetails user) {
        var token = generateToken(new HashMap<>(), user);
        var jwt = new Jwt(token, (AppUser) user);
        jwtRepository.save(jwt);
        return token;
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey())
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(bytes);
    }

    public Jwt getToken(String token) {
        return jwtRepository.getJwt(token).orElseThrow(() -> new InvalidJwtException("Invalid web token"));
    }

    public void invalidate(String token) {
        Jwt jwt = getToken(token);
        jwt.setLoggedOut(true);
        jwtRepository.update(jwt);
    }
}
