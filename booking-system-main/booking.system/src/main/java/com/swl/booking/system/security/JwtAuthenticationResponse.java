package com.swl.booking.system.security;

import java.io.Serializable;

import com.swl.booking.system.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; 

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthenticationResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8275276585290883860L;
	private String token;
	private User user;
}
