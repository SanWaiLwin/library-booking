package com.swl.booking.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date; 
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expirationins}")
	private long jwtExpirationIns;

	public String generateToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationIns * 3600000)) // Set expiration time
				.signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encodeToString(jwtSecret.getBytes())).compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(Base64.getEncoder().encodeToString(jwtSecret.getBytes())).parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getUsernameFromToken(String token) {
		return Jwts.parser().setSigningKey(Base64.getEncoder().encodeToString(jwtSecret.getBytes()))
				.parseClaimsJws(token).getBody().getSubject();
	}

	public Long getUserIdFromJWT(String token) {
		String secretKey = Base64.getEncoder().encodeToString(jwtSecret.getBytes());
		Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
		return Long.parseLong(claims.getSubject());
	}
}
