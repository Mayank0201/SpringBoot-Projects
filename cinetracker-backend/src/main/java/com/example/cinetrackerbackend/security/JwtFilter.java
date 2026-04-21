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
        String requestPath = request.getRequestURI();
        
        // Skip JWT validation for auth endpoints (they use permitAll() in SecurityConfig)
        if (requestPath.startsWith("/auth/") || requestPath.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader=request.getHeader("Authorization");

        //if no token continue ahead
        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = authHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception ex) {
            writeUnauthorized(response, "Invalid or expired token");
            return;
        }

        if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){

            User user=userRepository.findByUsername(username).orElse(null);
            
            if(user!=null && jwtService.isAccessTokenValid(token,user.getUsername())){

                UsernamePasswordAuthenticationToken authToken=
                new UsernamePasswordAuthenticationToken(user,null,Collections.emptyList());
            
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                writeUnauthorized(response, "Invalid token");
                return;
            }

        } else if (username == null) {
            writeUnauthorized(response, "Invalid token");
            return;

        }

        filterChain.doFilter(request,response);

    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\",\"status\":401}");
    }

}