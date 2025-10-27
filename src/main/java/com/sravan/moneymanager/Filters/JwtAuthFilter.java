package com.sravan.moneymanager.Filters;

import com.sravan.moneymanager.UserDetailsService.ProfileUserDetailsService;
import com.sravan.moneymanager.jwtUtilPackage.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
public class JwtAuthFilter extends OncePerRequestFilter {

    public JwtAuthFilter(JwtUtil jwtUtil, ProfileUserDetailsService profileUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.profileUserDetailsService = profileUserDetailsService;
    }
    public final JwtUtil jwtUtil;
    public final ProfileUserDetailsService profileUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String email = null;
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            email = jwtUtil.extractUsername(token);
        }
        if (email != null && SecurityContextHolder.getContext() .getAuthentication() == null) { UserDetails userDetails = profileUserDetailsService.loadUserByUsername(email); if (jwtUtil.validateToken(token, userDetails)) { UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()); authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); SecurityContextHolder.getContext() .setAuthentication(authToken); } } filterChain.doFilter(request, response);

    }
}
