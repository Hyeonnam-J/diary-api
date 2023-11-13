package com.hn.api.diary.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hn.api.diary.config.JwsKey;
import com.hn.api.diary.dto.SessionDTO;
import com.hn.api.diary.response.SessionResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class AccessFilter extends OncePerRequestFilter {

    @Autowired private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // /signIn -> signInFilter()
        if(request.getRequestURI().equals("/signIn")){
            filterChain.doFilter(request, response);
            return;
        }

        SessionResponse sessionResponse = objectMapper.readValue(request.getInputStream(), SessionResponse.class);
        String jws = sessionResponse.getAccessToken();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(JwsKey.getJwsSecretKey())
                    .build()
                    .parseSignedClaims(jws)
                    .getPayload();

            Date generateDate = claims.getIssuedAt();
            Date expirateDate = claims.getExpiration();

            String jwtSubject = claims.getSubject();
            SessionDTO sessionDTO = objectMapper.readValue(jwtSubject, SessionDTO.class);

            List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(sessionDTO.getRole());
            Authentication authentication = new UsernamePasswordAuthenticationToken(sessionDTO.getEmail(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // go controller
            filterChain.doFilter(request, response);
        }catch (ExpiredJwtException e){
            // todo: return errorResponse
            e.printStackTrace();
        }
    }
}
