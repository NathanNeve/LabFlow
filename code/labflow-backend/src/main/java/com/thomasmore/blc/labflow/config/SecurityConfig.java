package com.thomasmore.blc.labflow.config;
// file voor het registreren van beans met betrekking tot authenticatie
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
// dit zorgt ervoor dat we niet de default security provider gaan gebruiken
// we gaan niet de default flow volgen maar deze provider "DaoAuthenticationProvider"
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // disable csrf (stateless applicatie heeft dit niet nodig)
        http.csrf(AbstractHttpConfigurer::disable);
        // cors moet aanstaan met onze configuratie, in bean hieronder te zien
        //http.cors(Customizer.withDefaults());

        // alle requests moeten geauthenticeerd zijn behalve login
        http.authorizeHttpRequests(request -> request
                .requestMatchers("login").permitAll()
                .anyRequest().authenticated());

        // enabled form based login
        http.httpBasic(Customizer.withDefaults());

        // specifieren dat we een stateless applicatie bouwen
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // filter VOOR de user authenticatiefilter
        // parameter 1: onze jwt filter, parameter 2: de filter die na onze filter komt
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // build geeft ons het object "securityfilterchain" terug
        return http.build();
    }

    // bean voor het configureren van CORS
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:5173",
                "https://2425-tm-blc-001-labflow.pages.dev"
        ));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }


    // bean voor mee te geven welke users verified zijn
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // Om DaoAuthenticationProvider te doen werken moeten 2 zaken gespecifieerd worden:
        // 1 De passwordencoder voor encryptie
        // hierdoor weet de provider dat hij het passwoord verkregen van login moet hashen met Bcrypt in 12 rondes
        // dit vergelijkt hij met de hash in de database
        provider.setPasswordEncoder(new BCryptPasswordEncoder(4));

        // 2 Onze eigen userDetailsService
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }


    // bean AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
