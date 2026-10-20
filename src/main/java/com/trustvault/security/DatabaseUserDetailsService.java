package com.trustvault.security;

import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public DatabaseUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUser appUser = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("Invalid email or password"));

        return User.withUsername(appUser.getEmail())
            .password(appUser.getPasswordHash())
            .roles(appUser.getRole().name())
            .build();
    }
}
