package com.example.demo.middleware;

import com.example.demo.model.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenService {


    @Value("${jwt.secret}")
    private String jwtSecret;

    //Generate and return JWT
    public String generateAccessToken(User user) {
        Date expireDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(String.valueOf(user.getId()))
                .claim("user", Map.of(
                        "id", user.getId(),
                        "accountName", user.getAccountName(),
                        "nickname", user.getNickname(),
                        "image", user.getImage()
                ))
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}

