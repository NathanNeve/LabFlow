package com.thomasmore.blc.labflow.config;
// security filter voor het valideren van JWT token
import com.thomasmore.blc.labflow.service.JWTService;
import com.thomasmore.blc.labflow.service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter extend, aangezien we verder willen bouwen op een filter
// component zorgt ervoor dat deze class (bean) automatisch geinstantieerd wordt door spring
// dit maakt dependency injection (autowired) mogelijk
// https://www.baeldung.com/spring-component-annotation
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    ApplicationContext context;

    // enige methode die geïmplementeerd moet worden
    /*@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // van de client krijgen we een request met authheader in dit format: "Bearer jwttokenbestaandeuitveelkarakters"
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // we gaan na of de token valide is en start met "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // extractie van de token zonder "Bearer "
            token = authHeader.substring(7);
            // methode om email uit de token te halen in jwtService
            email = jwtService.extractEmail(token);
        }

        // email mag niet null zijn, en mag nog niet geauthenticeerd zijn
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Haalt de UserDetails op uit de context om na te gaan of het emailadres in de database zit
            UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(email);

            // Controleert of het emailadres in de database staat met behulp van de JWT-token validatie
            if(jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // Koppelt de authenticatiedetails aan de authToken
                // Dit zorgt ervoor dat de token weet van de request-informatie
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Stelt de beveiligingscontext in met de nieuwe authenticatietoken
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // voer filter uit en gaat naar de volgende filter
        filterChain.doFilter(request, response);
    }
}*/
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Haal de JWT uit een cookie in plaats van de Authorization header.
        String token = null;
        String email = null;

        // Controleer of de cookies bestaan in de request.
        if (request.getCookies() != null) {
            // Loop door de cookies om de juiste te vinden.
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break; // Stop met zoeken zodra de cookie is gevonden.
                }
            }
        } else if ("/login".equals(request.getRequestURI())) {
            // Allow requests to /login without a cookie
            filterChain.doFilter(request, response);
            return;
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // Stop further processing
        }

        // De logica hieronder blijft grotendeels hetzelfde.
        // De check op "Bearer " is verwijderd, omdat een cookie meestal alleen de token zelf bevat.
        if (token != null) {
            // Methode om email uit de token te halen in jwtService.
            email = jwtService.extractEmail(token);
        }

        // Email mag niet null zijn, en mag nog niet geauthenticeerd zijn.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Haalt de UserDetails op uit de context om na te gaan of het emailadres in de database zit.
            UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(email);

            // Controleert of het emailadres in de database staat met behulp van de JWT-token validatie.
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // Koppelt de authenticatiedetails aan de authToken.
                // Dit zorgt ervoor dat de token weet van de request-informatie.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Stelt de beveiligingscontext in met de nieuwe authenticatietoken.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Voer filter uit en gaat naar de volgende filter.
        filterChain.doFilter(request, response);
    }
}
