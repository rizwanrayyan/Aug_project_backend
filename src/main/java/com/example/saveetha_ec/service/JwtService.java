package com.example.saveetha_ec.service;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	private String secret_key="";
	public JwtService() {
		try {
			KeyGenerator keyGen=KeyGenerator.getInstance("HmacSHA256");
			SecretKey key=keyGen.generateKey();
			secret_key=Base64.getEncoder().encodeToString(key.getEncoded());
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}
	public String generateToken(String username,String role,long userId) {
		Map<String,Object> claims=new HashMap<>();
		claims.put("roles", role);
		claims.put("userId", userId);
		return Jwts.builder()
				.claims()
				.add(claims)
				.subject(username)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
				.and()
				.signWith(getKey())
				.compact();
	}
	public SecretKey getKey() {
		byte[] key=Decoders.BASE64.decode(secret_key);
		return Keys.hmacShaKeyFor(key);
	}
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	public <T> T extractClaim(String token,Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
	}
	public Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
