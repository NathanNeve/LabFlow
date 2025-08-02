package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.entity.Test;
import com.thomasmore.blc.labflow.entity.User;
import com.thomasmore.blc.labflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173/")  // allow origin van front-end
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping(value = "getusers")
    public List<User> readUsers(){
        return userService.readUsers();
    }

    // aanmaken van een user
    @PostMapping("/register")
    public User register(@RequestBody User user){
        return userService.register(user);
    }

    // login function for users
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user){
        return userService.verify(user);
    }

    // logout function for users
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return userService.logout();
    }

    @DeleteMapping("/deleteuser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        return userService.delete(id);
    }

    @PutMapping("/updateuser/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        try {
            return userService.update(id, user);
        } catch (Exception e) {
            throw new UniqueConstraintViolationException(e.getMessage());
        }
    }

    // update user without setting the password
    @PutMapping("/updateuserwithoutpassword/{id}")
    public ResponseEntity<User> updatewithoutpassword(@PathVariable Long id, @RequestBody User user) {
        try {
            return userService.updateWithoutPassword(id, user);
        } catch (Exception e) {
            throw new UniqueConstraintViolationException(e.getMessage());
        }
    }

}