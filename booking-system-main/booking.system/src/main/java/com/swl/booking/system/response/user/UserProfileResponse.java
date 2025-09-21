package com.swl.booking.system.response.user;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserProfileResponse implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 7891145518470672881L;

	private Long id;
	
	private String name;

	private String email;

	private String address;

	private String registrationDate;
}
