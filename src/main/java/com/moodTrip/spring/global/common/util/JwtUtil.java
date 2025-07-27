package com.moodTrip.spring.global.common.util;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.security.Key;
import java.util.Date;
import java.util.Base64;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyRaw;

    @Value("${jwt.expiration}")
    private long accessTokenValidityMs;

    private Key key;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyRaw);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 생성
    public String generateToken(String memberId, Long memberPk) {

        Claims claims = Jwts.claims()
                .add("memberId", memberId)
                .add("memberPk", memberPk)
                .build();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);

        // import io.jsonwebtoken.Jwts;
        return Jwts.builder()
                .claims(claims)          // 혹은 .setClaims()
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256) // 두 번째 파라미터는 반드시 SignatureAlgorithm.HS256!
                .compact();
    }

    // 토큰에서 회원ID 추출
    public String getMemberId(String token) {
        return getClaims(token).get("memberId", String.class);
    }

    // 토큰에서 회원PK 추출
    public Long getMemberPk(String token) {
        Integer pkInt = getClaims(token).get("memberPk", Integer.class);
        return pkInt != null ? pkInt.longValue() : null;
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }

    // 토큰에서 Claims 추출 (payload 정보)
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}