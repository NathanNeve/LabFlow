package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.auth.User;
import com.thomasmore.blc.labflow.entity.UserPrincipal;
import com.thomasmore.blc.labflow.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional("authTransactionManager")
public class MyUserDetailsService implements UserDetailsService {

    // repo dat met onze users kan interageren
    @Autowired
    private UserRepository repo;

    // om UserDetailsService te implementeren moeten we 1 methode implementeren
    // om dit te doen moeten we met de database verbonden zijn en dit gebeurt via een repo
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = repo.findByEmail(email);

        if(user == null) {
            System.out.println("user not found");
            throw new UsernameNotFoundException("user not found");
        }

        // om dit te kunnen returnen moeten we de methodes van UserDetails implementeren in UserPrincipal
        return new UserPrincipal(user);
    }
}
