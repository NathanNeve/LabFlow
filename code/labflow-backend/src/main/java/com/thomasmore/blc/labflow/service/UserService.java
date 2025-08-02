package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.Test;
import com.thomasmore.blc.labflow.entity.User;
import com.thomasmore.blc.labflow.repository.UserRepository;
// transactional zorgt ervoor dat een methode met meerdere database interacties volgens het ACID principe werkt
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    JWTService jwtService;

    // BcryptEncoder heeft 1 parameter 'strength'
    // hoe hoger het getal, hoe meer het wachtwoord wordt gehasht, maar hoe meer compute nodig is
    final  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);


    public List<User> readUsers(){
        return userRepository.findAll();
    }

    public User register(User user){
        // hashen van het wachtwoord
        user.setWachtwoord(encoder.encode(user.getWachtwoord()));
        return userRepository.save(user);
    }

    // authentication of user
    // IMPORTANT NOTE: this should not give back a json, it should give back a Cookie that is auto-set by the browser
    public ResponseEntity<?> verify(User user) {
        try {
            Authentication authentication =
                    authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getWachtwoord()));

            if (authentication.isAuthenticated()) {
                User fullUser = userRepository.findByEmail(user.getEmail());
                String token = jwtService.generateToken(user);

                System.out.println("Generated JWT token: " + token);

                fullUser.setRefreshToken(token); // store the JWT token in the user object, if needed
                userRepository.save(fullUser); // save the user with the new token

                System.out.println("User authenticated successfully: " + user.getEmail());

                // create a Cookie with the JWT token
                // Note: In production application, you would set the HttpOnly and Secure flags on the cookie
                ResponseCookie cookie = ResponseCookie.from(("token"), token)
                        .httpOnly(true) // prevents JavaScript access to the cookie
                        .secure(false) // ensures the cookie is sent over HTTPS only (change in production)
                        .path("/") // cookie is valid for the entire application
                        .maxAge(7 * 24 * 60 * 60) // cookie expires in 7 days
                        .sameSite("Strict") // prevents CSRF attacks
                        .build();

                // return the user with a blank password and the JWT token in a cookie
                fullUser.setWachtwoord("This is not for your eyes! You naughty hacker!");
                // Stop token in een JSON format
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(fullUser);
            }
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Problem with Authentication");
    }

    // delete
    public ResponseEntity<Integer> delete(Long id) {
        User deleteUser = userRepository.findById(id);
        if (deleteUser != null) {
            userRepository.delete(deleteUser);
            return new ResponseEntity<>(userRepository.findAll().size(), HttpStatus.OK);
        }
        return new ResponseEntity<>(userRepository.findAll().size(), HttpStatus.NOT_FOUND);
    }

    // update
    public ResponseEntity<User> update(Long id, User user) {
        User updateUser = userRepository.findById(id);
        if (updateUser != null) {
            updateUser.setEmail(user.getEmail());
            updateUser.setRol(user.getRol());
            updateUser.setWachtwoord(encoder.encode(user.getWachtwoord()));
            updateUser.setVoorNaam(user.getVoorNaam());
            updateUser.setAchterNaam(user.getAchterNaam());
            userRepository.save(updateUser);
            return new ResponseEntity<>(updateUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // update without password
    public ResponseEntity<User> updateWithoutPassword(Long id, User user) {
        User updateUser = userRepository.findById(id);
        if (updateUser != null) {
            updateUser.setEmail(user.getEmail());
            updateUser.setRol(user.getRol());
            updateUser.setVoorNaam(user.getVoorNaam());
            updateUser.setAchterNaam(user.getAchterNaam());
            userRepository.save(updateUser);
            return new ResponseEntity<>(updateUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}