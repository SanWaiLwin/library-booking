package com.swl.booking.system.response.user;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserLoginResponse implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -4949512248971401757L;

	private Long id;
	
	private String name;
	
	private String email;
	
	private String token;
	
	// Manual getter and setter methods
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
}
