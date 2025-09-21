package com.swl.booking.system.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.swl.booking.system.security.UserPrincipal;

import java.util.Arrays;
import java.util.Collection;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Collection<SimpleGrantedAuthority> authorities;
                boolean isSuperAdmin = false;
                
                // Determine authorities and admin status based on username
                if ("admin".equals(username)) {
                    authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    isSuperAdmin = true;
                } else {
                    authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
                    isSuperAdmin = false;
                }
                
                return new UserPrincipal(
                    1L, // id
                    username + "@example.com", // email
                    "password", // password
                    "Test Address", // orgNo/address
                    isSuperAdmin, // isSuperAdmin
                    authorities // authorities
                );
            }
        };
    }
}