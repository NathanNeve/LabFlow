package com.thomasmore.blc.labflow.entity;

import com.thomasmore.blc.labflow.entity.auth.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// de methoden van UserDetails worden geimplementeerd
// object dient voor authenticatie users
// niet alle methodes worden gebruikt (returnen gewoon true), omdat we een simpele login hebben
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    // singleton zorgt ervoor dat we 1 object als "collection" kunnen returnen
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return user.getWachtwoord();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // methodes die niet echt gebruikt worden maar wel geïmplementeerd worden
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
