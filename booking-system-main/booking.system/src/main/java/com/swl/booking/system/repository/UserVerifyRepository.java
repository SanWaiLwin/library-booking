package com.swl.booking.system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swl.booking.system.entity.UserVerify;

@Repository
public interface UserVerifyRepository extends JpaRepository<UserVerify, Long> {
	
	Optional<UserVerify> findByToken(String token);
	
	Optional<UserVerify> findByUserId(Long userId); 
}
