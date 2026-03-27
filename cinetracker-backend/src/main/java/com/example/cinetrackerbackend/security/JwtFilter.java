package com.example.cinetrackerbackend.security;

import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.user.User;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter{

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain)
    throws ServletException, IOException
    {

        final String authHeader=request.getHeader("Authorization");

        //if no token continue ahead
        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = authHeader.substring(7);
        String username=jwtService.extractUsername(token);

        if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){

            User user=userRepository.findByUsername(username).orElseThrow(null);
            
            if(user!=null && jwtService.isTokenValid(token,user.getUsername())){

                UsernamePasswordAuthenticationToken authToken=
                new UsernamePasswordAuthenticationToken(user,null,Collections.emptyList());
            
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

            }

            filterChain.doFilter(request,response);

        }

    }

}