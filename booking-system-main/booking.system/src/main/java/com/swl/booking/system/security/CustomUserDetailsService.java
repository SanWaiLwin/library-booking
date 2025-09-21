package com.swl.booking.system.security;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swl.booking.system.entity.User;
import com.swl.booking.system.repository.UserRepository; 

@Service
public class CustomUserDetailsService implements UserDetailsService {
	protected final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

	@Autowired
	private UserRepository userRepository;
 
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<User> user = userRepository.findByEmail(email);
		if (user.isEmpty()) {
			logger.error("User Not Found");
			throw new UsernameNotFoundException("User Not Found");
		}

		return UserPrincipal.create(user.get());
	}
 
	@Transactional
	public UserDetails loadUserById(Long id) {// This method is used by JWTAuthenticationFilter
		Optional<User> user = userRepository.findById(id);
		if (user.isEmpty()) {
			logger.error("User Not Found");
			throw new UsernameNotFoundException("User Not Found");
		}

		return UserPrincipal.create(user.get());
	}
}
